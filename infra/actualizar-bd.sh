#!/bin/bash
# Actualiza una base de datos que ya estaba creada (por ejemplo la de la VM) con las tablas de la semana 5
# y los permisos que necesita ms-eventos para escribir en ellas. Se puede ejecutar mas de una vez.
# Uso, desde la carpeta infra y con los contenedores levantados:  ./actualizar-bd.sh
set -euo pipefail
cd "$(dirname "$0")"

docker compose exec -T mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' < ../bd/02_interacciones.sql

docker compose exec -T mysql sh -c 'exec mysql -uroot -p"$MYSQL_ROOT_PASSWORD"' <<'EOSQL'
GRANT INSERT, UPDATE, DELETE ON gamer_eventos.multimedia_evento TO 'app_eventos'@'%';
GRANT INSERT, UPDATE, DELETE ON gamer_eventos.comentarios_evento TO 'app_eventos'@'%';
GRANT INSERT, UPDATE, DELETE ON gamer_eventos.compartidos_evento TO 'app_eventos'@'%';
FLUSH PRIVILEGES;
SHOW GRANTS FOR 'app_eventos'@'%';
EOSQL
