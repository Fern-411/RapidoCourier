-- V2__audit_fields.sql
-- Agregar campos de auditoría requeridos por ddl-auto: validate

ALTER TABLE paquetes ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
ALTER TABLE categorias ADD COLUMN IF NOT EXISTS created_at TIMESTAMP;
ALTER TABLE categorias ADD COLUMN IF NOT EXISTS updated_at TIMESTAMP;
