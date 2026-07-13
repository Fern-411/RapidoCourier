package com.rapidocourier.servicio_notificaciones.repository;

import com.rapidocourier.servicio_notificaciones.entity.Notificacion;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface NotificacionRepository extends JpaRepository<Notificacion, UUID> {
    List<Notificacion> findByPaqueteId(UUID paqueteId);
    List<Notificacion> findByPaqueteIdOrderByFechaEnvioDesc(UUID paqueteId);
}
