package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record OtpSendRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un correo electrónico válido")
    String email,

    @jakarta.validation.constraints.Pattern(regexp = "^(EMAIL|SMS)$", message = "El canal debe ser EMAIL o SMS")
    String canal
) {}
