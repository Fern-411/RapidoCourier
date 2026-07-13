package com.rapidocourier.servicio_auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class OAuth2LoginRequest {
    
    // Para Google será el idToken, para GitHub será el código de autorización (code)
    @NotBlank(message = "El token o código no puede estar vacío")
    private String token;
}
