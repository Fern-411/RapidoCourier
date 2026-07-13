package com.rapidocourier.servicio_auth.event;

import com.rapidocourier.servicio_auth.entity.Usuario;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class UserLoginEvent {
    private final Usuario usuario;
    private final String email;
    private final String ipAddress;
    private final String userAgent;
    private final String estado;
    private final String motivo;
}
