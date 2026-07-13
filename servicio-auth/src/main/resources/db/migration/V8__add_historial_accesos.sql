-- Crear tabla historial_accesos si no fue creada previamente por Hibernate update
CREATE TABLE IF NOT EXISTS historial_accesos (
    id UUID PRIMARY KEY,
    usuario_id UUID REFERENCES usuarios(id),
    email_intentado VARCHAR(150),
    ip_address VARCHAR(50),
    user_agent VARCHAR(500),
    estado VARCHAR(20) NOT NULL,
    motivo_fallo VARCHAR(255),
    fecha_intento TIMESTAMP NOT NULL
);

CREATE INDEX IF NOT EXISTS idx_historial_usuario ON historial_accesos(usuario_id);
CREATE INDEX IF NOT EXISTS idx_historial_fecha ON historial_accesos(fecha_intento);
