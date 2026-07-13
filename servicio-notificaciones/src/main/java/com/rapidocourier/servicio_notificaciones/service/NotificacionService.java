package com.rapidocourier.servicio_notificaciones.service;

import com.rapidocourier.servicio_notificaciones.dto.request.NotificacionRequest;
import com.rapidocourier.servicio_notificaciones.dto.response.NotificacionResponse;
import com.rapidocourier.servicio_notificaciones.entity.Notificacion;
import com.rapidocourier.servicio_notificaciones.exception.ErrorCode;
import com.rapidocourier.servicio_notificaciones.repository.NotificacionRepository;
import com.rapidocourier.shared_kernel.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class NotificacionService {

    private static final Logger logger = LoggerFactory.getLogger(NotificacionService.class);
    private final NotificacionRepository notificacionRepository;
    private final EmailService emailService;
    private final SmsService smsService;
    private final SimpMessagingTemplate messagingTemplate;

    @org.springframework.amqp.rabbit.annotation.RabbitListener(queues = com.rapidocourier.servicio_notificaciones.config.RabbitMQConfig.QUEUE_NOTIFICACIONES)
    @Transactional
    public void procesarEventoNotificacion(com.rapidocourier.shared_kernel.event.NotificacionEvent event) {
        logger.info("Recibido evento completo: {}", event);
        if ("OTP_AUTH".equals(event.getTipoNotificacion())) {
            logger.info("Procesando evento OTP_AUTH para: {}", event.getDestinatario());
            // El mensaje trae algo como "Tu código de verificación es: 123456. Expirará en 10 minutos."
            // Extraeremos solo los dígitos del código (asumiendo formato estándar de 6 dígitos)
            String codigo = event.getMensaje().replaceAll("\\D+", "");
            if (codigo.length() > 6) {
                codigo = codigo.substring(0, 6);
            }
            if (event.getDestinatario() != null && event.getDestinatario().contains("@")) {
                emailService.enviarCodigoVerificacion(event.getDestinatario(), codigo);
            } else if (event.getDestinatario() != null) {
                smsService.enviarSms(event.getDestinatario(), "Tu código de verificación es: " + codigo);
            }
            return;
        }
        if ("WELCOME_EMAIL".equals(event.getTipoNotificacion())) {
            logger.info("Procesando evento WELCOME_EMAIL para: {}", event.getDestinatario());
            emailService.enviarCorreoBienvenida(event.getDestinatario(), event.getAsunto()); // Usaremos Asunto para pasar el Nombre
            return;
        }

        if ("BOLETA_EMAIL".equals(event.getTipoNotificacion())) {
            logger.info("Procesando evento BOLETA_EMAIL para: {}", event.getDestinatario());
            emailService.enviarCorreoBoleta(event.getDestinatario(), event.getMensaje()); 
            return;
        }

        if ("VERIFICACION_EMAIL".equals(event.getTipoNotificacion())) {
            logger.info("Procesando evento VERIFICACION_EMAIL para: {}", event.getDestinatario());
            emailService.enviarCorreoVerificacion(event.getDestinatario(), event.getMensaje(), event.getAsunto()); 
            
            if (event.getNumeroContacto() != null && !event.getNumeroContacto().isEmpty()) {
                smsService.enviarSms(event.getNumeroContacto(), "Tu código de verificación Rapido Courier es: " + event.getMensaje());
            }
            return;
        }

        if ("ALERTA_SEGURIDAD".equals(event.getTipoNotificacion())) {
            logger.info("Procesando evento ALERTA_SEGURIDAD para: {}", event.getDestinatario());
            emailService.enviarCorreoAlertaSeguridad(event.getDestinatario(), event.getMensaje(), event.getAsunto());
            return;
        }

        if ("PAQUETE_PIN".equals(event.getTipoNotificacion())) {
            logger.info("Procesando evento PAQUETE_PIN para: {}", event.getDestinatario());
            // Extraer el PIN (asumiendo que el mensaje dice "Tu PIN es: 123456")
            String pin = event.getMensaje().replaceAll("\\D+", "");
            emailService.enviarPinPaquete(event.getDestinatario(), pin);
            // No hacemos return aquí para que la notificación se guarde en BD y se envíe por WebSocket
        }

        logger.info("Recibido evento de notificación para paquete {}: {}", event.getPaqueteId(), event.getMensaje());
        NotificacionRequest request = new NotificacionRequest(event.getPaqueteId(), event.getMensaje());
        enviarNotificacion(request);
    }
    @Transactional
    public NotificacionResponse enviarNotificacion(NotificacionRequest request) {
        if (request.paqueteId() == null) throw new BaseException(ErrorCode.PAQUETE_ID_NULO);
        if (request.mensaje() == null || request.mensaje().trim().isEmpty()) throw new BaseException(ErrorCode.MENSAJE_VACIO);

        Notificacion notificacion = new Notificacion();
        notificacion.setPaqueteId(request.paqueteId());
        notificacion.setMensaje(request.mensaje());
        notificacion.setEstado("ENVIADA");

        Notificacion guardada = notificacionRepository.save(notificacion);

        NotificacionResponse response = new NotificacionResponse(
                guardada.getId(),
                guardada.getPaqueteId(),
                guardada.getMensaje(),
                guardada.getEstado(),
                guardada.getFechaEnvio()
        );

        // Emitir evento por WebSocket al frontend
        messagingTemplate.convertAndSend("/topic/paquetes/" + request.paqueteId(), response);

        return response;
    }
    @Transactional
    public List<NotificacionResponse> obtenerPorPaquete(UUID paqueteId) {
        return notificacionRepository.findByPaqueteIdOrderByFechaEnvioDesc(paqueteId)
                .stream()
                .map(n -> new NotificacionResponse(
                        n.getId(),
                        n.getPaqueteId(),
                        n.getMensaje(),
                        n.getEstado(),
                        n.getFechaEnvio()
                ))
                .toList();
    }
}