package com.rapidocourier.servicio_pagos.exception;

import com.rapidocourier.shared_kernel.exception.CodigoError;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements CodigoError {

    PAGO_YA_PROCESADO("PAG_409", "El pago para este paquete ya ha sido procesado anteriormente", HttpStatus.CONFLICT),
    PAGO_NO_ENCONTRADO("PAG_404", "No se encontró registro de pago para este paquete", HttpStatus.NOT_FOUND),
    MONTO_INVALIDO("PAG_400", "El monto ingresado no es válido", HttpStatus.BAD_REQUEST),
    ERROR_PROCESAMIENTO("PAG_500", "Error interno al procesar el pago", HttpStatus.INTERNAL_SERVER_ERROR);

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