package com.rapidocourier.servicio_envios.exception;

import com.rapidocourier.shared_kernel.exception.CodigoError;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements CodigoError {
    AGENCIA_NO_ENCONTRADA("ENV_001", "La agencia no fue encontrada", HttpStatus.NOT_FOUND),
    AGENCIA_YA_EXISTE("ENV_002", "La agencia ya existe", HttpStatus.BAD_REQUEST),
    ENVIO_NO_ENCONTRADO("ENV_003", "El envío no fue encontrado", HttpStatus.NOT_FOUND),
    PAQUETE_YA_TIENE_ENVIO("ENV_004", "El paquete ya tiene un envío registrado", HttpStatus.BAD_REQUEST),
    ENVIO_YA_ENTREGADO("ENV_005", "El envío ya fue entregado", HttpStatus.BAD_REQUEST),
    ESTADO_INVALIDO_PARA_ENTREGA("ENV_006", "El envío no está en la agencia destino", HttpStatus.BAD_REQUEST),
    PIN_INVALIDO("ENV_007", "El PIN de recojo es inválido", HttpStatus.BAD_REQUEST),
    ACCESO_DENEGADO("ENV_008", "El DNI proporcionado no coincide con el destinatario", HttpStatus.FORBIDDEN),
    RECOJO_BLOQUEADO("ENV_009", "El paquete está bloqueado por múltiples intentos fallidos", HttpStatus.LOCKED),
    OTP_INVALIDO("ENV_010", "El código de recuperación es inválido o expiró", HttpStatus.BAD_REQUEST),
    PERMISO_DENEGADO("ENV_011", "No tienes permisos para realizar esta acción", HttpStatus.FORBIDDEN),
    PAGO_PENDIENTE("ENV_012", "El paquete no ha sido pagado. Por favor, realice el pago antes de entregar.", HttpStatus.PAYMENT_REQUIRED),
    ERROR_INTERNO("ENV_500", "Error interno del servidor", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;

    ErrorCode(String code, String message, HttpStatus httpStatus) {
        this.code = code;
        this.message = message;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCodigo() { return code; }
    @Override
    public String getMensajeDefault() { return message; }
    @Override
    public HttpStatus getHttpStatus() { return httpStatus; }
}
