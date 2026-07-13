package com.rapidocourier.servicio_auth.listener;

import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.event.UserLoginEvent;
import com.rapidocourier.servicio_auth.repository.HistorialAccesoRepository;
import com.rapidocourier.shared_kernel.event.NotificacionEvent;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.context.event.EventListener;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class AuthEventListener {

    private final HistorialAccesoRepository historialAccesoRepository;
    private final RabbitTemplate rabbitTemplate;

    @EventListener
    @Async
    public void handleUserLoginEvent(UserLoginEvent event) {
        Usuario usuario = event.getUsuario();
        String ip = event.getIpAddress();
        String userAgent = event.getUserAgent();
        String email = event.getEmail();
        String estado = event.getEstado();
        String motivo = event.getMotivo();
        
        if ("EXITO".equals(estado) && usuario != null) {
            boolean deviceExists = historialAccesoRepository.existsByUsuarioAndIpAddressAndUserAgentAndEstado(usuario, ip, userAgent, "EXITO");
            if (!deviceExists) {
                com.rapidocourier.shared_kernel.event.NotificacionEvent notifEvent = new com.rapidocourier.shared_kernel.event.NotificacionEvent();
                notifEvent.setDestinatario(usuario.getEmail());
                notifEvent.setTipoNotificacion("ALERTA_SEGURIDAD");
                notifEvent.setAsunto("Nuevo inicio de sesión detectado - Rapido Courier");
                notifEvent.setMensaje("Hemos detectado un nuevo inicio de sesión en tu cuenta desde el dispositivo/navegador: " + userAgent + " con la IP: " + ip + ". Si no fuiste tú, por favor cambia tu contraseña inmediatamente.");
                try {
                    rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.routing.key", notifEvent);
                } catch (Exception e) {
                    System.err.println("Error al enviar alerta de seguridad por RabbitMQ: " + e.getMessage());
                }
            }
        }

        com.rapidocourier.servicio_auth.entity.HistorialAcceso historial = new com.rapidocourier.servicio_auth.entity.HistorialAcceso();
        historial.setUsuario(usuario);
        historial.setEmailIntentado(email);
        historial.setIpAddress(ip);
        historial.setUserAgent(userAgent);
        historial.setEstado(estado);
        historial.setMotivoFallo(motivo);
        historialAccesoRepository.save(historial);
    }
}
