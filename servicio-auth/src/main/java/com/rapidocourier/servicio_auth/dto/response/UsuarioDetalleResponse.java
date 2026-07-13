package com.rapidocourier.servicio_auth.dto.response;

public record UsuarioDetalleResponse(
    String nombre,
    String email,
    String numeroContacto,
    String rol
) {}
