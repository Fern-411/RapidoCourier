package com.rapidocourier.servicio_clientes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

public record ClienteRequest(
    @NotBlank(message = "El DNI no puede estar vacio")
    @Pattern(regexp = "\\d{8}", message = "El DNI debe tener 8 digitos")
    String dni,

    @NotBlank(message = "El email no puede estar vacio")
    @Email(message = "Formato de email invalido")
    String email,

    @Size(max = 20, message = "El telefono no debe exceder los 20 caracteres")
    String telefono
) {}
