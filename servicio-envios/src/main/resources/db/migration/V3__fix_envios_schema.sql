-- Drop incorrect tables copied from paquetes
DROP TABLE IF EXISTS paquetes_categorias CASCADE;
DROP TABLE IF EXISTS categorias CASCADE;
DROP TABLE IF EXISTS historial_estados CASCADE;
DROP TABLE IF EXISTS paquetes CASCADE;

-- Create valid tables for envios service if they do not exist (hibernate might have created them on update)
CREATE TABLE IF NOT EXISTS agencias (
    id UUID PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    direccion VARCHAR(200) NOT NULL
);

CREATE TABLE IF NOT EXISTS envios (
    id UUID PRIMARY KEY,
    codigo_rastreo VARCHAR(50) NOT NULL UNIQUE,
    paquete_id UUID NOT NULL,
    destinatario_id UUID NOT NULL,
    agencia_origen_id UUID NOT NULL REFERENCES agencias(id),
    agencia_destino_id UUID NOT NULL REFERENCES agencias(id),
    estado_actual VARCHAR(50) NOT NULL,
    clave_recojo VARCHAR(6),
    created_at TIMESTAMP,
    updated_at TIMESTAMP
);

CREATE TABLE IF NOT EXISTS historial_estado_envios (
    id UUID PRIMARY KEY,
    envio_id UUID NOT NULL REFERENCES envios(id),
    estado VARCHAR(50) NOT NULL,
    fecha_cambio TIMESTAMP,
    usuario_responsable VARCHAR(100) NOT NULL
);
