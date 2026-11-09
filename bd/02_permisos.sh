#!/bin/bash
# Crea un usuario de base de datos por microservicio, solo con permiso de lectura sobre su propio esquema.
# Las claves llegan como variables de entorno desde docker-compose, no quedan escritas en el repositorio.
set -e

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<EOSQL
CREATE USER IF NOT EXISTS 'app_usuarios'@'%' IDENTIFIED BY '${DB_CLAVE_USUARIOS}';
GRANT SELECT ON gamer_usuarios.* TO 'app_usuarios'@'%';

CREATE USER IF NOT EXISTS 'app_eventos'@'%' IDENTIFIED BY '${DB_CLAVE_EVENTOS}';
GRANT SELECT ON gamer_eventos.* TO 'app_eventos'@'%';

FLUSH PRIVILEGES;
EOSQL
