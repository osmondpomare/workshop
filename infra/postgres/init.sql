CREATE TABLE IF NOT EXISTS parametro (
    id_parametro    VARCHAR(60)  PRIMARY KEY,
    valor           VARCHAR(255) NOT NULL,
    descripcion     VARCHAR(255)
);

CREATE TABLE IF NOT EXISTS reverso (
    id_reverso            VARCHAR(36)    PRIMARY KEY,
    id_mensaje            VARCHAR(64)    NOT NULL UNIQUE,
    id_transaccion        VARCHAR(64)    NOT NULL,
    numero_celular        VARCHAR(20)    NOT NULL,
    valor                 NUMERIC(18,2)  NOT NULL,
    moneda                VARCHAR(3)     NOT NULL,
    tasa_cambio           NUMERIC(18,6),
    valor_cop             NUMERIC(18,2),
    motivo                VARCHAR(255),
    estado                VARCHAR(20)    NOT NULL,
    canal                 VARCHAR(20),
    fecha_creacion        TIMESTAMP      NOT NULL,
    fecha_modificacion    TIMESTAMP,
    usuario_creacion      VARCHAR(50),
    usuario_modificacion  VARCHAR(50)
);

CREATE INDEX IF NOT EXISTS idx_reverso_transaccion ON reverso (id_transaccion);

INSERT INTO parametro (id_parametro, valor, descripcion) VALUES
    ('BCO_WKS901_MONEDA_DESTINO', 'COP', 'Target currency used to convert reversal amounts'),
    ('BCO_WKS901_MAX_REINTENTOS', '3', 'Max JMS redelivery attempts processed by the notification MDB')
ON CONFLICT (id_parametro) DO NOTHING;
