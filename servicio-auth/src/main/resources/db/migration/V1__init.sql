-- Creación de la tabla de Roles
CREATE TABLE roles (
                       id UUID PRIMARY KEY,
                       nombre VARCHAR(50) NOT NULL UNIQUE
);

-- Creación de la tabla de Usuarios
CREATE TABLE usuarios (
                          id UUID PRIMARY KEY,
                          nombre VARCHAR(150) NOT NULL,
                          email VARCHAR(150) NOT NULL UNIQUE,
                          password VARCHAR(255) NOT NULL,
                          rol_id UUID NOT NULL,
                          created_at TIMESTAMP(6),
                          updated_at TIMESTAMP(6),
                          CONSTRAINT fkqf5elo4jcq7qrt83oi0qmenjo FOREIGN KEY (rol_id) REFERENCES roles(id)
);