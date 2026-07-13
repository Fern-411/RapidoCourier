package com.rapidocourier.servicio_notificaciones.exception;

import com.rapidocourier.shared_kernel.exception.CodigoError;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements CodigoError {

    PAQUETE_ID_NULO("NOT_400", "El ID del paquete es obligatorio", HttpStatus.BAD_REQUEST),
    MENSAJE_VACIO("NOT_400", "El mensaje de la notificación no puede estar vacío", HttpStatus.BAD_REQUEST),
    ERROR_ENVIO("NOT_500", "Error interno al procesar la notificación", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String codigo;
    private final String mensajeDefault;
    private final HttpStatus httpStatus;

    ErrorCode(String codigo, String mensajeDefault, HttpStatus httpStatus) {
        this.codigo = codigo;
        this.mensajeDefault = mensajeDefault;
        this.httpStatus = httpStatus;
    }

    @Override public String getCodigo() { return codigo; }
    @Override public String getMensajeDefault() { return mensajeDefault; }
    @Override public HttpStatus getHttpStatus() { return httpStatus; }
}