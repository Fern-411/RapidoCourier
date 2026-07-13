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
-- Name: pagos; Type: TABLE; Schema: public; Owner: rapidouser
--

CREATE TABLE public.pagos (
    id uuid NOT NULL,
    estado_pago character varying(50) NOT NULL,
    fecha_pago timestamp(6) without time zone,
    monto numeric(10,2) NOT NULL,
    paquete_id uuid NOT NULL
);


ALTER TABLE public.pagos OWNER TO rapidouser;

--
-- Name: pagos pagos_pkey; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT pagos_pkey PRIMARY KEY (id);


--
-- Name: pagos ukhenauqgcga0ir6e78xygw143v; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.pagos
    ADD CONSTRAINT ukhenauqgcga0ir6e78xygw143v UNIQUE (paquete_id);


--
-- PostgreSQL database dump complete
--


