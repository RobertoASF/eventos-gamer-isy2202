# Eventos Gamer

Plataforma web que reúne eventos de la comunidad gamer: torneos, LAN parties, convenciones, lanzamientos y encuentros, presenciales u online.

Proyecto de la asignatura Seguridad y Calidad en el Desarrollo de Software (ISY2202).

## Arquitectura

```
Navegador ──► nginx :80 / :443 ──► web-eventos :8080  (/eventos)
              HTTP redirige          Spring Boot, Spring Security y Thymeleaf
              a HTTPS                │
                                     ├──► ms-usuarios :8081 ──► MySQL (gamer_usuarios)
                                     │      Login, entrega el token JWT
                                     │
                                     └──► ms-eventos :8082 ──► MySQL (gamer_eventos)
                                            Eventos, búsqueda y banners, valida el JWT
```

Solo nginx publica puertos hacia afuera (80 y 443). Las API y MySQL quedan en la red interna de Docker.

## Funcionalidades

| Página | Ruta | Acceso |
|---|---|---|
| Inicio con eventos recientes, populares y banners | `/eventos/` | Pública |
| Búsqueda por nombre, videojuego, tipo, país, ciudad, fecha y modalidad | `/eventos/buscar` | Pública |
| Detalle del evento con fotografías | `/eventos/detalle/{id}` | Con sesión |
| Fotos y videos de la comunidad (subir, ver y eliminar los propios) | `/eventos/detalle/{id}#multimedia` | Con sesión |
| Comentar y valorar eventos (1 a 5 estrellas) | `/eventos/detalle/{id}#opiniones` | Con sesión |
| Compartir en el sitio y en redes sociales | `/eventos/detalle/{id}#compartir` | Con sesión |
| Eventos compartidos por la comunidad | `/eventos/comunidad` | Con sesión |
| Inicio de sesión | `/eventos/login` | Pública |

El sitio no guarda usuarios propios: el inicio de sesión se valida contra la API de `ms-usuarios`, y el token JWT que entrega queda en la sesión de cada usuario para llamar a las rutas privadas de `ms-eventos`.

## API

### ms-usuarios

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/auth/csrf` | Público | Entrega el token CSRF que pide el login (cookie `XSRF-TOKEN` y encabezado `X-XSRF-TOKEN`) |
| POST | `/api/auth/login` | Público, con token CSRF | Valida usuario y clave, entrega el token |
| GET | `/api/usuarios/perfil` | Con token | Datos del usuario del token |

### ms-eventos

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| GET | `/api/eventos/recientes?limite=` | Público | Próximos eventos, del publicado más nuevo al más antiguo |
| GET | `/api/eventos/populares?limite=` | Público | Próximos eventos con más interesados |
| GET | `/api/eventos/buscar?nombre=&videojuegoId=&categoriaId=&pais=&ciudad=&modalidad=&desde=&hasta=&pagina=&tamano=` | Público | Búsqueda con filtros opcionales |
| GET | `/api/eventos/filtros` | Público | Opciones para el formulario de búsqueda |
| GET | `/api/banners` | Público | Avisos comerciales activos |
| GET | `/api/eventos/{id}` | Con token | Detalle completo con fotografías |
| GET, POST | `/api/eventos/{id}/multimedia` | Con token | Lista o sube una foto (JPEG/PNG, 5 MB) o un video (MP4/WebM, 7 MB) |
| GET | `/api/eventos/{id}/multimedia/{mid}/contenido` | Con token | Archivo de la foto o del video (admite `Range`) |
| DELETE | `/api/eventos/{id}/multimedia/{mid}` | Con token | Solo el autor o un ADMIN |
| GET, POST | `/api/eventos/{id}/comentarios` | Con token | Opiniones y promedio; una opinión por usuario |
| DELETE | `/api/eventos/{id}/comentarios/{cid}` | Con token | Solo el autor o un ADMIN |
| GET, POST | `/api/eventos/{id}/compartidos` | Con token | Resumen o registro de un compartido (SITIO, FACEBOOK, X, WHATSAPP, TELEGRAM, LINKEDIN) |
| GET | `/api/compartidos?limite=` | Con token | Lo compartido en el sitio, para la página Comunidad |

Las rutas con token piden el encabezado `Authorization: Bearer <token>`. La definición OpenAPI de cada API está en `zap/api`.

## Usuarios de prueba

Todos usan la clave `Gamer2026*`.

| Usuario | Nombre | Rol |
|---|---|---|
| `admin` | Administrador Plataforma | ADMIN |
| `vale` | Valentina Mora | USUARIO |
| `mati` | Matías Fuentes | USUARIO |
| `cata` | Catalina Pérez | USUARIO |
| `roberto` | Roberto Sánchez | USUARIO |

Después de 5 intentos fallidos seguidos, la cuenta queda bloqueada por 15 minutos.

## Base de datos

Los scripts están en la carpeta `bd` y Docker los ejecuta en orden la primera vez que crea el contenedor de MySQL:

1. `01_eventos_gamer.sql` crea los esquemas `gamer_usuarios` y `gamer_eventos`, sus tablas y los datos de prueba.
2. `02_interacciones.sql` crea las tablas de la semana 5 (`multimedia_evento`, `comentarios_evento` y `compartidos_evento`) con datos de prueba.
3. `03_permisos.sh` crea un usuario de base de datos por microservicio: `app_usuarios` solo lee; `app_eventos` lee su esquema y solo puede escribir en las tres tablas de la semana 5.

Para una base que ya existía (por ejemplo la de la VM) se ejecuta `infra/actualizar-bd.sh` con los contenedores levantados.

## Cómo ejecutarlo en el computador

Se necesita Docker con Docker Compose.

```bash
cd infra
cp .env.example .env
./certificado-local.sh
```

Cambiar las claves del archivo `.env` y luego:

```bash
docker compose up -d --build
```

El sitio queda en `https://localhost/eventos` (el navegador avisará que el certificado de prueba es autofirmado). Si el puerto 443 está ocupado, en `.env` se puede usar `PUERTO_HTTPS=8443` y `URL_HTTPS=https://localhost:8443`.

Para borrar todo, incluida la base de datos:

```bash
docker compose down -v
```

## Despliegue en la máquina virtual

Desplegado en AWS (EC2 t3.medium, Ubuntu Server 26.04 LTS, 4 GB de RAM, 20 GB de disco) con IP elástica, para que la dirección no cambie al detener y volver a iniciar la instancia.

1. Abrir los puertos 80 y 443 en el grupo de seguridad (y el 22 solo desde la IP de quien administra).
2. Instalar Docker y copiar el proyecto:

```bash
sudo apt-get install -y docker.io docker-compose-v2 docker-buildx unzip
sudo usermod -aG docker $USER
```

3. En `infra`, copiar `.env.example` como `.env` y cambiar las claves.
4. Ejecutar con la IP pública de la VM:

```bash
./desplegar-vm.sh 20.10.30.40
```

El script levanta los contenedores y obtiene un certificado de Let's Encrypt que cubre la IP y el nombre `[IP].sslip.io` (sslip.io resuelve ese nombre a la misma IP). El sitio queda en `https://[IP]/eventos`, y `http://[IP]/eventos` redirige a HTTPS.

Let's Encrypt entrega los certificados para IP con una validez de 6 días, por eso el script deja programado `renovar-certificado.sh` en cron, dos veces al día y cada vez que la máquina se enciende. Los contenedores también vuelven a levantarse solos al encender la VM.

## Seguridad

Resumen de las medidas, ordenadas según el OWASP Top 10 2021:

| Categoría | Medidas |
|---|---|
| A01 Control de acceso | Rutas públicas y privadas en el sitio y en la API, token por sesión, CSRF activo en el sitio y en las API (doble envío en el login), cookie SameSite=Strict, solo el autor o un ADMIN elimina fotos y comentarios, usuarios de BD con el mínimo de permisos |
| A02 Fallas criptográficas | BCrypt, HTTPS con TLS 1.2/1.3, HSTS, cookie Secure, secretos fuera del repositorio |
| A03 Inyección | JPA Specification con parámetros, validación de filtros, Thymeleaf escapa la salida |
| A04 Diseño inseguro | Límites de paginación y de largo, bloqueo por intentos fallidos, cuerpo máximo de 64 KB (9 MB solo en la ruta de subida), tipo de archivo por su firma, fotos recodificadas sin EXIF, máximo de archivos por usuario |
| A05 Configuración | CSP y cabeceras de seguridad, versión de nginx oculta, actuator en puerto interno, errores sin detalles, contenedores sin privilegios |
| A06 Componentes | Versiones actuales y fijas (Spring Boot 4.1.2, nginx 1.30.6, MySQL 8.4.13, Temurin 21.0.13), revisión con Trivy |
| A07 Autenticación | Bloqueo temporal, mensaje genérico, cambio de id de sesión, expiración de sesión y de token |
| A08 Integridad | Firma y emisor del JWT validados, algoritmo fijo |
| A09 Registro | Registro de logins correctos, fallidos y bloqueados con usuario e IP, sin claves ni tokens |
| A10 SSRF | Las URL de las API vienen de la configuración, nunca del usuario |

## Análisis estático con SonarQube

La carpeta `sonar` tiene la instalación (SonarQube Community Build 26.11 con PostgreSQL), la configuración y el análisis:

```bash
cd sonar
cp .env.example .env          # cambiar la clave de la base de datos
docker compose up -d          # interfaz en http://localhost:9100
# cambiar la clave de admin en el primer ingreso y guardarla en .env como SONAR_ADMIN_CLAVE
./configurar.sh               # modo Standard Experience, perfiles, Quality Gate, proyectos y token
./analizar.sh despues         # compila, prueba y analiza web-eventos, ms-usuarios y ms-eventos
```

Cada análisis deja la salida de Maven y un resumen de los hallazgos en `sonar/reportes/<carpeta>`:

| Carpeta | Ejecución |
|---|---|
| `antes` | Primer análisis: 7 vulnerabilidades (3 Critical), 9 bugs y 1 code smell |
| `despues` | Después de las correcciones: Quality Gate aprobado, sin vulnerabilidades, bugs ni code smells |

## Análisis con OWASP ZAP

La carpeta `zap` tiene los planes del Automation Framework, las definiciones OpenAPI y el script para ejecutarlos con la imagen oficial `zaproxy/zap-stable`:

```bash
cd zap
OBJETIVO=https://nginx ZAP_CLAVE='Gamer2026*' ./escanear.sh reportes/despues
```

Los reportes de cada ejecución quedan en `zap/reportes`:

| Carpeta | Ejecución |
|---|---|
| `antes` | Versión de la semana 2, antes de las correcciones |
| `verificacion-1` | Primera verificación después de las correcciones |
| `despues` | Verificación final |
| `vm` | Sitio desplegado en AWS, escaneado desde fuera de la VM |

## Pruebas

```bash
cd ms-usuarios
mvn test
```

## Estructura

```
eventos-gamer
├── bd            Scripts de creación y carga de la base de datos
├── infra         docker-compose, nginx, certificados y script de despliegue
├── ms-usuarios   API de login con JWT
├── ms-eventos    API de eventos, búsqueda y banners
├── sonar         Instalación, configuración y reportes de SonarQube
├── web-eventos   Sitio web (Thymeleaf)
└── zap           Planes, definiciones OpenAPI y reportes de OWASP ZAP
```
