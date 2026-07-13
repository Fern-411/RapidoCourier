package com.rapidocourier.servicio_envios.repository;

import com.rapidocourier.servicio_envios.entity.HistorialEstadoEnvio;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.UUID;

public interface HistorialEstadoEnvioRepository extends JpaRepository<HistorialEstadoEnvio, UUID> {
    List<HistorialEstadoEnvio> findByEnvioIdOrderByFechaCambioDesc(UUID envioId);
}
