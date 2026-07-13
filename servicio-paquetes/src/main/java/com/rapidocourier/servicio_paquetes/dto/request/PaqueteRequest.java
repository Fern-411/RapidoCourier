package com.rapidocourier.servicio_paquetes.dto.request;

import jakarta.validation.constraints.*;
import java.math.BigDecimal;
import java.util.Set;

public record PaqueteRequest(
        @NotBlank(message = "El DNI del remitente es obligatorio")
        String dniRemitente,
        @NotBlank(message = "El DNI del destinatario es obligatorio")
        String dniDestinatario,
        @NotNull(message = "El peso es obligatorio")
        @Positive(message = "El peso debe ser mayor a 0")
        BigDecimal pesoKg,
        @NotNull(message = "El valor declarado es obligatorio")
        @Positive(message = "El valor declarado debe ser mayor a 0")
        BigDecimal valorDeclarado,

        @NotNull(message = "El alto es obligatorio")
        @Positive(message = "El alto debe ser mayor a 0")
        BigDecimal altoCm,

        @NotNull(message = "El ancho es obligatorio")
        @Positive(message = "El ancho debe ser mayor a 0")
        BigDecimal anchoCm,

        @NotNull(message = "El largo es obligatorio")
        @Positive(message = "El largo debe ser mayor a 0")
        BigDecimal largoCm,

        @Size(min = 1, message = "Debe haber al menos una categoria")
        Set<String> categorias
) {}