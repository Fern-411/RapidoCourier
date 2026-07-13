package com.rapidocourier.servicio_envios.dto.response;

import java.math.BigDecimal;
import java.time.LocalDateTime;

public record BoletaDetalleResponse(
        String numeroOrden,
        String codigoRastreo,
        LocalDateTime fechaEmision,
        String agenciaOrigen,
        String direccionOrigen,
        String agenciaDestino,
        String direccionDestino,
        ClienteDetalle remitente,
        ClienteDetalle destinatario,
        BigDecimal pesoKg,
        String descripcionPaquete,
        BigDecimal montoTotal,
        String estadoPago
) {
    public record ClienteDetalle(
            String nombreCompleto,
            String dni,
            String telefono
    ) {}
}
