package com.rapidocourier.servicio_auth.repository;

import com.rapidocourier.servicio_auth.entity.HistorialAcceso;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.List;

@Repository
public interface HistorialAccesoRepository extends JpaRepository<HistorialAcceso, UUID> {
    List<HistorialAcceso> findByUsuarioIdOrderByFechaIntentoDesc(UUID usuarioId);
    
    boolean existsByUsuarioAndIpAddressAndUserAgentAndEstado(com.rapidocourier.servicio_auth.entity.Usuario usuario, String ipAddress, String userAgent, String estado);
}
