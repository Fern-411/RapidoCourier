package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;

import jakarta.validation.constraints.Pattern;

public record ResetPasswordRequest(
    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email válido")
    String email,

    @NotBlank(message = "El token de reseteo es obligatorio")
    String resetToken,

    @NotBlank(message = "La nueva contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", message = "La nueva contraseña debe tener al menos 8 caracteres, incluyendo al menos una letra y un número")
    String nuevaPassword
) {}
