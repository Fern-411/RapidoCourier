-- Insertar nuevos roles: REPARTIDOR y SUPERVISOR
INSERT INTO roles (id, nombre) VALUES
    (gen_random_uuid(), 'REPARTIDOR'),
    (gen_random_uuid(), 'SUPERVISOR');
