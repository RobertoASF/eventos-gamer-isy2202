-- Base de datos de la plataforma de eventos gamer (MySQL 8.4)
-- Crea un esquema por microservicio, sus tablas y los datos de prueba.
--   gamer_usuarios -> ms-usuarios
--   gamer_eventos  -> ms-eventos
-- Usuarios de prueba, todos con la clave Gamer2026*: admin, vale, mati, cata, roberto

SET NAMES utf8mb4;

DROP DATABASE IF EXISTS gamer_eventos;
DROP DATABASE IF EXISTS gamer_usuarios;

CREATE DATABASE gamer_usuarios CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;
CREATE DATABASE gamer_eventos CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci;

-- =====================================================================
-- Usuarios
-- =====================================================================
USE gamer_usuarios;

CREATE TABLE roles (
    id          INT          PRIMARY KEY,
    nombre      VARCHAR(20)  NOT NULL UNIQUE,
    descripcion VARCHAR(150) NOT NULL
);

CREATE TABLE usuarios (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    nombre_usuario VARCHAR(30)  NOT NULL UNIQUE,
    nombre         VARCHAR(80)  NOT NULL,
    email          VARCHAR(120) NOT NULL UNIQUE,
    password_hash  VARCHAR(100) NOT NULL,
    rol_id         INT          NOT NULL,
    activo         BOOLEAN      NOT NULL DEFAULT TRUE,
    fecha_creacion DATETIME     NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_usuarios_rol FOREIGN KEY (rol_id) REFERENCES roles (id)
);

INSERT INTO roles (id, nombre, descripcion) VALUES
(1, 'ADMIN', 'Administra la plataforma y los eventos publicados'),
(2, 'USUARIO', 'Gamer registrado, puede ver el detalle de los eventos');

-- Las claves se guardan con BCrypt, nunca en texto plano
INSERT INTO usuarios (nombre_usuario, nombre, email, password_hash, rol_id, fecha_creacion) VALUES
('admin', 'Administrador Plataforma', 'admin@eventosgamer.cl', '$2a$10$AA7wKWU3IaATSwS5vOofau2mnfNQt7hrcg0BmANQFCgMaBDKXyJ.i', 1, '2026-09-28 09:00:00'),
('vale', 'Valentina Mora', 'vale@eventosgamer.cl', '$2a$10$NFxeS4tqXt13VL3QlIVtmuaW1L2IKTBpbEUOeqnHMfUQGgb9zq4zO', 2, '2026-10-02 18:40:00'),
('mati', 'Matías Fuentes', 'mati@eventosgamer.cl', '$2a$10$DBdvY5qk.UwmymNAcxzad./hMUfaXY8HGeRMaZKFMVuuuWR/WIhJG', 2, '2026-10-04 21:15:00'),
('cata', 'Catalina Pérez', 'cata@eventosgamer.cl', '$2a$10$2N6Vx1c8YGiM1OiYcrFDcO.vBPfSah6GGL0NB8CL1QeJVdXEJRJiO', 2, '2026-10-11 12:05:00'),
('roberto', 'Roberto Sánchez', 'roberto@eventosgamer.cl', '$2a$10$4HjXFwcF4ob/Y9qcyjwR0uyTnHMd34LPQuGaejJuZJmYewht2vFya', 2, '2026-11-09 20:58:00');

-- =====================================================================
-- Eventos
-- =====================================================================
USE gamer_eventos;

CREATE TABLE categorias (
    id     BIGINT      PRIMARY KEY,
    nombre VARCHAR(40) NOT NULL UNIQUE
);

CREATE TABLE videojuegos (
    id     BIGINT      PRIMARY KEY,
    nombre VARCHAR(60) NOT NULL UNIQUE,
    genero VARCHAR(40) NOT NULL
);

CREATE TABLE eventos (
    id                BIGINT        AUTO_INCREMENT PRIMARY KEY,
    nombre            VARCHAR(120)  NOT NULL,
    resumen           VARCHAR(250)  NOT NULL,
    descripcion       VARCHAR(2000) NOT NULL,
    videojuego_id     BIGINT        NOT NULL,
    categoria_id      BIGINT        NOT NULL,
    modalidad         VARCHAR(12)   NOT NULL,
    pais              VARCHAR(60)   NOT NULL,
    ciudad            VARCHAR(60)   NOT NULL,
    lugar             VARCHAR(150),
    enlace_acceso     VARCHAR(255),
    fecha_inicio      DATETIME      NOT NULL,
    organizador       VARCHAR(120)  NOT NULL,
    requisitos        VARCHAR(1000) NOT NULL,
    inscripcion       VARCHAR(1000) NOT NULL,
    valor_entrada     INT           NOT NULL DEFAULT 0,
    cupos             INT           NOT NULL,
    interesados       INT           NOT NULL DEFAULT 0,
    imagen_portada    VARCHAR(150)  NOT NULL,
    fecha_publicacion DATETIME      NOT NULL,
    CONSTRAINT fk_eventos_videojuego FOREIGN KEY (videojuego_id) REFERENCES videojuegos (id),
    CONSTRAINT fk_eventos_categoria FOREIGN KEY (categoria_id) REFERENCES categorias (id),
    CONSTRAINT ck_eventos_modalidad CHECK (modalidad IN ('PRESENCIAL', 'ONLINE'))
);

CREATE INDEX idx_eventos_fecha_inicio ON eventos (fecha_inicio);
CREATE INDEX idx_eventos_pais_ciudad ON eventos (pais, ciudad);

CREATE TABLE fotos_evento (
    id          BIGINT       AUTO_INCREMENT PRIMARY KEY,
    evento_id   BIGINT       NOT NULL,
    url         VARCHAR(150) NOT NULL,
    descripcion VARCHAR(120) NOT NULL,
    orden       INT          NOT NULL,
    CONSTRAINT fk_fotos_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE
);

CREATE TABLE banners (
    id      BIGINT       AUTO_INCREMENT PRIMARY KEY,
    marca   VARCHAR(60)  NOT NULL,
    titulo  VARCHAR(80)  NOT NULL,
    mensaje VARCHAR(200) NOT NULL,
    imagen  VARCHAR(150) NOT NULL,
    orden   INT          NOT NULL,
    activo  BOOLEAN      NOT NULL DEFAULT TRUE
);

INSERT INTO categorias (id, nombre) VALUES
(1, 'Torneo'),
(2, 'LAN Party'),
(3, 'Convención'),
(4, 'Lanzamiento'),
(5, 'Encuentro de comunidad'),
(6, 'Clasificatorio');

INSERT INTO videojuegos (id, nombre, genero) VALUES
(1, 'Counter-Strike 2', 'Shooter táctico'),
(2, 'Valorant', 'Shooter táctico'),
(3, 'League of Legends', 'MOBA'),
(4, 'Dota 2', 'MOBA'),
(5, 'EA Sports FC 26', 'Deportes'),
(6, 'Rocket League', 'Deportes'),
(7, 'Street Fighter 6', 'Pelea'),
(8, 'Tekken 8', 'Pelea'),
(9, 'Super Smash Bros. Ultimate', 'Pelea'),
(10, 'Fortnite', 'Battle royale'),
(11, 'Minecraft', 'Mundo abierto'),
(12, 'Clash Royale', 'Estrategia'),
(13, 'Mario Kart 8 Deluxe', 'Carreras'),
(14, 'Varios juegos', 'Multijuego');

INSERT INTO eventos (nombre, resumen, descripcion, videojuego_id, categoria_id, modalidad, pais, ciudad, lugar,
                     enlace_acceso, fecha_inicio, organizador, requisitos, inscripcion, valor_entrada, cupos,
                     interesados, imagen_portada, fecha_publicacion) VALUES
('Copa Valorant Santiago 2026',
 'Torneo 5v5 presencial con fase de grupos el sábado y final en escenario con narración en vivo.',
 'Dos días de competencia para equipos amateur y semiprofesionales. La fase de grupos se juega el sábado en 32 estaciones y los cuatro mejores equipos llegan a la final del domingo en el escenario principal, con transmisión en vivo y narración en español. También habrá zona de práctica libre, food trucks y una tienda de periféricos con descuentos para los asistentes.',
 2, 1, 'PRESENCIAL', 'Chile', 'Santiago', 'Centro de Eventos Arena Sur, Av. Departamental 1450, San Miguel', NULL,
 '2026-11-21 15:00:00', 'Liga Andes Esports',
 'Equipos de 5 jugadores más 1 suplente. Todos deben tener cuenta de Riot con rango Plata o superior y ser mayores de 15 años; los menores de 18 presentan autorización firmada por su apoderado.',
 'Inscripción por equipo hasta el 18 de noviembre en el formulario de la liga. El capitán recibe por correo la confirmación y el horario de su grupo. El público general compra su entrada en boletería.',
 5000, 160, 842, 'img/eventos/evento-01.svg', '2026-10-20 10:00:00'),

('LAN Party Puerto Conectado',
 '48 horas de LAN con torneos rápidos de Counter-Strike 2, desafíos retro y zona de streaming.',
 'LAN party de fin de semana para traer tu propio PC o consola. La organización pone la red de 10 Gbps, la energía y las mesas. Durante el evento se juegan torneos rápidos de Counter-Strike 2 y desafíos de juegos retro, y hay una zona de descanso para quienes se quedan toda la noche. El domingo en la tarde se entregan premios a los ganadores de cada desafío.',
 14, 2, 'PRESENCIAL', 'Chile', 'Valparaíso', 'Galpón Cultural Barón, Av. Errázuriz 2120', NULL,
 '2026-11-28 10:00:00', 'Colectivo LAN Puerto',
 'Traer computador o consola propia, cable de red de al menos 5 metros y alargador. Mayores de 16 años; los menores de 18 necesitan autorización de su apoderado.',
 'Los cupos son por mesa y se reservan pagando la entrada en línea. El ticket incluye una mesa, un punto de red y dos enchufes. No se venden entradas en el lugar.',
 15000, 120, 515, 'img/eventos/evento-02.svg', '2026-10-28 18:30:00'),

('Liga Online EA Sports FC 26 - Fecha de noviembre',
 'Torneo online 1v1 en PlayStation 5 con llaves de eliminación directa y premios en tarjetas de regalo.',
 'Fecha mensual de la liga online de EA Sports FC 26 en modo Ultimate Team. Se juega por llaves de eliminación directa al mejor de tres partidos. Los resultados se reportan con captura de pantalla en el servidor de Discord de la liga y un equipo de árbitros resuelve cualquier disputa. Los tres primeros lugares suman puntos para el ranking anual.',
 5, 1, 'ONLINE', 'Chile', 'Santiago', NULL, 'https://discord.gg/ligafc26cl',
 '2026-11-15 19:00:00', 'Comunidad FC Chile',
 'PlayStation 5 con PS Plus activo, copia de EA Sports FC 26 y cuenta de Discord. Se recomienda conexión por cable.',
 'Inscripción gratuita en el canal de inscripciones del Discord hasta las 18:00 del día del torneo. Las llaves se publican a las 18:30.',
 0, 128, 1290, 'img/eventos/evento-03.svg', '2026-11-02 12:00:00'),

('Batalla de Barrio Street Fighter 6',
 'Torneo de pelea presencial con formato de doble eliminación, top 8 en escenario y side events.',
 'El torneo de la escena de pelea penquista llega a su quinta edición. Se juega con formato de doble eliminación al mejor de tres y el top 8 al mejor de cinco. Las estaciones usan monitores de baja latencia y se permite usar control, stick arcade o hitbox propio. Después del torneo principal hay side events de otros juegos de pelea.',
 7, 1, 'PRESENCIAL', 'Chile', 'Concepción', 'Bar Temático Combo Final, Calle Barros Arana 980', NULL,
 '2026-12-05 12:00:00', 'FGC Biobío',
 'Traer control propio compatible con PlayStation 5. Mayores de 14 años.',
 'Inscripción en línea hasta el 3 de diciembre, o en el lugar hasta las 11:30 si quedan cupos disponibles.',
 3000, 96, 377, 'img/eventos/evento-04.svg', '2026-10-15 09:00:00'),

('Convención Gamer del Sur 2027',
 'Tres días de convención con zona indie, cosplay, consolas retro y torneos abiertos.',
 'La convención más grande del sur de Chile reúne a desarrolladores independientes, tiendas, clubes de cosplay y comunidades de jugadores. Habrá charlas sobre desarrollo de videojuegos, una zona retro con consolas de los años 90, concurso de cosplay y torneos abiertos de varios títulos durante los tres días.',
 14, 3, 'PRESENCIAL', 'Chile', 'Temuco', 'Recinto Ferial Ñielol, Av. Pablo Neruda 01200', NULL,
 '2027-01-15 11:00:00', 'Productora Araucanía Play',
 'Entrada general para todo público. Los menores de 12 años entran gratis acompañados de un adulto.',
 'Entradas a la venta en línea y en la boletería del recinto. Cada torneo tiene inscripción aparte en el stand de la comunidad que lo organiza.',
 8000, 3000, 960, 'img/eventos/evento-05.svg', '2026-10-05 10:00:00'),

('Clasificatorio Andino de League of Legends',
 'Clasificatorio online para el circuito regional, con fase suiza y playoffs al mejor de tres.',
 'Clasificatorio abierto para equipos de Chile, Perú y Bolivia. La fase suiza se juega en tres jornadas online y los ocho mejores equipos avanzan a playoffs al mejor de tres. Los dos finalistas obtienen un cupo en el circuito regional del próximo año. Todas las partidas de playoffs se transmiten con narración en español.',
 3, 6, 'ONLINE', 'Chile', 'Santiago', NULL, 'https://www.twitch.tv/clasificatorioandino',
 '2026-11-29 16:00:00', 'Circuito Andino de Esports',
 'Equipos de 5 titulares y hasta 2 suplentes, con cuentas del servidor LAS en nivel 30 o superior. El capitán debe ser mayor de 18 años.',
 'Registro del equipo en la plataforma del circuito hasta el 25 de noviembre. Se pide el nombre de invocador de cada integrante.',
 0, 64, 1530, 'img/eventos/evento-06.svg', '2026-10-30 15:00:00'),

('Smash Mensual Lima - Edición diciembre',
 'Torneo presencial de Smash Ultimate que suma puntos para el ranking mensual de la comunidad.',
 'Torneo mensual de la comunidad limeña de Super Smash Bros. Ultimate. Se juega con llaves de doble eliminación, reglas estándar de la escena competitiva y premios para el top 3. Antes del torneo hay una hora de juego libre para calentar y conocer a otros jugadores.',
 9, 1, 'PRESENCIAL', 'Perú', 'Lima', 'Local Comunal Smash Perú, Av. Arequipa 3560, Miraflores', NULL,
 '2026-12-12 14:00:00', 'Smash Perú Comunidad',
 'Traer control propio (Pro Controller, control de GameCube o Joy-Con). Se agradece traer una Nintendo Switch para sumar estaciones.',
 'Pre-registro gratuito en línea hasta el 10 de diciembre. El registro se confirma en el lugar hasta las 13:45.',
 0, 64, 288, 'img/eventos/evento-07.svg', '2026-11-04 20:00:00'),

('Copa Primavera Rocket League 3v3',
 'Torneo online 3v3 multiplataforma con fase de grupos y final transmitida con narración.',
 'Copa online abierta a jugadores de Sudamérica. Los equipos se dividen en grupos de cuatro y los dos primeros de cada grupo pasan a playoffs. La final se transmite con narradores y repetición de las mejores jugadas. Se juega en servidores de Sudamérica para mantener una latencia pareja.',
 6, 1, 'ONLINE', 'Argentina', 'Buenos Aires', NULL, 'https://www.twitch.tv/copaprimaverarl',
 '2026-11-22 17:00:00', 'Rocket Sur Comunidad',
 'Equipos de 3 jugadores más 1 suplente, de cualquier plataforma, con rango Platino o superior en 3v3.',
 'Registro por equipo en el formulario de la copa hasta el 20 de noviembre. El check-in es obligatorio 30 minutos antes del inicio.',
 0, 48, 640, 'img/eventos/evento-08.svg', '2026-10-24 11:00:00'),

('Noche de Lanzamiento Tekken 8 Temporada 3',
 'Juego libre de la nueva temporada, exhibiciones de jugadores locales y sorteos de periféricos.',
 'Celebramos la nueva temporada de Tekken 8 con una noche de juego libre en 20 estaciones, exhibiciones de jugadores reconocidos de la escena local y un torneo relámpago. Habrá sorteos de periféricos entre los asistentes y descuentos en la tienda durante el evento.',
 8, 4, 'PRESENCIAL', 'Chile', 'Santiago', 'Tienda Nivel Final, Av. Providencia 2251, Providencia', NULL,
 '2026-12-03 19:30:00', 'Tienda Nivel Final',
 'Evento para mayores de 13 años. No es necesario traer consola ni control.',
 'Entrada liberada con registro previo en línea. El torneo relámpago tiene inscripción en el lugar hasta completar 32 jugadores.',
 0, 150, 410, 'img/eventos/evento-09.svg', '2026-11-06 17:00:00'),

('Encuentro de Constructores Minecraft',
 'Jornada online de construcción colaborativa con talleres de redstone y concurso por equipos.',
 'Servidor especial con un mapa preparado para construir una ciudad entre todos. Durante la jornada hay talleres de redstone para principiantes, un concurso de construcción por equipos y una presentación final en vivo donde cada equipo muestra su proyecto.',
 11, 5, 'ONLINE', 'México', 'Ciudad de México', NULL, 'https://discord.gg/constructoresmc',
 '2026-12-19 12:00:00', 'Comunidad Bloques LATAM',
 'Minecraft Java Edition 1.21 o superior y cuenta de Discord para coordinar con el equipo. Apto para todo público.',
 'Inscripción gratuita en el formulario del Discord. Los equipos se arman al comenzar la jornada.',
 0, 200, 720, 'img/eventos/evento-10.svg', '2026-11-01 09:30:00'),

('Final Liga Universitaria de Dota 2',
 'Final presencial entre las cuatro mejores universidades de la liga, con transmisión en vivo.',
 'Después de tres meses de liga online, los cuatro mejores equipos universitarios se enfrentan de forma presencial. Las semifinales se juegan al mejor de tres y la gran final al mejor de cinco. El público puede seguir las partidas en pantalla gigante y participar en trivias entre mapas.',
 4, 1, 'PRESENCIAL', 'Colombia', 'Bogotá', 'Auditorio Mayor, Calle 26 # 68-40', NULL,
 '2027-01-23 10:00:00', 'Liga Universitaria de Esports Colombia',
 'Abierto al público. Los jugadores de cada equipo deben acreditar matrícula vigente.',
 'Entrada gratuita con registro en línea. Los cupos de público están limitados por el aforo del auditorio.',
 0, 400, 505, 'img/eventos/evento-11.svg', '2026-10-12 14:00:00'),

('Fortnite Duos Cup Antofagasta',
 'Torneo presencial de dúos en estaciones de PC con premios para el top 3 y el jugador con más eliminaciones.',
 'Torneo de dúos con 25 estaciones de PC instaladas en red local. Se juegan seis partidas con puntaje por posición y por eliminaciones. Al final se premia a los tres mejores dúos y al jugador con más eliminaciones de la jornada.',
 10, 1, 'PRESENCIAL', 'Chile', 'Antofagasta', 'Centro Juvenil Costanera, Av. Grecia 1650', NULL,
 '2026-12-14 15:00:00', 'Antofagasta Gaming Club',
 'Dúos de jugadores mayores de 12 años; los menores de 18 necesitan autorización. Cuenta de Epic Games con autenticación de dos factores activa.',
 'Inscripción por dúo hasta el 11 de diciembre. La estación asignada se confirma por correo.',
 4000, 50, 830, 'img/eventos/evento-12.svg', '2026-10-26 16:00:00'),

('CS2 Open Viña del Mar',
 'Torneo abierto de Counter-Strike 2 con fase de grupos y final en escenario frente al mar.',
 'Torneo abierto para equipos de todo Chile. La fase de grupos se juega al mejor de uno y los playoffs al mejor de tres. La final se juega en escenario con pantalla gigante y se usan los mapas del pool competitivo vigente.',
 1, 1, 'PRESENCIAL', 'Chile', 'Viña del Mar', 'Centro de Convenciones Borde Costero, Av. San Martín 199', NULL,
 '2027-02-06 10:00:00', 'Liga Costa Esports',
 'Equipos de 5 jugadores con cuenta de Steam sin bloqueos VAC. Mayores de 16 años.',
 'Registro del equipo y pago de la inscripción hasta el 31 de enero. El público compra su entrada en línea.',
 6000, 160, 690, 'img/eventos/evento-13.svg', '2026-11-05 10:00:00'),

('Mario Kart Navideño en Familia',
 'Jornada familiar con carreras por categoría de edad, carreras en equipo y premios para todos.',
 'Encuentro pensado para jugar en familia. Hay categorías infantil, juvenil y adulto, carreras en equipo de adulto con niño y una zona de juegos de mesa para quienes esperan su turno. Todos los participantes se llevan un recuerdo del evento.',
 13, 5, 'PRESENCIAL', 'Chile', 'La Serena', 'Centro Cultural Las Compañías, Av. Los Perales 1800', NULL,
 '2026-12-20 11:00:00', 'Club Gamer Elqui',
 'Apto para todo público. Los menores de 10 años deben venir con un adulto.',
 'Inscripción gratuita en el lugar desde las 10:30, por orden de llegada.',
 0, 80, 233, 'img/eventos/evento-14.svg', '2026-11-07 13:00:00'),

('Desafío Express Clash Royale',
 'Torneo online relámpago de dos horas con modo triple elixir y final transmitida.',
 'Torneo de dos horas dentro del juego con modo triple elixir. La clasificación se define por victorias y los ocho mejores juegan una final que se transmite en vivo con comentarios de jugadores de la comunidad.',
 12, 1, 'ONLINE', 'Chile', 'Santiago', NULL, 'https://www.youtube.com/@desafioexpresscr',
 '2026-11-18 21:00:00', 'Comunidad Royale Chile',
 'Nivel de rey 11 o superior y cuenta de Supercell ID.',
 'El código del torneo se publica en el Discord de la comunidad 15 minutos antes del inicio.',
 0, 1000, 1105, 'img/eventos/evento-15.svg', '2026-11-08 09:00:00'),

('Expo Indie Dev & Play 2027',
 'Feria de videojuegos independientes chilenos con demos jugables, charlas y revisión de proyectos.',
 'Más de 40 estudios independientes muestran sus demos jugables. El programa incluye charlas sobre diseño, financiamiento y publicación de videojuegos, y una zona de revisión donde los desarrolladores reciben comentarios del público y de mentores de la industria.',
 14, 3, 'PRESENCIAL', 'Chile', 'Santiago', 'Centro Cultural Ribera Norte, Av. Matucana 640', NULL,
 '2027-03-13 10:00:00', 'Asociación de Desarrolladores Indie',
 'Abierto a todo público. Los estudios que quieran exponer deben postular con una demo jugable.',
 'Entrada general en línea. La postulación de estudios cierra el 15 de enero.',
 5000, 2000, 1020, 'img/eventos/evento-16.svg', '2026-10-01 08:00:00');

INSERT INTO fotos_evento (evento_id, url, descripcion, orden) VALUES
(1, 'img/eventos/evento-01-1.svg', 'Escenario principal de la final', 1),
(1, 'img/eventos/evento-01-2.svg', 'Zona de estaciones de la fase de grupos', 2),
(1, 'img/eventos/evento-01-3.svg', 'Premiación de la edición anterior', 3),
(2, 'img/eventos/evento-02-1.svg', 'Mesas de juego durante la noche', 1),
(2, 'img/eventos/evento-02-2.svg', 'Torneo rápido de Counter-Strike 2', 2),
(2, 'img/eventos/evento-02-3.svg', 'Zona de juegos retro', 3),
(3, 'img/eventos/evento-03-1.svg', 'Transmisión de la final del mes pasado', 1),
(3, 'img/eventos/evento-03-2.svg', 'Llaves del torneo', 2),
(3, 'img/eventos/evento-03-3.svg', 'Ganadores de la fecha anterior', 3),
(4, 'img/eventos/evento-04-1.svg', 'Estaciones con monitores de baja latencia', 1),
(4, 'img/eventos/evento-04-2.svg', 'Top 8 en escenario', 2),
(4, 'img/eventos/evento-04-3.svg', 'Público siguiendo la gran final', 3),
(5, 'img/eventos/evento-05-1.svg', 'Pabellón principal de la convención', 1),
(5, 'img/eventos/evento-05-2.svg', 'Concurso de cosplay', 2),
(5, 'img/eventos/evento-05-3.svg', 'Zona retro', 3),
(6, 'img/eventos/evento-06-1.svg', 'Mesa de narradores', 1),
(6, 'img/eventos/evento-06-2.svg', 'Partida de playoffs', 2),
(6, 'img/eventos/evento-06-3.svg', 'Equipos clasificados de la edición anterior', 3),
(7, 'img/eventos/evento-07-1.svg', 'Juego libre antes del torneo', 1),
(7, 'img/eventos/evento-07-2.svg', 'Llaves de doble eliminación', 2),
(7, 'img/eventos/evento-07-3.svg', 'Podio del mes anterior', 3),
(8, 'img/eventos/evento-08-1.svg', 'Transmisión de la final', 1),
(8, 'img/eventos/evento-08-2.svg', 'Repetición de la mejor jugada', 2),
(8, 'img/eventos/evento-08-3.svg', 'Equipo campeón de la copa de otoño', 3),
(9, 'img/eventos/evento-09-1.svg', 'Estaciones de juego libre', 1),
(9, 'img/eventos/evento-09-2.svg', 'Exhibición de jugadores locales', 2),
(9, 'img/eventos/evento-09-3.svg', 'Sorteo de periféricos', 3),
(10, 'img/eventos/evento-10-1.svg', 'Ciudad construida en la jornada anterior', 1),
(10, 'img/eventos/evento-10-2.svg', 'Taller de redstone', 2),
(10, 'img/eventos/evento-10-3.svg', 'Presentación de proyectos', 3),
(11, 'img/eventos/evento-11-1.svg', 'Auditorio preparado para la final', 1),
(11, 'img/eventos/evento-11-2.svg', 'Equipos en semifinales', 2),
(11, 'img/eventos/evento-11-3.svg', 'Premiación de la liga', 3),
(12, 'img/eventos/evento-12-1.svg', 'Estaciones de PC en red local', 1),
(12, 'img/eventos/evento-12-2.svg', 'Dúos en partida', 2),
(12, 'img/eventos/evento-12-3.svg', 'Entrega de premios', 3),
(13, 'img/eventos/evento-13-1.svg', 'Escenario con pantalla gigante', 1),
(13, 'img/eventos/evento-13-2.svg', 'Fase de grupos', 2),
(13, 'img/eventos/evento-13-3.svg', 'Público en la final', 3),
(14, 'img/eventos/evento-14-1.svg', 'Carreras de la categoría infantil', 1),
(14, 'img/eventos/evento-14-2.svg', 'Zona de juegos de mesa', 2),
(14, 'img/eventos/evento-14-3.svg', 'Premiación familiar', 3),
(15, 'img/eventos/evento-15-1.svg', 'Transmisión de la final', 1),
(15, 'img/eventos/evento-15-2.svg', 'Tabla de clasificación', 2),
(15, 'img/eventos/evento-15-3.svg', 'Comentaristas de la comunidad', 3),
(16, 'img/eventos/evento-16-1.svg', 'Stands de estudios independientes', 1),
(16, 'img/eventos/evento-16-2.svg', 'Charla sobre publicación de juegos', 2),
(16, 'img/eventos/evento-16-3.svg', 'Zona de demos jugables', 3);

INSERT INTO banners (marca, titulo, mensaje, imagen, orden) VALUES
('HyperByte Periféricos', 'Teclados mecánicos con 20% de descuento', 'Usa el código EVENTOS20 en tu primera compra de teclados y mouse.', 'img/banners/banner-1.svg', 1),
('NovaNet Fibra', 'Fibra de 1 Gbps para jugar sin lag', 'Instalación gratis para la comunidad gamer durante noviembre.', 'img/banners/banner-2.svg', 2),
('Pixel Energy', 'La bebida oficial de las LAN', 'Encuéntrala en los stands de los eventos patrocinados.', 'img/banners/banner-3.svg', 3),
('Arena Seat', 'Sillas ergonómicas para sesiones largas', 'Hasta 6 cuotas sin interés en todos los modelos.', 'img/banners/banner-4.svg', 4);
