-- V3__refactor_paquetes_schema.sql

-- Drop table that belongs to servicio-envios now
DROP TABLE IF EXISTS historial_estados;

-- Drop constraints that might cause issues when dropping columns
ALTER TABLE paquetes DROP CONSTRAINT IF EXISTS ukd235b8yyecdoi0hm9aug9mq5;
ALTER TABLE paquetes DROP CONSTRAINT IF EXISTS paquetes_estado_actual_check;

-- Drop removed columns
ALTER TABLE paquetes DROP COLUMN IF EXISTS codigo_rastreo CASCADE;
ALTER TABLE paquetes DROP COLUMN IF EXISTS estado_actual CASCADE;
ALTER TABLE paquetes DROP COLUMN IF EXISTS sucursal_destino CASCADE;
ALTER TABLE paquetes DROP COLUMN IF EXISTS sucursal_origen CASCADE;
ALTER TABLE paquetes DROP COLUMN IF EXISTS tarifa CASCADE;

-- Add new columns
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS alto_cm NUMERIC(10,2) DEFAULT 0.0 NOT NULL;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS ancho_cm NUMERIC(10,2) DEFAULT 0.0 NOT NULL;
ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS largo_cm NUMERIC(10,2) DEFAULT 0.0 NOT NULL;
