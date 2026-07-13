package com.rapidocourier.servicio_envios.dto.response;

import java.util.Map;

public record EstadisticasEnvioResponse(
        long totalEnviosHoy,
        Map<String, Long> enviosPorEstado
) {}
