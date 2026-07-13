package com.rapidocourier.servicio_notificaciones.service;

import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import io.github.resilience4j.retry.annotation.Retry;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;
    
    @Value("${app.frontend.url}")
    private String frontendUrl;

    @Retry(name = "emailService")
    public void enviarCorreoBienvenida(String destino, String nombre) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject("🚀 ¡Bienvenido a Rapido Courier!");

            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; color: #1e293b;'>"
                    + "  <h2 style='color: #0f172a; border-bottom: 2px solid #ea580c; padding-bottom: 10px;'>¡Bienvenido a Rapido Courier!</h2>"
                    + "  <p>Hola <strong>" + (nombre != null ? nombre : "") + "</strong>,</p>"
                    + "  <p>Tu cuenta ha sido creada y verificada exitosamente. Gracias por unirte a la plataforma logística más rápida del país.</p>"
                    + "  <p>Ya puedes empezar a enviar paquetes y hacer seguimiento en tiempo real de tus envíos.</p>"
                    + "  <div style='text-align: center; margin: 30px 0;'>"
                    + "    <a href='" + frontendUrl + "' style='background-color: #ea580c; color: #ffffff; padding: 12px 24px; text-decoration: none; font-weight: bold; border-radius: 6px; display: inline-block; box-shadow: 0 4px 6px -1px rgba(234, 88, 12, 0.2);'>Ir a mi Dashboard</a>"
                    + "  </div>"
                    + "  <p style='font-size: 13px; color: #64748b; margin-top: 30px;'>Si necesitas ayuda, no dudes en contactar a nuestro equipo de soporte.</p>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(mimeMessage);

            log.info("[EmailService] Correo HTML de bienvenida enviado a: {}", destino);
        } catch (Exception e) {
            log.error("[EmailService] Fallo al enviar correo de bienvenida a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Fallo al enviar correo de bienvenida", e);
        }
    }

    @Retry(name = "emailService")
    public void enviarCodigoVerificacion(String destino, String codigo) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject("🔒 Tu Código de Verificación - Rapido Courier");

            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; color: #1e293b;'>"
                    + "  <h2 style='color: #0f172a; border-bottom: 2px solid #ea580c; padding-bottom: 10px;'>Verificación de Seguridad</h2>"
                    + "  <p>Hola,</p>"
                    + "  <p>Hemos recibido una solicitud que requiere validación de seguridad en <strong>Rapido Courier</strong>.</p>"
                    + "  <p>Tu código de verificación de 6 dígitos es el siguiente:</p>"
                    + "  <div style='text-align: center; background-color: #f8fafc; padding: 20px; border: 2px dashed #ea580c; border-radius: 8px; margin: 30px 0;'>"
                    + "    <span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #ea580c;'>" + codigo + "</span>"
                    + "  </div>"
                    + "  <p style='font-size: 13px; color: #ef4444; font-weight: bold;'>⏳ Este código expirará en 10 minutos.</p>"
                    + "  <hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;'>"
                    + "  <p style='font-size: 12px; color: #94a3b8;'>Si no realizaste esta solicitud, puedes ignorar este correo. Tu cuenta sigue segura.</p>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(mimeMessage);

            log.info("[EmailService] Código OTP enviado a: {}", destino);
        } catch (Exception e) {
            log.error("[EmailService] Fallo asíncrono al enviar OTP a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Fallo al enviar OTP", e);
        }
    }
    @Retry(name = "emailService")
    public void enviarCorreoVerificacion(String destino, String codigo, String nombre) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject("✅ Verifica tu cuenta - Rapido Courier");

            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; color: #1e293b;'>"
                    + "  <h2 style='color: #0f172a; border-bottom: 2px solid #ea580c; padding-bottom: 10px;'>¡Casi listo para empezar!</h2>"
                    + "  <p>Hola <strong>" + (nombre != null ? nombre : "") + "</strong>,</p>"
                    + "  <p>Gracias por registrarte en Rapido Courier. Para activar tu cuenta, ingresa el siguiente código de verificación en la aplicación:</p>"
                    + "  <div style='text-align: center; background-color: #f8fafc; padding: 20px; border: 2px dashed #ea580c; border-radius: 8px; margin: 30px 0;'>"
                    + "    <span style='font-size: 32px; font-weight: bold; letter-spacing: 5px; color: #ea580c;'>" + codigo + "</span>"
                    + "  </div>"
                    + "  <p style='font-size: 13px; color: #64748b; margin-top: 30px;'>Si no solicitaste crear una cuenta, puedes ignorar este correo.</p>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(mimeMessage);

            log.info("[EmailService] Correo de verificación enviado a: {}", destino);
        } catch (Exception e) {
            log.error("[EmailService] Fallo al enviar correo de verificación a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Fallo al enviar correo", e); // Trigger DLQ
        }
    }

    @Retry(name = "emailService")
    public void enviarCorreoAlertaSeguridad(String destino, String mensaje, String asunto) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject(asunto != null ? asunto : "⚠️ Alerta de Seguridad - Rapido Courier");

            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; color: #1e293b;'>"
                    + "  <h2 style='color: #0f172a; border-bottom: 2px solid #ef4444; padding-bottom: 10px;'>Alerta de Seguridad</h2>"
                    + "  <p>Hola,</p>"
                    + "  <p>" + mensaje + "</p>"
                    + "  <hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;'>"
                    + "  <p style='font-size: 12px; color: #94a3b8;'>Este es un mensaje automático de seguridad. No respondas a este correo.</p>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(mimeMessage);

            log.info("[EmailService] Correo de alerta de seguridad enviado a: {}", destino);
        } catch (Exception e) {
            log.error("[EmailService] Fallo al enviar correo de alerta a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Fallo al enviar alerta", e);
        }
    }

    @Retry(name = "emailService")
    public void enviarPinPaquete(String destino, String pin) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject("📦 ¡Tu paquete está listo para recoger! - Rapido Courier");

            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; color: #1e293b; text-align: center;'>"
                    + "  <h2 style='color: #f59e0b; border-bottom: 2px solid #f59e0b; padding-bottom: 10px;'>¡Tu paquete ha llegado!</h2>"
                    + "  <p>Hola,</p>"
                    + "  <p>Te informamos que tu paquete ya se encuentra en la agencia de destino y está listo para que pases a recogerlo.</p>"
                    + "  <div style='background-color: #f8fafc; padding: 15px; border-radius: 8px; margin: 20px 0;'>"
                    + "    <p style='font-size: 16px; margin: 0;'>Tu código de seguridad (PIN) es:</p>"
                    + "    <h1 style='color: #0f172a; margin: 10px 0; letter-spacing: 5px; font-size: 36px;'>" + pin + "</h1>"
                    + "  </div>"
                    + "  <p style='color: #ef4444; font-weight: bold;'>⚠️ IMPORTANTE: No compartas este PIN con nadie.</p>"
                    + "  <p>Acércate a la ventanilla con tu documento de identidad y brinda este PIN para que te entreguen el paquete.</p>"
                    + "  <hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;'>"
                    + "  <p style='font-size: 12px; color: #94a3b8;'>Gracias por confiar en Rapido Courier. Este es un mensaje automático.</p>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(mimeMessage);

            log.info("[EmailService] Correo de PIN enviado a: {}", destino);
        } catch (Exception e) {
            log.error("[EmailService] Fallo al enviar PIN a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Fallo al enviar PIN", e);
        }
    }

    @Retry(name = "emailService")
    public void enviarCorreoBoleta(String destino, String urlBoleta) {
        try {
            MimeMessage mimeMessage = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(mimeMessage, true, "UTF-8");

            helper.setTo(destino);
            helper.setSubject("📄 Tu Boleta de Venta Electrónica - Rapido Courier");

            String cuerpoHtml = "<div style='font-family: Arial, sans-serif; max-width: 600px; margin: 0 auto; padding: 20px; border: 1px solid #e2e8f0; border-radius: 8px; color: #1e293b; text-align: center;'>"
                    + "  <h2 style='color: #2563eb; border-bottom: 2px solid #2563eb; padding-bottom: 10px;'>¡Gracias por usar Rápido Courier!</h2>"
                    + "  <p>Hola,</p>"
                    + "  <p>Hemos procesado tu envío exitosamente. Puedes descargar tu boleta de venta electrónica haciendo clic en el siguiente enlace:</p>"
                    + "  <div style='margin: 30px 0;'>"
                    + "    <a href='" + urlBoleta + "' style='background-color: #2563eb; color: white; padding: 12px 24px; text-decoration: none; border-radius: 6px; font-weight: bold; display: inline-block;'>Descargar Boleta PDF</a>"
                    + "  </div>"
                    + "  <p style='font-size: 14px; color: #475569;'>Si el botón no funciona, copia y pega este enlace en tu navegador:</p>"
                    + "  <p style='font-size: 12px; color: #94a3b8; word-break: break-all;'>" + urlBoleta + "</p>"
                    + "  <hr style='border: 0; border-top: 1px solid #e2e8f0; margin: 20px 0;'>"
                    + "  <p style='font-size: 12px; color: #94a3b8;'>Este es un mensaje automático. Por favor, no respondas a este correo.</p>"
                    + "</div>";

            helper.setText(cuerpoHtml, true);
            mailSender.send(mimeMessage);

            log.info("[EmailService] Correo con boleta enviado a: {}", destino);
        } catch (Exception e) {
            log.error("[EmailService] Fallo al enviar boleta a {}: {}", destino, e.getMessage());
            throw new RuntimeException("Fallo al enviar boleta", e);
        }
    }
}
