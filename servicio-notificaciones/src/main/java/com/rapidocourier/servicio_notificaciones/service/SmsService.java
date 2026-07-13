package com.rapidocourier.servicio_notificaciones.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;
import com.twilio.Twilio;
import com.twilio.rest.api.v2010.account.Message;
import com.twilio.type.PhoneNumber;

@Service
public class SmsService {

    private static final Logger logger = LoggerFactory.getLogger(SmsService.class);

    @Value("${twilio.account-sid:}")
    private String accountSid;

    @Value("${twilio.auth-token:}")
    private String authToken;

    @Value("${twilio.phone-number:}")
    private String fromNumber;

    @Retry(name = "smsService")
    public void enviarSms(String numero, String mensaje) {
        if (accountSid == null || accountSid.isEmpty() || authToken == null || authToken.isEmpty()) {
            logger.info("==========================================");
            logger.info("📱 SIMULANDO ENVÍO DE SMS (Faltan credenciales Twilio)");
            logger.info("A: {}", numero);
            logger.info("MENSAJE: {}", mensaje);
            logger.info("==========================================");
            return;
        }

        try {
            Twilio.init(accountSid, authToken);
            Message twilioMessage = Message.creator(
                    new PhoneNumber(numero),
                    new PhoneNumber(fromNumber),
                    mensaje
            ).create();
            logger.info("SMS enviado correctamente. SID: {}", twilioMessage.getSid());
        } catch (Exception e) {
            logger.error("Error al enviar SMS real con Twilio: {}", e.getMessage());
            throw new RuntimeException("Fallo al enviar SMS", e); // Throw exception to trigger Retry / DLQ
        }
    }
}
