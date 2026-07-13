package com.rapidocourier.servicio_pagos.dto.response;

import java.math.BigDecimal;

public record EstadisticasPagoResponse(
        BigDecimal ingresosHoy,
        BigDecimal ingresosMes
) {}
