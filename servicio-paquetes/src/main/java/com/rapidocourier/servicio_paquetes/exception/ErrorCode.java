package com.rapidocourier.servicio_paquetes.exception;

import com.rapidocourier.shared_kernel.exception.CodigoError;
import org.springframework.http.HttpStatus;

public enum ErrorCode implements CodigoError {

    // Errores existentes
    PAQUETE_NO_ENCONTRADO("PAQ_404", "El paquete solicitado no existe", HttpStatus.NOT_FOUND),
    DNI_NO_REGISTRADO("PAQ_409_1", "El DNI no está registrado en el sistema", HttpStatus.CONFLICT),
    TRANSICION_INVALIDA("PAQ_409_2", "Transición de estado no válida para este paquete", HttpStatus.CONFLICT),
    PAGO_NO_COMPLETADO("PAQ_409_3", "El pago no ha sido completado para este paquete", HttpStatus.CONFLICT),
    ERROR_COMUNICACION_CLIENTES("PAQ_502_1", "Error al comunicar con servicio-clientes", HttpStatus.BAD_GATEWAY),
    ERROR_COMUNICACION_PAGOS("PAQ_502_2", "Error al comunicar con servicio-pagos", HttpStatus.BAD_GATEWAY),

    // AGREGA ESTOS DOS QUE TE FALTAN:
    RECURSO_NO_ENCONTRADO("RC-404", "El recurso solicitado no existe", HttpStatus.NOT_FOUND),
    FALLO_EXTERNO("RC-502", "Error al comunicarse con un servicio externo", HttpStatus.BAD_GATEWAY);

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