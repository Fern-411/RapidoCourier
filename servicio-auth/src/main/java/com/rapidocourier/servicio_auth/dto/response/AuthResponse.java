package com.rapidocourier.servicio_auth.dto.response;

public record AuthResponse(
    String token,
    String refreshToken,
    String email,
    String rol
) {}
