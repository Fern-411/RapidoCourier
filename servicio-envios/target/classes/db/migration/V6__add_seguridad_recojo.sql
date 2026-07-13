ALTER TABLE envios ADD COLUMN intentos_fallidos_recojo INT DEFAULT 0;
ALTER TABLE envios ADD COLUMN recojo_bloqueado BOOLEAN DEFAULT FALSE;
ALTER TABLE envios ADD COLUMN otp_desbloqueo VARCHAR(6);
ALTER TABLE envios ADD COLUMN otp_desbloqueo_expiracion TIMESTAMP;
