package com.rapidocourier.servicio_notificaciones.dto.request;

import io.swagger.v3.oas.annotations.media.Schema;
import java.util.UUID;

/**
 * DTO para la solicitud de envío de notificaciones.
 * Usamos record para inmutabilidad y código conciso.
 */
public record NotificacionRequest(

        @Schema(description = "UUID del paquete al que pertenece la notificación")
        UUID paqueteId,

        @Schema(description = "Contenido del mensaje de la notificación")
        String mensaje
) {}