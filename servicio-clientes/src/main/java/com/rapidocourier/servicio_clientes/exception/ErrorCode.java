package com.rapidocourier.servicio_clientes.exception;

import com.rapidocourier.shared_kernel.exception.CodigoError;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements CodigoError {

    CLIENTE_NO_ENCONTRADO("CLI_404", "El cliente solicitado no existe", HttpStatus.NOT_FOUND),
    EMAIL_YA_REGISTRADO("CLI_409_1", "El email ya está registrado", HttpStatus.CONFLICT),
    DNI_YA_REGISTRADO("CLI_409_2", "El DNI ya está registrado", HttpStatus.CONFLICT),
    RENIEC_RESPUESTA_VACIA("CLI_502_1", "Respuesta vacía del servicio RENIEC", HttpStatus.BAD_GATEWAY),
    RENIEC_FALLO_COMUNICACION("CLI_502_2", "Fallo al comunicar con RENIEC", HttpStatus.BAD_GATEWAY),
    RENIEC_NO_DISPONIBLE("CLI_503", "Servicio RENIEC no disponible. Intente más tarde.", HttpStatus.SERVICE_UNAVAILABLE);

    private final String codigo;
    private final String mensajeDefault;
    private final HttpStatus httpStatus;

    ErrorCode(String codigo, String mensajeDefault, HttpStatus httpStatus) {
        this.codigo = codigo;
        this.mensajeDefault = mensajeDefault;
        this.httpStatus = httpStatus;
    }

    @Override
    public String getCodigo() { return codigo; }

    @Override
    public String getMensajeDefault() { return mensajeDefault; }

    @Override
    public HttpStatus getHttpStatus() { return httpStatus; }
}