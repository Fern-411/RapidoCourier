package com.rapidocourier.servicio_auth.strategy;

import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.shared_kernel.event.NotificacionEvent;
import com.rapidocourier.shared_kernel.exception.BaseException;
import com.rapidocourier.servicio_auth.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class EmailOtpSenderStrategy implements OtpSenderStrategy {
    
    private final RabbitTemplate rabbitTemplate;
    
    @Override
    public void sendOtp(Usuario usuario, String codigo) {
        NotificacionEvent event = new NotificacionEvent();
        event.setAsunto("Código de Verificación - Rapido Courier");
        event.setMensaje("Tu código de verificación es: " + codigo + ". Expirará en 10 minutos.");
        event.setDestinatario(usuario.getEmail());
        event.setTipoNotificacion("VERIFICACION_EMAIL");
        
        try {
            rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.routing.key", event);
        } catch (Exception e) {
            System.err.println("Error al enviar OTP por email vía RabbitMQ: " + e.getMessage());
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
    }

    @Override
    public String getChannel() {
        return "EMAIL";
    }
}
