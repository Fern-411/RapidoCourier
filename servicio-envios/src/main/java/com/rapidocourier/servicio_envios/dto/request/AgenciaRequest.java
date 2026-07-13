package com.rapidocourier.servicio_envios.dto.request;

import jakarta.validation.constraints.NotBlank;

public record AgenciaRequest(
        @NotBlank(message = "El nombre de la agencia es obligatorio")
        String nombre,
        @NotBlank(message = "La dirección de la agencia es obligatoria")
        String direccion
) {}
