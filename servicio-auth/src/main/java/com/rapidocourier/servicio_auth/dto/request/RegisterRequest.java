package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @NotBlank(message = "El email es obligatorio")
    @Email(message = "Debe ser un email valido")
    String email,

    @NotBlank(message = "La contraseña es obligatoria")
    @Pattern(regexp = "^(?=.*[A-Za-z])(?=.*\\d)[A-Za-z\\d@$!%*#?&]{8,}$", message = "La contraseña debe tener al menos 8 caracteres, incluyendo al menos una letra y un número")
    String password,

    @NotBlank(message = "El rol es obligatorio")
    String rol,

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "El número de contacto debe contener entre 9 y 15 dígitos y puede iniciar con '+'")
    String numeroContacto
) {}
