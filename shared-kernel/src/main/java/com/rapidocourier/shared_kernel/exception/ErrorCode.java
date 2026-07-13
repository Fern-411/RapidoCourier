package com.rapidocourier.shared_kernel.exception;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements CodigoError {

    // Estos códigos cubren exactamente lo que pide el RNF-02
    SOLICITUD_MAL_FORMADA("RC-400", "La solicitud contiene errores de formato", HttpStatus.BAD_REQUEST),
    RECURSO_NO_ENCONTRADO("RC-404", "El recurso solicitado no existe", HttpStatus.NOT_FOUND),
    CONFLICTO_ESTADO("RC-409", "Conflicto o transición de estado inválida", HttpStatus.CONFLICT),
    FALLO_EXTERNO("RC-502", "Error al comunicarse con un servicio externo (Ej. RENIEC)", HttpStatus.BAD_GATEWAY),
    ERROR_INTERNO("RC-500", "Ha ocurrido un error inesperado en el servidor", HttpStatus.INTERNAL_SERVER_ERROR);

    private final String codigo;
    private final String mensajeDefault;
    private final HttpStatus httpStatus;

    ErrorCode(String codigo, String mensajeDefault, HttpStatus httpStatus) {
        this.codigo = codigo;
        this.mensajeDefault = mensajeDefault;
        this.httpStatus = httpStatus;
    }
}