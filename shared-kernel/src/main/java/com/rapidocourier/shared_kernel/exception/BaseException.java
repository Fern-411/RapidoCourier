package com.rapidocourier.shared_kernel.exception;
import lombok.Getter;


@Getter
public class BaseException extends RuntimeException {
    private final CodigoError codigoError;
    private final Object[] args;

    public BaseException(CodigoError codigoError, Object... args) {
        super(codigoError.getMensajeDefault());
        this.codigoError = codigoError;
        this.args = args;
    }
}