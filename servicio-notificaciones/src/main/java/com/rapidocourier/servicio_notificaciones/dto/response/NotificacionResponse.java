package com.rapidocourier.servicio_notificaciones.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record NotificacionResponse(
        UUID id,
        UUID paqueteId,
        String mensaje,
        String estado,
        LocalDateTime fechaEnvio
) {}