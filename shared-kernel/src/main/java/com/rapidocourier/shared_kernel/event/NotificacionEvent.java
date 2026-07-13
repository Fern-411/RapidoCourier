package com.rapidocourier.shared_kernel.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class NotificacionEvent implements Serializable {
    private UUID paqueteId; // Puede ser null si es un evento auth
    private String destinatario; // Email
    private String numeroContacto; // Puede ser null
    private String tipoNotificacion; // "PAQUETE", "OTP_AUTH", "VERIFICACION_EMAIL"
    private String asunto; // "Tu paquete fue entregado" o "Código de Verificación"
    private String mensaje;
}
