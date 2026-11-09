# Eventos Gamer

Plataforma web que reúne eventos de la comunidad gamer: torneos, LAN parties, convenciones, lanzamientos y encuentros, presenciales u online.

Proyecto de la asignatura Seguridad y Calidad en el Desarrollo de Software (ISY2202).

## Arquitectura

```
Navegador ──► nginx :80 ──► web-eventos :8080  (/eventos)
                              Spring Boot, Spring Security y Thymeleaf
                              │
                              ├──► ms-usuarios :8081 ──► MySQL (gamer_usuarios)
                              │      Login, entrega el token JWT
                              │
                              └──► ms-eventos :8082 ──► MySQL (gamer_eventos)
                                     Eventos, búsqueda y banners, valida el JWT
```

Solo nginx publica un puerto hacia afuera (80). Las API y MySQL quedan en la red interna de Docker.

## Funcionalidades

| Página | Ruta | Acceso |
|---|---|---|
| Inicio con eventos recientes, populares y banners | `/eventos/` | Pública |
| Búsqueda por nombre, videojuego, tipo, país, ciudad, fecha y modalidad | `/eventos/buscar` | Pública |
| Detalle del evento con fotografías | `/eventos/detalle/{id}` | Con sesión |
| Inicio de sesión | `/eventos/login` | Pública |

El sitio no guarda usuarios propios: el inicio de sesión se valida contra la API de `ms-usuarios`, y el token JWT que entrega queda en la sesión de cada usuario para llamar a las rutas privadas de `ms-eventos`.

## API

### ms-usuarios

| Método | Ruta | Acceso | Descripción |
|---|---|---|---|
| POST | `/api/auth/login` | Público | Valida usuario y clave, entrega el token |
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

Las rutas con token piden el encabezado `Authorization: Bearer <token>`.

## Usuarios de prueba

Todos usan la clave `Gamer2026*`.

| Usuario | Nombre | Rol |
|---|---|---|
| `admin` | Administrador Plataforma | ADMIN |
| `vale` | Valentina Mora | USUARIO |
| `mati` | Matías Fuentes | USUARIO |
| `cata` | Catalina Pérez | USUARIO |

## Base de datos

Los scripts están en la carpeta `bd` y Docker los ejecuta en orden la primera vez que crea el contenedor de MySQL:

1. `01_eventos_gamer.sql` crea los esquemas `gamer_usuarios` y `gamer_eventos`, sus tablas y los datos de prueba.
2. `02_permisos.sh` crea un usuario de base de datos por microservicio, solo con permiso de lectura sobre su esquema.

## Cómo ejecutarlo

Se necesita Docker con Docker Compose.

```bash
cd infra
cp .env.example .env
```

Cambiar las claves del archivo `.env` y luego:

```bash
docker compose up -d --build
```

El sitio queda en `http://localhost/eventos` (en la máquina virtual, `http://[IP]/eventos`).

Para borrar todo, incluida la base de datos:

```bash
docker compose down -v
```

## Estructura

```
eventos-gamer
├── bd            Scripts de creación y carga de la base de datos
├── infra         docker-compose, configuración de nginx y variables de ejemplo
├── ms-usuarios   API de login con JWT
├── ms-eventos    API de eventos, búsqueda y banners
└── web-eventos   Sitio web (Thymeleaf)
```
