package com.rapidocourier.servicio_paquetes.dto.response;


import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public record PaqueteResponse(
    UUID id,
    BigDecimal pesoKg,
    BigDecimal valorDeclarado,
    BigDecimal altoCm,
    BigDecimal anchoCm,
    BigDecimal largoCm,
    LocalDateTime fechaRegistro,
    String nombreRemitente,
    String nombreDestinatario,
    UUID remitenteId,
    UUID destinatarioId
) {}
