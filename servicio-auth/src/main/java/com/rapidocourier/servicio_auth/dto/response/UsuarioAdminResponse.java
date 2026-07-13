package com.rapidocourier.servicio_auth.dto.response;

import java.util.UUID;

public record UsuarioAdminResponse(
    UUID id,
    String nombre,
    String email,
    String numeroContacto,
    String rol
) {}
