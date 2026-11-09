#!/bin/bash
# Genera un certificado autofirmado para probar HTTPS en el computador local.
# En la maquina virtual se usa uno de Let's Encrypt (ver desplegar-vm.sh).
set -euo pipefail
export MSYS_NO_PATHCONV=1
cd "$(dirname "$0")"

openssl req -x509 -newkey rsa:2048 -nodes -days 30 \
    -keyout certificados/privkey.pem -out certificados/fullchain.pem \
    -subj "/CN=localhost" \
    -addext "subjectAltName=DNS:localhost,DNS:nginx,IP:127.0.0.1"

echo "Certificado de prueba creado en infra/certificados"
