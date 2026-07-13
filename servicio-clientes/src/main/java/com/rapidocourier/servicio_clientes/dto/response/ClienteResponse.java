package com.rapidocourier.servicio_clientes.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record ClienteResponse(
        UUID id,
        String dni,
        String nombreCompleto,
        String email,
        String telefono,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {}