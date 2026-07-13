package com.rapidocourier.servicio_envios.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record HistorialEstadoEnvioResponse(
        UUID id,
        String estado,
        LocalDateTime fechaCambio,
        String usuarioResponsable
) {}
