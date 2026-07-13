-- Add Contact and OTP fields to usuarios
ALTER TABLE usuarios
ADD COLUMN numero_contacto VARCHAR(20),
ADD COLUMN codigo_verificacion VARCHAR(6),
ADD COLUMN codigo_expiracion TIMESTAMP,
ADD COLUMN intentos_verificacion INTEGER DEFAULT 0;

-- Create Refresh Token Table
CREATE TABLE refresh_tokens (
    id UUID PRIMARY KEY,
    token_hash VARCHAR(64) NOT NULL UNIQUE,
    family_id UUID NOT NULL,
    usuario_id UUID NOT NULL,
    revoked BOOLEAN NOT NULL DEFAULT FALSE,
    expires_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_refresh_usuario FOREIGN KEY (usuario_id) REFERENCES usuarios (id)
);

CREATE INDEX idx_refresh_token_hash ON refresh_tokens (token_hash);
CREATE INDEX idx_refresh_family ON refresh_tokens (family_id);
CREATE INDEX idx_refresh_usuario ON refresh_tokens (usuario_id);
