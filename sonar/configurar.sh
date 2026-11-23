#!/bin/bash
# Configura SonarQube para el proyecto Eventos Gamer usando su API web:
#   1. Clasificacion "Standard Experience": tipos Vulnerability, Bug, Code Smell y Security Hotspot,
#      con severidades Blocker, Critical, Major, Minor e Info.
#   2. Perfiles de calidad para Java, HTML y JavaScript: copia de "Sonar way core" (el perfil por defecto)
#      mas todas las reglas de seguridad (vulnerabilidades y hotspots) de cada lenguaje.
#   3. Quality Gate "Eventos Gamer - Seguridad": falla si hay vulnerabilidades, problemas bloqueantes o criticos,
#      hotspots sin revisar o una calificacion de seguridad o confiabilidad distinta de A.
#   4. Un proyecto por componente: el sitio (frontend) y las dos API (backend).
#   5. Token de analisis para el scanner de Maven, que se guarda en .env.
# Uso: ./configurar.sh   (lee SONAR_ADMIN_CLAVE desde .env)
set -euo pipefail
cd "$(dirname "$0")"
source .env

URL=${SONAR_URL:-http://localhost:9100}
AUTH="admin:${SONAR_ADMIN_CLAVE}"
GATE="Eventos Gamer - Seguridad"

api() { curl -sf -u "$AUTH" -X POST "$URL/api/$1" "${@:2}"; }

echo "1. Modo de clasificacion Standard Experience"
curl -sf -u "$AUTH" -X PATCH -H "Content-Type: application/merge-patch+json" \
    -d '{"mode":"STANDARD_EXPERIENCE"}' "$URL/api/v2/clean-code-policy/mode"
echo

echo "2. Perfiles de calidad: Sonar way core + todas las reglas de seguridad (Java, HTML y JavaScript)"
clave_perfil() {
    curl -sf -u "$AUTH" "$URL/api/qualityprofiles/search?language=$1" | python -c \
        "import sys,json; print(next((p['key'] for p in json.load(sys.stdin)['profiles'] if p['name']==sys.argv[1]), ''))" "$2"
}
perfil() {
    local lenguaje=$1 nombre=$2 base clave
    base=$(clave_perfil "$lenguaje" "Sonar way core")
    clave=$(clave_perfil "$lenguaje" "$nombre")
    if [ -z "$clave" ]; then
        api qualityprofiles/copy -d fromKey="$base" --data-urlencode toName="$nombre" > /dev/null
        clave=$(clave_perfil "$lenguaje" "$nombre")
    fi
    echo -n "   $nombre: reglas de seguridad activadas "
    api qualityprofiles/activate_rules -d targetKey="$clave" -d languages="$lenguaje" \
        -d types=VULNERABILITY,SECURITY_HOTSPOT -d activation=false -d statuses=READY -d is_template=false
    echo
    api qualityprofiles/set_default -d language="$lenguaje" --data-urlencode qualityProfile="$nombre"
}
perfil java "Eventos Gamer - Java"
perfil web "Eventos Gamer - HTML"
perfil js "Eventos Gamer - JavaScript"

echo "3. Quality Gate '$GATE'"
api qualitygates/create --data-urlencode name="$GATE" > /dev/null 2>&1 || true
condicion() {
    api qualitygates/create_condition --data-urlencode gateName="$GATE" -d metric="$1" -d op="$2" -d error="$3" \
        > /dev/null 2>&1 || echo "   (la condicion $1 ya existia)"
}
condicion vulnerabilities GT 0
condicion security_rating GT 1
condicion security_hotspots_reviewed LT 100
condicion blocker_violations GT 0
condicion critical_violations GT 0
condicion reliability_rating GT 1
# Al crear un gate SonarQube agrega condiciones para el codigo nuevo. La cobertura de pruebas no es el foco
# de esta revision de seguridad, asi que se quita esa condicion y se mantienen las demas
ID_COBERTURA=$(curl -sf -u "$AUTH" --get --data-urlencode name="$GATE" "$URL/api/qualitygates/show" | python -c \
    "import sys,json; print(next((c['id'] for c in json.load(sys.stdin)['conditions'] if c['metric']=='new_coverage'), ''))")
[ -n "$ID_COBERTURA" ] && api qualitygates/delete_condition -d id="$ID_COBERTURA"
api qualitygates/set_as_default --data-urlencode name="$GATE"

echo "4. Proyectos"
proyecto() {
    api projects/create -d project="$1" --data-urlencode name="$2" -d mainBranch=main > /dev/null 2>&1 \
        || echo "   (el proyecto $1 ya existia)"
}
proyecto eventos-gamer-web "Eventos Gamer - web-eventos (frontend)"
proyecto eventos-gamer-ms-usuarios "Eventos Gamer - ms-usuarios (backend)"
proyecto eventos-gamer-ms-eventos "Eventos Gamer - ms-eventos (backend)"

echo "5. Token de analisis"
if [ -z "${SONAR_TOKEN:-}" ]; then
    TOKEN=$(api user_tokens/generate -d name=analisis-eventos-gamer -d type=GLOBAL_ANALYSIS_TOKEN \
        | sed -E 's/.*"token":"([^"]+)".*/\1/')
    sed -i "s|^SONAR_TOKEN=.*|SONAR_TOKEN=$TOKEN|" .env
    echo "   token guardado en .env"
fi
echo "Listo"
