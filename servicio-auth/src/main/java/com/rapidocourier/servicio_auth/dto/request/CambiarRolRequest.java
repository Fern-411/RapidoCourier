package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.NotBlank;

public record CambiarRolRequest(
    @NotBlank(message = "El nuevo rol es obligatorio")
    String nuevoRol
) {}
