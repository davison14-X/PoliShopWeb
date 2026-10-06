-- ============================================================
--  PoliShop — Esquema de base de datos
--  Motor: MySQL 8+
--  Charset: utf8mb4 / utf8mb4_unicode_ci
-- ============================================================

CREATE DATABASE IF NOT EXISTS polishop
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

USE polishop;

-- ------------------------------------------------------------
-- 1. CATEGORIA
-- ------------------------------------------------------------
CREATE TABLE categoria (
                           id     BIGINT        NOT NULL AUTO_INCREMENT,
                           nombre VARCHAR(80)   NOT NULL,
                           icono  VARCHAR(100)  NULL,

                           CONSTRAINT pk_categoria PRIMARY KEY (id),
                           CONSTRAINT uq_categoria_nombre UNIQUE (nombre)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 2. USUARIO
-- ------------------------------------------------------------
CREATE TABLE usuario (
                         id                    BIGINT        NOT NULL AUTO_INCREMENT,
                         correo_institucional  VARCHAR(150)  NOT NULL,
                         nombre                VARCHAR(80)   NOT NULL,
                         apellido              VARCHAR(80)   NOT NULL,
                         contrasena_hash       VARCHAR(255)  NOT NULL,
                         verificado            BOOLEAN       NOT NULL DEFAULT FALSE,
                         creado_en             TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         actualizado_en        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                             ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT pk_usuario PRIMARY KEY (id),
                         CONSTRAINT uq_usuario_correo UNIQUE (correo_institucional)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 3. TOKEN_VERIFICACION
-- ------------------------------------------------------------
CREATE TABLE token_verificacion (
                                    id          BIGINT      NOT NULL AUTO_INCREMENT,
                                    id_usuario  BIGINT      NOT NULL,
                                    codigo      VARCHAR(10) NOT NULL,
                                    intentos    TINYINT     NOT NULL DEFAULT 0,
                                    expira_en   TIMESTAMP   NOT NULL,
                                    usado       BOOLEAN     NOT NULL DEFAULT FALSE,
                                    creado_en   TIMESTAMP   NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                    CONSTRAINT pk_token_verificacion PRIMARY KEY (id),
                                    CONSTRAINT fk_token_usuario
                                        FOREIGN KEY (id_usuario)
                                            REFERENCES usuario (id)
                                            ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 4. EMPRENDIMIENTO
-- ------------------------------------------------------------
CREATE TABLE emprendimiento (
                                id               BIGINT        NOT NULL AUTO_INCREMENT,
                                id_usuario       BIGINT        NOT NULL,
                                id_categoria     BIGINT        NOT NULL,
                                nombre           VARCHAR(120)  NOT NULL,
                                slug             VARCHAR(150)  NOT NULL,
                                descripcion      TEXT          NULL,
                                logo_url         VARCHAR(500)  NULL,
                                portada_url      VARCHAR(500)  NULL,
                                ubicacion        VARCHAR(200)  NULL,
                                es_virtual       BOOLEAN       NOT NULL DEFAULT FALSE,
                                activo           BOOLEAN       NOT NULL DEFAULT TRUE,
    -- Disponibilidad flash
                                disponible_desde TIMESTAMP     NULL,
                                disponible_hasta TIMESTAMP     NULL,
                                creado_en        TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                actualizado_en   TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                                    ON UPDATE CURRENT_TIMESTAMP,

                                CONSTRAINT pk_emprendimiento PRIMARY KEY (id),
                                CONSTRAINT uq_emprendimiento_slug UNIQUE (slug),
                                CONSTRAINT fk_emprendimiento_usuario
                                    FOREIGN KEY (id_usuario)
                                        REFERENCES usuario (id)
                                        ON DELETE RESTRICT,
                                CONSTRAINT fk_emprendimiento_categoria
                                    FOREIGN KEY (id_categoria)
                                        REFERENCES categoria (id)
                                        ON DELETE RESTRICT
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 5. HORARIO
-- ------------------------------------------------------------
CREATE TABLE horario (
                         id                 BIGINT   NOT NULL AUTO_INCREMENT,
                         id_emprendimiento  BIGINT   NOT NULL,
    -- 1 = lunes … 7 = domingo (ISO 8601)
                         dia_semana         TINYINT  NOT NULL,
                         hora_apertura      TIME     NOT NULL,
                         hora_cierre        TIME     NOT NULL,
                         activo             BOOLEAN  NOT NULL DEFAULT TRUE,

                         CONSTRAINT pk_horario PRIMARY KEY (id),
                         CONSTRAINT fk_horario_emprendimiento
                             FOREIGN KEY (id_emprendimiento)
                                 REFERENCES emprendimiento (id)
                                 ON DELETE CASCADE,
                         CONSTRAINT chk_horario_dia
                             CHECK (dia_semana BETWEEN 1 AND 7),
                         CONSTRAINT chk_horario_horas
                             CHECK (hora_cierre > hora_apertura),
    -- Un emprendimiento no puede tener dos filas para el mismo día
                         CONSTRAINT uq_horario_dia UNIQUE (id_emprendimiento, dia_semana)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 6. CONTACTO
-- ------------------------------------------------------------
CREATE TABLE contacto (
                          id                BIGINT       NOT NULL AUTO_INCREMENT,
                          id_emprendimiento BIGINT       NOT NULL,
    -- Valores posibles: WHATSAPP | INSTAGRAM | TELEFONO | EMAIL | OTRO
                          tipo              VARCHAR(30)  NOT NULL,
                          valor             VARCHAR(200) NOT NULL,
                          principal         BOOLEAN      NOT NULL DEFAULT FALSE,

                          CONSTRAINT pk_contacto PRIMARY KEY (id),
                          CONSTRAINT fk_contacto_emprendimiento
                              FOREIGN KEY (id_emprendimiento)
                                  REFERENCES emprendimiento (id)
                                  ON DELETE CASCADE,
                          CONSTRAINT chk_contacto_tipo
                              CHECK (tipo IN ('WHATSAPP','INSTAGRAM','TELEFONO','EMAIL','OTRO'))
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 7. PRODUCTO
-- ------------------------------------------------------------
CREATE TABLE producto (
                          id                BIGINT        NOT NULL AUTO_INCREMENT,
                          id_emprendimiento BIGINT        NOT NULL,
                          nombre            VARCHAR(120)  NOT NULL,
                          slug              VARCHAR(150)  NOT NULL,
                          descripcion       TEXT          NULL,
                          precio            DECIMAL(12,2) NOT NULL,
                          disponible        BOOLEAN       NOT NULL DEFAULT TRUE,
                          creado_en         TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          actualizado_en    TIMESTAMP     NOT NULL DEFAULT CURRENT_TIMESTAMP
                              ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT pk_producto PRIMARY KEY (id),
                          CONSTRAINT uq_producto_slug UNIQUE (slug),
                          CONSTRAINT fk_producto_emprendimiento
                              FOREIGN KEY (id_emprendimiento)
                                  REFERENCES emprendimiento (id)
                                  ON DELETE CASCADE,
                          CONSTRAINT chk_producto_precio
                              CHECK (precio >= 0)
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 8. IMAGEN_PRODUCTO
-- ------------------------------------------------------------
CREATE TABLE imagen_producto (
                                 id          BIGINT       NOT NULL AUTO_INCREMENT,
                                 id_producto BIGINT       NOT NULL,
                                 url         VARCHAR(500) NOT NULL,
    -- 0 = imagen principal
                                 orden       TINYINT      NOT NULL DEFAULT 0,

                                 CONSTRAINT pk_imagen_producto PRIMARY KEY (id),
                                 CONSTRAINT fk_imagen_producto
                                     FOREIGN KEY (id_producto)
                                         REFERENCES producto (id)
                                         ON DELETE CASCADE
) ENGINE=InnoDB;

-- ------------------------------------------------------------
-- 9. ME_GUSTA
-- Exactamente uno de id_producto / id_emprendimiento debe ser NOT NULL
-- ------------------------------------------------------------
CREATE TABLE me_gusta (
                          id                BIGINT    NOT NULL AUTO_INCREMENT,
                          id_usuario        BIGINT    NOT NULL,
                          id_producto       BIGINT    NULL,
                          id_emprendimiento BIGINT    NULL,
                          creado_en         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,

                          CONSTRAINT pk_me_gusta PRIMARY KEY (id),
                          CONSTRAINT fk_megusta_usuario
                              FOREIGN KEY (id_usuario)
                                  REFERENCES usuario (id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_megusta_producto
                              FOREIGN KEY (id_producto)
                                  REFERENCES producto (id)
                                  ON DELETE CASCADE,
                          CONSTRAINT fk_megusta_emprendimiento
                              FOREIGN KEY (id_emprendimiento)
                                  REFERENCES emprendimiento (id)
                                  ON DELETE CASCADE,
    -- Solo un like por usuario por entidad
                          CONSTRAINT uq_megusta_producto
                              UNIQUE (id_usuario, id_producto),
                          CONSTRAINT uq_megusta_emprendimiento
                              UNIQUE (id_usuario, id_emprendimiento),
    -- Exactamente uno de los dos debe estar presente
                          CONSTRAINT chk_megusta_target
                              CHECK (
                                  (id_producto IS NOT NULL AND id_emprendimiento IS NULL)
                                      OR
                                  (id_producto IS NULL AND id_emprendimiento IS NOT NULL)
                                  )
) ENGINE=InnoDB;

-- ============================================================
--  ÍNDICES DE BÚSQUEDA Y FILTRADO
-- ============================================================

-- Búsqueda de emprendimientos por categoría y estado
CREATE INDEX idx_emprendimiento_categoria ON emprendimiento (id_categoria);
CREATE INDEX idx_emprendimiento_activo    ON emprendimiento (activo);

-- Filtro de flash disponibility
CREATE INDEX idx_emprendimiento_flash
    ON emprendimiento (disponible_desde, disponible_hasta);

-- Búsqueda de productos por emprendimiento y disponibilidad
CREATE INDEX idx_producto_emprendimiento ON producto (id_emprendimiento);
CREATE INDEX idx_producto_disponible     ON producto (disponible);

-- Búsqueda full-text sobre nombre y descripción de producto
CREATE FULLTEXT INDEX ft_producto_nombre_desc
  ON producto (nombre, descripcion);

-- Búsqueda full-text sobre nombre y descripción de emprendimiento
CREATE FULLTEXT INDEX ft_emprendimiento_nombre_desc
  ON emprendimiento (nombre, descripcion);

-- Tokens de verificación por usuario (para buscar el más reciente)
CREATE INDEX idx_token_usuario ON token_verificacion (id_usuario);

-- ============================================================
--  DATOS INICIALES — Categorías
-- ============================================================

INSERT INTO categoria (nombre, icono) VALUES
                                          ('Comida y bebidas',     'ti-bowl'),
                                          ('Ropa y accesorios',    'ti-shirt'),
                                          ('Tecnología',           'ti-device-laptop'),
                                          ('Arte y manualidades',  'ti-palette'),
                                          ('Belleza y cuidado',    'ti-sparkles'),
                                          ('Servicios',            'ti-tools'),
                                          ('Papelería y útiles',   'ti-notebook'),
                                          ('Otros',                'ti-dots');