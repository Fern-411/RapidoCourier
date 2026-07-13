package com.rapidocourier.servicio_paquetes.dto.response;

import java.util.UUID;

public record ClienteResponse(
    UUID id,
    String dni,
    String nombreCompleto,
    String email,
    String telefono
) {}
