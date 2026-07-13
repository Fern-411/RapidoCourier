package com.rapidocourier.servicio_envios.repository;

import com.rapidocourier.servicio_envios.entity.Envio;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface EnvioRepository extends JpaRepository<Envio, UUID>, org.springframework.data.jpa.repository.JpaSpecificationExecutor<Envio> {
    Optional<Envio> findByCodigoRastreo(String codigoRastreo);
    Optional<Envio> findByNumeroOrdenAndCodigoRastreo(String numeroOrden, String codigoRastreo);
    Optional<Envio> findByPaqueteId(UUID paqueteId);
    java.util.List<Envio> findByDestinatarioIdOrderByCreatedAtDesc(UUID destinatarioId);

    @org.springframework.data.jpa.repository.Query("SELECT e.estadoActual, COUNT(e) FROM Envio e WHERE e.updatedAt >= :startOfDay GROUP BY e.estadoActual")
    java.util.List<Object[]> countByEstadoDesde(@org.springframework.data.repository.query.Param("startOfDay") java.time.LocalDateTime startOfDay);
}
