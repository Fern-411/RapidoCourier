-- Insertar roles iniciales
INSERT INTO roles (id, nombre) VALUES
                                   (gen_random_uuid(), 'ADMIN'),
                                   (gen_random_uuid(), 'EMPLEADO'),
                                   (gen_random_uuid(), 'CLIENTE');