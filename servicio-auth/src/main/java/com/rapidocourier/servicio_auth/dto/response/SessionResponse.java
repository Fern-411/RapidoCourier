package com.rapidocourier.servicio_auth.dto.response;

import java.time.LocalDateTime;
import java.util.UUID;

public record SessionResponse(
        UUID familyId,
        String ipAddress,
        String userAgent,
        LocalDateTime createdAt
) {}
