package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ActualizarPerfilRequest(
    @NotBlank(message = "El nombre es obligatorio")
    String nombre,

    @Pattern(regexp = "^\\+?[0-9]{9,15}$", message = "El número de contacto debe contener entre 9 y 15 dígitos y puede iniciar con '+'")
    String numeroContacto
) {}
