package com.rapidocourier.servicio_pagos.dto.request;

import java.math.BigDecimal;
import java.util.UUID;

public record PagoRequest(UUID paqueteId, BigDecimal monto) {}