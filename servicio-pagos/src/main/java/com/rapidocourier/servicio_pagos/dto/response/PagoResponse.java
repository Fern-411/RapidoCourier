package com.rapidocourier.servicio_pagos.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PagoResponse(
        UUID id,
        UUID paqueteId,
        BigDecimal monto,
        String estadoPago,
        LocalDateTime fechaPago
) {}