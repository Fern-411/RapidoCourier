package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record EmailChangeRequest(
    @NotBlank(message = "El nuevo email es obligatorio")
    @Email(message = "Debe ser un email válido")
    String nuevoEmail
) {}
