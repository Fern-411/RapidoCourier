package com.rapidocourier.servicio_envios.dto.request;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record EnvioRequest(
        @NotNull(message = "El ID del paquete es obligatorio")
        UUID paqueteId,
        @NotNull(message = "La agencia de origen es obligatoria")
        UUID agenciaOrigenId,
        @NotNull(message = "La agencia de destino es obligatoria")
        UUID agenciaDestinoId,
        @jakarta.validation.constraints.NotBlank(message = "La clave de recojo es obligatoria")
        String claveRecojo,
        String tipoPago
) {}
