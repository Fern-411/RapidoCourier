package com.rapidocourier.servicio_auth.listener;

import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.repository.UsuarioRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class SagaCompensacionListener {

    private final UsuarioRepository usuarioRepository;

    @RabbitListener(queues = "saga.auth.rollback.queue", ackMode = "AUTO")
    public void handleRollbackEvent(RollbackEvent event) {
        System.out.println("Recibido evento de compensación (Saga): " + event.getReason() + " para el usuario: " + event.getEmail());
        
        usuarioRepository.findByEmail(event.getEmail()).ifPresent(usuario -> {
            System.out.println("Desactivando usuario debido a fallo en transacción distribuida.");
            usuario.setIsEmailVerified(false);
            usuario.setIsPhoneVerified(false);
            // También se podría eliminar físicamente el usuario dependiendo de la regla de negocio
            // usuarioRepository.delete(usuario);
            usuarioRepository.save(usuario);
        });
    }

    @lombok.Data
    public static class RollbackEvent {
        private String email;
        private String reason;
    }
}
