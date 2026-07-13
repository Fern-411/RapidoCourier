package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public record LoginRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email valido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    String password
) {}
