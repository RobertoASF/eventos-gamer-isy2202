#!/bin/bash
# Despliega la plataforma en la maquina virtual con HTTPS de Let's Encrypt.
#
# Requisitos: Docker con Compose, puertos 80 y 443 abiertos en el firewall de la VM y el archivo .env listo.
# Como Let's Encrypt entrega certificados para un nombre de dominio, se usa sslip.io, que resuelve
# [IP].sslip.io a la misma IP sin registrar nada. Ejemplo para la IP 20.10.30.40:
#
#   ./desplegar-vm.sh 20.10.30.40
#
# El sitio queda en https://20.10.30.40.sslip.io/eventos y http://20.10.30.40/eventos redirige a esa direccion.
set -euo pipefail
cd "$(dirname "$0")"

IP="${1:?Indica la IP publica de la maquina virtual}"
DOMINIO="$IP.sslip.io"

if [ ! -f .env ]; then
    echo "Falta el archivo .env. Copia .env.example como .env y cambia las claves." >&2
    exit 1
fi

# La URL publica queda guardada en .env para que nginx redirija HTTP hacia ella
grep -v '^URL_HTTPS=' .env > .env.tmp && mv .env.tmp .env
echo "URL_HTTPS=https://$DOMINIO" >> .env

# nginx necesita algun certificado para partir; el primero es temporal y autofirmado
if [ ! -f certificados/fullchain.pem ]; then
    openssl req -x509 -newkey rsa:2048 -nodes -days 1 \
        -keyout certificados/privkey.pem -out certificados/fullchain.pem -subj "/CN=$DOMINIO"
fi

docker compose up -d --build

# Let's Encrypt valida el dominio pidiendo un archivo en http://$DOMINIO/.well-known/acme-challenge/
docker run --rm \
    -v "$(pwd)/certificados/letsencrypt:/etc/letsencrypt" \
    -v eventos-gamer_certbot-web:/var/www/certbot \
    certbot/certbot:v5.9.0 certonly --webroot -w /var/www/certbot \
    -d "$DOMINIO" --agree-tos --register-unsafely-without-email --non-interactive --keep-until-expiring

cp -L "certificados/letsencrypt/live/$DOMINIO/fullchain.pem" certificados/fullchain.pem
cp -L "certificados/letsencrypt/live/$DOMINIO/privkey.pem" certificados/privkey.pem
chmod 600 certificados/privkey.pem
docker compose exec nginx nginx -s reload

echo "Listo: https://$DOMINIO/eventos (http://$IP/eventos redirige a esa direccion)"
