package com.rapidocourier.servicio_notificaciones.listener;

import com.rapidocourier.shared_kernel.event.NotificacionEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class NotificacionDlqListener {

    @RabbitListener(queues = "notificaciones.dlq.queue")
    public void processFailedNotification(NotificacionEvent event) {
        log.error("¡ALERTA CRÍTICA! Notificación fallida interceptada en DLQ.");
        log.error("Destinatario afectado: {}", event.getDestinatario());
        log.error("Tipo de Notificación: {}", event.getTipoNotificacion());
        log.error("Asunto: {}", event.getAsunto());
        
        // Aquí en el futuro se puede agregar lógica para:
        // 1. Guardar en base de datos para reintento manual.
        // 2. Enviar una alerta a Slack/Discord para el equipo de soporte.
        // 3. Reencolar a la cola principal con un delay si fue un error temporal.
    }
}
