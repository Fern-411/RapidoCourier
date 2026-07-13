package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Pattern;

public record CambiarPasswordRequest(
    @NotBlank(message = "La contraseña actual es obligatoria")
    String passwordActual,

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", message = "La nueva contraseña debe tener al menos 8 caracteres, incluyendo al menos una letra y un número")
    String nuevaPassword
) {}
