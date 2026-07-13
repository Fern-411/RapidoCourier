package com.rapidocourier.servicio_envios.dto.response;

import java.util.UUID;

public record AgenciaResponse(
        UUID id,
        String nombre,
        String direccion
) {}
