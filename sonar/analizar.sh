#!/bin/bash
# Analiza con SonarQube el frontend (web-eventos) y el backend (ms-usuarios y ms-eventos).
# Cada modulo se compila, se ejecutan sus pruebas (con cobertura de JaCoCo) y se envia el resultado a SonarQube.
# La salida de Maven y un resumen de los hallazgos quedan en sonar/reportes/<carpeta>.
# Uso: ./analizar.sh antes      (o despues, o el nombre que se quiera para la carpeta)
set -uo pipefail
cd "$(dirname "$0")"
source .env

CARPETA="reportes/${1:-ultimo}"
mkdir -p "$CARPETA"

for modulo in web-eventos ms-usuarios ms-eventos; do
    echo "== $modulo =="
    if ! (cd "../$modulo" && mvn -B clean verify sonar:sonar -Dsonar.token="$SONAR_TOKEN" \
            -Dsonar.qualitygate.wait=true) > "$CARPETA/$modulo.log" 2>&1; then
        echo "   El Quality Gate no se aprobo o el analisis fallo (ver $CARPETA/$modulo.log)"
    fi
    grep -E "Tests run:.*Fail|ANALYSIS SUCCESSFUL|QUALITY GATE STATUS|BUILD (SUCCESS|FAILURE)" "$CARPETA/$modulo.log" \
        | grep -v "in cl\." | sed 's/^/   /'
done

python resumen.py "$CARPETA"
