package com.rapidocourier.servicio_paquetes.repository;

import com.rapidocourier.servicio_paquetes.entity.Paquete;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public interface PaqueteRepository extends JpaRepository<Paquete, UUID> {
    List<Paquete> findByRemitenteId(UUID remitenteId);

    List<Paquete> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
    List<Paquete> findByCreatedAtGreaterThanEqual(LocalDateTime start);
    List<Paquete> findByCreatedAtLessThanEqual(LocalDateTime end);
}
