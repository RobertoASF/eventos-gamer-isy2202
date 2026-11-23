#!/bin/bash
# Ejecuta el analisis dinamico de OWASP ZAP (imagen oficial zaproxy/zap-stable) contra el sitio y las API.
#
# Uso:
#   ZAP_CLAVE='clave-del-usuario-de-prueba' ./escanear.sh reportes/antes
#
# Variables opcionales:
#   OBJETIVO     URL base del sitio. Por defecto http://nginx (el contenedor de nginx en la red de Docker).
#   RED          Red de Docker donde corren los servicios. Por defecto eventos-gamer_interna
#   ZAP_USUARIO  Usuario de prueba con el que ZAP inicia sesion. Por defecto mati
#   SOLO_WEB     Con 1 escanea solo el sitio. Se usa desde fuera de la VM, donde las API no son accesibles:
#                OBJETIVO=https://[IP] RED=bridge SOLO_WEB=1 ZAP_CLAVE='...' ./escanear.sh reportes/vm
set -uo pipefail
export MSYS_NO_PATHCONV=1

cd "$(dirname "$0")"
CARPETA="$(pwd -W 2>/dev/null || pwd)"

REPORTES="${1:-reportes/ultimo}"
OBJETIVO="${OBJETIVO:-http://nginx}"
ZAP_USUARIO="${ZAP_USUARIO:-mati}"
ZAP_CLAVE="${ZAP_CLAVE:?Falta la variable ZAP_CLAVE con la clave del usuario de prueba}"
API_EVENTOS="${API_EVENTOS:-http://ms-eventos:8082}"
API_USUARIOS="${API_USUARIOS:-http://ms-usuarios:8081}"
RED="${RED:-eventos-gamer_interna}"
IMAGEN="zaproxy/zap-stable"

mkdir -p "$REPORTES" .tmp
chmod a+w "$REPORTES" .tmp 2>/dev/null || true

# Deja el plan listo con los valores de esta ejecucion. Queda en .tmp y se borra al terminar
preparar_plan() {
    sed -e "s#\${OBJETIVO}#$OBJETIVO#g" \
        -e "s#\${ZAP_USUARIO}#$ZAP_USUARIO#g" \
        -e "s#\${ZAP_CLAVE}#$ZAP_CLAVE#g" \
        -e "s#\${API_EVENTOS}#$API_EVENTOS#g" \
        -e "s#\${API_USUARIOS}#$API_USUARIOS#g" \
        -e "s#\${REPORTES}#$REPORTES#g" \
        "$1" > ".tmp/$1"
}

# Corre ZAP en segundo plano y espera a que termine, asi un corte de la terminal no detiene el escaneo
ejecutar_zap() {
    local nombre="$1" plan="$2"
    shift 2
    docker rm -f "$nombre" > /dev/null 2>&1
    docker run -d --name "$nombre" --network "$RED" -v "$CARPETA:/zap/wrk:rw" "$@" \
        "$IMAGEN" zap.sh -cmd -autorun "/zap/wrk/.tmp/$plan" > /dev/null
    docker wait "$nombre" > /dev/null
    docker logs "$nombre" 2>&1 | grep -v -E "^Job .* set |^[0-9]+ \[main\]|prefs|preferences directory" | tee "$REPORTES/$nombre.log"
    docker rm "$nombre" > /dev/null
}

trap 'rm -rf .tmp' EXIT

echo "== Sitio web: $OBJETIVO/eventos"
preparar_plan plan-web.yaml
ejecutar_zap zap-web plan-web.yaml

if [ "${SOLO_WEB:-0}" = "1" ]; then
    echo "== Reportes en zap/$REPORTES (solo sitio web)"
    exit 0
fi

echo "== Token JWT para las rutas privadas de la API"
# Desde la semana 5 el login exige el token CSRF: se pide en /api/auth/csrf y se envia en la cookie y en el encabezado
TOKEN=$(docker run --rm --network "$RED" -e API="$API_USUARIOS" -e USUARIO="$ZAP_USUARIO" -e CLAVE="$ZAP_CLAVE" \
    --entrypoint bash "$IMAGEN" -c '
    R=$(curl -s -D /tmp/cab "$API/api/auth/csrf"); T=$(echo "$R" | sed -E "s/.*\"token\":\"([^\"]+)\".*/\1/")
    C=$(grep -i set-cookie /tmp/cab | sed -E "s/.*(XSRF-TOKEN=[^;]+).*/\1/")
    curl -s -X POST "$API/api/auth/login" -H "X-XSRF-TOKEN: $T" -H "Cookie: $C" -H "Content-Type: application/json" \
        -d "{\"nombreUsuario\":\"$USUARIO\",\"password\":\"$CLAVE\"}"' | sed -E 's/.*"token":"([^"]+)".*/\1/')
echo "Token obtenido: ${TOKEN:0:20}..."

echo "== API: $API_EVENTOS y $API_USUARIOS"
preparar_plan plan-api.yaml
ejecutar_zap zap-api plan-api.yaml -e ZAP_AUTH_HEADER=Authorization -e "ZAP_AUTH_HEADER_VALUE=Bearer $TOKEN"

echo "== Reportes en zap/$REPORTES"
