--
-- PostgreSQL database dump
--


-- Dumped from database version 15.18
-- Dumped by pg_dump version 15.18

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: categorias; Type: TABLE; Schema: public; Owner: rapidouser
--

CREATE TABLE public.categorias (
    id uuid NOT NULL,
    nombre character varying(100) NOT NULL
);


ALTER TABLE public.categorias OWNER TO rapidouser;

--
-- Name: historial_estados; Type: TABLE; Schema: public; Owner: rapidouser
--

CREATE TABLE public.historial_estados (
    id uuid NOT NULL,
    estado character varying(50) NOT NULL,
    fecha_cambio timestamp(6) without time zone NOT NULL,
    usuario_responsable character varying(150) NOT NULL,
    paquete_id uuid NOT NULL,
    CONSTRAINT historial_estados_estado_check CHECK (((estado)::text = ANY ((ARRAY['REGISTRADO'::character varying, 'EN_TRANSITO'::character varying, 'EN_SUCURSAL_DESTINO'::character varying, 'ENTREGADO'::character varying])::text[])))
);


ALTER TABLE public.historial_estados OWNER TO rapidouser;

--
-- Name: paquetes; Type: TABLE; Schema: public; Owner: rapidouser
--

CREATE TABLE public.paquetes (
    id uuid NOT NULL,
    codigo_rastreo character varying(50) NOT NULL,
    created_at timestamp(6) without time zone,
    destinatario_id uuid NOT NULL,
    estado_actual character varying(50) NOT NULL,
    peso_kg numeric(10,2) NOT NULL,
    remitente_id uuid NOT NULL,
    sucursal_destino character varying(100) NOT NULL,
    sucursal_origen character varying(100) NOT NULL,
    tarifa numeric(10,2) NOT NULL,
    valor_declarado numeric(10,2) NOT NULL,
    CONSTRAINT paquetes_estado_actual_check CHECK (((estado_actual)::text = ANY ((ARRAY['REGISTRADO'::character varying, 'EN_TRANSITO'::character varying, 'EN_SUCURSAL_DESTINO'::character varying, 'ENTREGADO'::character varying])::text[])))
);


ALTER TABLE public.paquetes OWNER TO rapidouser;

--
-- Name: paquetes_categorias; Type: TABLE; Schema: public; Owner: rapidouser
--

CREATE TABLE public.paquetes_categorias (
    paquete_id uuid NOT NULL,
    categoria_id uuid NOT NULL
);


ALTER TABLE public.paquetes_categorias OWNER TO rapidouser;

--
-- Name: categorias categorias_pkey; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT categorias_pkey PRIMARY KEY (id);


--
-- Name: historial_estados historial_estados_pkey; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.historial_estados
    ADD CONSTRAINT historial_estados_pkey PRIMARY KEY (id);


--
-- Name: paquetes_categorias paquetes_categorias_pkey; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.paquetes_categorias
    ADD CONSTRAINT paquetes_categorias_pkey PRIMARY KEY (paquete_id, categoria_id);


--
-- Name: paquetes paquetes_pkey; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.paquetes
    ADD CONSTRAINT paquetes_pkey PRIMARY KEY (id);


--
-- Name: paquetes ukd235b8yyecdoi0hm9aug9mq5; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.paquetes
    ADD CONSTRAINT ukd235b8yyecdoi0hm9aug9mq5 UNIQUE (codigo_rastreo);


--
-- Name: categorias ukqcog8b7hps1hioi9onqwjdt6y; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.categorias
    ADD CONSTRAINT ukqcog8b7hps1hioi9onqwjdt6y UNIQUE (nombre);


--
-- Name: historial_estados fkbatsytb2a6000at2pdhlspc1j; Type: FK CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.historial_estados
    ADD CONSTRAINT fkbatsytb2a6000at2pdhlspc1j FOREIGN KEY (paquete_id) REFERENCES public.paquetes(id);


--
-- Name: paquetes_categorias fkgbgg7lxsxemppynk5j8tcm1b4; Type: FK CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.paquetes_categorias
    ADD CONSTRAINT fkgbgg7lxsxemppynk5j8tcm1b4 FOREIGN KEY (categoria_id) REFERENCES public.categorias(id);


--
-- Name: paquetes_categorias fkr2ob6nlw13vj8gaq9k3dyskqw; Type: FK CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.paquetes_categorias
    ADD CONSTRAINT fkr2ob6nlw13vj8gaq9k3dyskqw FOREIGN KEY (paquete_id) REFERENCES public.paquetes(id);


--
-- PostgreSQL database dump complete
--


