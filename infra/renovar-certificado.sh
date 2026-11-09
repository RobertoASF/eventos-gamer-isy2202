#!/bin/bash
# Renueva el certificado de Let's Encrypt cuando le queda poco, lo copia para nginx y recarga nginx.
# desplegar-vm.sh lo deja programado en cron dos veces al dia y al encender la maquina, porque el
# certificado para la IP dura solo 6 dias (perfil shortlived de Let's Encrypt).
set -euo pipefail
cd "$(dirname "$0")"

DOMINIO=$(grep '^URL_HTTPS=' .env | sed 's#^URL_HTTPS=https://##')

docker run --rm \
    -v "$(pwd)/certificados/letsencrypt:/etc/letsencrypt" \
    -v eventos-gamer_certbot-web:/var/www/certbot \
    certbot/certbot:v5.9.0 renew --webroot -w /var/www/certbot --quiet

# certbot deja los archivos como root, por eso la copia se hace dentro de un contenedor y luego se devuelven al usuario
docker run --rm -v "$(pwd)/certificados:/certificados" --entrypoint sh certbot/certbot:v5.9.0 -c "
    cp -L /certificados/letsencrypt/live/$DOMINIO/fullchain.pem /certificados/fullchain.pem &&
    cp -L /certificados/letsencrypt/live/$DOMINIO/privkey.pem /certificados/privkey.pem &&
    chown $(id -u):$(id -g) /certificados/fullchain.pem /certificados/privkey.pem &&
    chmod 600 /certificados/privkey.pem"

docker compose exec -T nginx nginx -s reload
echo "$(date -u +%FT%TZ) certificado revisado para $DOMINIO"
