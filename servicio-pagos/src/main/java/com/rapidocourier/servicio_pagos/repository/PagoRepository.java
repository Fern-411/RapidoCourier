package com.rapidocourier.servicio_pagos.repository;

import com.rapidocourier.servicio_pagos.entity.Pago;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PagoRepository extends JpaRepository<Pago, UUID> {
    Optional<Pago> findByPaqueteId(UUID paqueteId);
    
    @org.springframework.data.jpa.repository.Query("SELECT SUM(p.monto) FROM Pago p WHERE p.estadoPago = :estado AND p.fechaPago >= :startDate AND p.fechaPago < :endDate")
    java.math.BigDecimal sumMontoByEstadoAndFechaBetween(
        @org.springframework.data.repository.query.Param("estado") String estado,
        @org.springframework.data.repository.query.Param("startDate") java.time.LocalDateTime startDate,
        @org.springframework.data.repository.query.Param("endDate") java.time.LocalDateTime endDate
    );
}
