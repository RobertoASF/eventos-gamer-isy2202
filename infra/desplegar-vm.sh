#!/bin/bash
# Despliega la plataforma en la maquina virtual con HTTPS de Let's Encrypt.
#
# Requisitos: Docker con Compose, puertos 80 y 443 abiertos en el firewall de la VM y el archivo .env listo.
# El certificado cubre dos nombres:
#   - la IP publica, para que http://[IP]/eventos funcione aunque el navegador intente HTTPS primero
#   - [IP].sslip.io, un nombre que sslip.io resuelve a la misma IP sin registrar nada
# Ejemplo para la IP 20.10.30.40:
#
#   ./desplegar-vm.sh 20.10.30.40
#
# El sitio queda en https://20.10.30.40/eventos y en https://20.10.30.40.sslip.io/eventos.
# http://20.10.30.40/eventos redirige a HTTPS.
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

# Let's Encrypt valida cada nombre pidiendo un archivo en http://<nombre>/.well-known/acme-challenge/
# Los certificados para IP solo se entregan con el perfil shortlived (validez de 6 dias)
docker run --rm \
    -v "$(pwd)/certificados/letsencrypt:/etc/letsencrypt" \
    -v eventos-gamer_certbot-web:/var/www/certbot \
    certbot/certbot:v5.9.0 certonly --webroot -w /var/www/certbot \
    --cert-name "$DOMINIO" -d "$DOMINIO" --ip-address "$IP" --preferred-profile shortlived \
    --agree-tos --register-unsafely-without-email --non-interactive --keep-until-expiring

chmod +x renovar-certificado.sh
./renovar-certificado.sh

# Renovacion automatica: dos veces al dia y cada vez que la maquina se enciende
RENOVAR="$(pwd)/renovar-certificado.sh >> $HOME/renovacion-certificado.log 2>&1"
( crontab -l 2>/dev/null | grep -v renovar-certificado.sh || true
  echo "17 */12 * * * $RENOVAR"
  echo "@reboot sleep 90 && $RENOVAR" ) | crontab -

echo "Listo: https://$IP/eventos y https://$DOMINIO/eventos (http://$IP/eventos redirige a HTTPS)"
