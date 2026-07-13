package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record RefreshTokenRequest(
    @NotBlank(message = "El token de refresco no puede estar vacío")
    String refreshToken
) {}
