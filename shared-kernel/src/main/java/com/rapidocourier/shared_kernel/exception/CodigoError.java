package com.rapidocourier.shared_kernel.exception;

import org.springframework.http.HttpStatus;

public interface CodigoError {
    String getCodigo();
    String getMensajeDefault();
    HttpStatus getHttpStatus();
}