package com.rapidocourier.servicio_envios.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record EnvioResponse(
        UUID id,
        String numeroOrden,
        String codigoRastreo,
        UUID paqueteId,
        String agenciaOrigen,
        String agenciaDestino,
        String estadoActual,
        String urlBoleta,
        String urlGuia,
        String tipoPago,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}
