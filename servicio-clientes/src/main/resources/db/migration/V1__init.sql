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
-- Name: clientes; Type: TABLE; Schema: public; Owner: rapidouser
--

CREATE TABLE public.clientes (
    id uuid NOT NULL,
    created_at timestamp(6) without time zone,
    dni character varying(8) NOT NULL,
    email character varying(150) NOT NULL,
    nombre_completo character varying(150) NOT NULL,
    telefono character varying(20),
    updated_at timestamp(6) without time zone
);


ALTER TABLE public.clientes OWNER TO rapidouser;

--
-- Name: clientes clientes_pkey; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT clientes_pkey PRIMARY KEY (id);


--
-- Name: clientes uk1c96wv36rk2hwui7qhjks3mvg; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT uk1c96wv36rk2hwui7qhjks3mvg UNIQUE (email);


--
-- Name: clientes ukm6ysdwsqke00e5piajbvgn6lg; Type: CONSTRAINT; Schema: public; Owner: rapidouser
--

ALTER TABLE ONLY public.clientes
    ADD CONSTRAINT ukm6ysdwsqke00e5piajbvgn6lg UNIQUE (dni);


--
-- PostgreSQL database dump complete
--


