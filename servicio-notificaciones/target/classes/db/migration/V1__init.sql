CREATE TABLE notificaciones (
                                id uuid NOT NULL,
                                estado character varying(20) NOT NULL,
                                fecha_envio timestamp(6) without time zone,
                                mensaje character varying(500) NOT NULL,
                                paquete_id uuid NOT NULL,
                                CONSTRAINT notificaciones_pkey PRIMARY KEY (id)
);