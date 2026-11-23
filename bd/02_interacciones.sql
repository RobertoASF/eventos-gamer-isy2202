-- Semana 5: multimedia, comentarios con valoracion y eventos compartidos (esquema gamer_eventos)
-- En una instalacion nueva Docker lo ejecuta despues de 01_eventos_gamer.sql.
-- En una base que ya existe se puede ejecutar a mano: crea solo lo que falta y no duplica los datos.

SET NAMES utf8mb4;
USE gamer_eventos;

-- Fotos y videos que suben los usuarios. El archivo se guarda en la base de datos y nunca con el
-- nombre que traia el usuario, asi no hay rutas ni nombres controlados desde afuera
CREATE TABLE IF NOT EXISTS multimedia_evento (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    evento_id      BIGINT       NOT NULL,
    usuario_id     BIGINT       NOT NULL,
    usuario        VARCHAR(30)  NOT NULL,
    autor          VARCHAR(80)  NOT NULL,
    tipo           VARCHAR(5)   NOT NULL,
    tipo_contenido VARCHAR(30)  NOT NULL,
    tamano         INT          NOT NULL,
    descripcion    VARCHAR(120) NOT NULL,
    contenido      LONGBLOB     NOT NULL,
    fecha_creacion DATETIME     NOT NULL,
    CONSTRAINT fk_multimedia_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT ck_multimedia_tipo CHECK (tipo IN ('FOTO', 'VIDEO')),
    CONSTRAINT ck_multimedia_contenido CHECK (tipo_contenido IN ('image/jpeg', 'image/png', 'video/mp4', 'video/webm')),
    CONSTRAINT ck_multimedia_tamano CHECK (tamano > 0 AND tamano <= 26214400),
    INDEX idx_multimedia_evento (evento_id, fecha_creacion)
);

-- Una opinion por usuario y evento: puntuacion de 1 a 5 estrellas y un comentario
CREATE TABLE IF NOT EXISTS comentarios_evento (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    evento_id      BIGINT       NOT NULL,
    usuario_id     BIGINT       NOT NULL,
    usuario        VARCHAR(30)  NOT NULL,
    autor          VARCHAR(80)  NOT NULL,
    puntuacion     INT          NOT NULL,
    texto          VARCHAR(500) NOT NULL,
    fecha_creacion DATETIME     NOT NULL,
    fecha_edicion  DATETIME,
    CONSTRAINT fk_comentarios_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT ck_comentarios_puntuacion CHECK (puntuacion BETWEEN 1 AND 5),
    CONSTRAINT uq_comentarios_usuario UNIQUE (evento_id, usuario_id)
);

-- Veces que un usuario compartio un evento, en el sitio (con mensaje) o en una red social
CREATE TABLE IF NOT EXISTS compartidos_evento (
    id             BIGINT       AUTO_INCREMENT PRIMARY KEY,
    evento_id      BIGINT       NOT NULL,
    usuario_id     BIGINT       NOT NULL,
    usuario        VARCHAR(30)  NOT NULL,
    autor          VARCHAR(80)  NOT NULL,
    destino        VARCHAR(10)  NOT NULL,
    mensaje        VARCHAR(280),
    fecha_creacion DATETIME     NOT NULL,
    CONSTRAINT fk_compartidos_evento FOREIGN KEY (evento_id) REFERENCES eventos (id) ON DELETE CASCADE,
    CONSTRAINT ck_compartidos_destino CHECK (destino IN ('SITIO', 'FACEBOOK', 'X', 'WHATSAPP', 'TELEGRAM', 'LINKEDIN')),
    CONSTRAINT uq_compartidos_usuario UNIQUE (evento_id, usuario_id, destino),
    INDEX idx_compartidos_fecha (destino, fecha_creacion)
);

-- Datos de prueba (usuarios de gamer_usuarios: 1 admin, 2 vale, 3 mati, 4 cata, 5 roberto)
INSERT IGNORE INTO comentarios_evento (id, evento_id, usuario_id, usuario, autor, puntuacion, texto, fecha_creacion) VALUES
(1, 1, 2, 'vale', 'Valentina Mora', 5, 'Fui a la edición anterior y la organización fue impecable. Buen nivel de los equipos y la transmisión se veía muy bien.', '2026-11-12 19:20:00'),
(2, 1, 3, 'mati', 'Matías Fuentes', 4, 'Muy buen torneo. Ojalá esta vez agreguen más estaciones para la fase de grupos.', '2026-11-13 21:05:00'),
(3, 2, 4, 'cata', 'Catalina Pérez', 5, 'La mejor LAN de la región, el ambiente es increíble. Lleven alargador.', '2026-11-14 10:42:00'),
(4, 5, 2, 'vale', 'Valentina Mora', 4, 'Mucho para ver y el concurso de cosplay estuvo genial. Las filas de la entrada fueron largas.', '2026-11-15 16:10:00'),
(5, 4, 3, 'mati', 'Matías Fuentes', 3, 'Buen nivel de juego, pero el lugar quedó chico para tanta gente.', '2026-11-16 22:30:00');

INSERT IGNORE INTO compartidos_evento (id, evento_id, usuario_id, usuario, autor, destino, mensaje, fecha_creacion) VALUES
(1, 1, 2, 'vale', 'Valentina Mora', 'SITIO', '¿Quién se anima a armar equipo para la Copa? Faltan dos.', '2026-11-12 19:25:00'),
(2, 5, 4, 'cata', 'Catalina Pérez', 'SITIO', 'Voy con cosplay este año, nos vemos en la convención.', '2026-11-15 18:00:00'),
(3, 2, 3, 'mati', 'Matías Fuentes', 'WHATSAPP', NULL, '2026-11-14 11:00:00');
