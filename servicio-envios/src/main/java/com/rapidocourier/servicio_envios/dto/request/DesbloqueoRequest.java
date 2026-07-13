package com.rapidocourier.servicio_envios.dto.request;

import jakarta.validation.constraints.NotBlank;

public record DesbloqueoRequest(
        @NotBlank(message = "El OTP es obligatorio")
        String otp,
        @NotBlank(message = "La nueva clave de recojo es obligatoria")
        String nuevaClaveRecojo
) {}
