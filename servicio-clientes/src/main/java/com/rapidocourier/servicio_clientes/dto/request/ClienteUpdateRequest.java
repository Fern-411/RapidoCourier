package com.rapidocourier.servicio_clientes.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.Size;

public record ClienteUpdateRequest(
    @Email(message = "Formato de email invalido")
    String email,

    @Size(max = 20, message = "El telefono no debe exceder los 20 caracteres")
    String telefono
) {}
