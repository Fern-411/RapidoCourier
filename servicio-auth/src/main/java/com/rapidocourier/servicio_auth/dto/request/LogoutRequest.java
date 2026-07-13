package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record LogoutRequest(
    @NotBlank(message = "El refresh token es obligatorio")
    String refreshToken
) {}
