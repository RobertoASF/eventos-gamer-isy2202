#!/bin/bash
# Crea un usuario de base de datos por microservicio con el minimo de permisos sobre su propio esquema.
# ms-eventos solo puede escribir en las tablas que usan las funcionalidades de la semana 5.
# Las claves llegan como variables de entorno desde docker-compose, no quedan escritas en el repositorio.
set -e

mysql --protocol=socket -uroot -p"${MYSQL_ROOT_PASSWORD}" <<EOSQL
CREATE USER IF NOT EXISTS 'app_usuarios'@'%' IDENTIFIED BY '${DB_CLAVE_USUARIOS}';
GRANT SELECT ON gamer_usuarios.* TO 'app_usuarios'@'%';

CREATE USER IF NOT EXISTS 'app_eventos'@'%' IDENTIFIED BY '${DB_CLAVE_EVENTOS}';
GRANT SELECT ON gamer_eventos.* TO 'app_eventos'@'%';
GRANT INSERT, UPDATE, DELETE ON gamer_eventos.multimedia_evento TO 'app_eventos'@'%';
GRANT INSERT, UPDATE, DELETE ON gamer_eventos.comentarios_evento TO 'app_eventos'@'%';
GRANT INSERT, UPDATE, DELETE ON gamer_eventos.compartidos_evento TO 'app_eventos'@'%';

FLUSH PRIVILEGES;
EOSQL
