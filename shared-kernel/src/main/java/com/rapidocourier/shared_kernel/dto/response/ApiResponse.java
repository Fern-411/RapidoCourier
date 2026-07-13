package com.rapidocourier.shared_kernel.dto.response;

import com.fasterxml.jackson.annotation.JsonInclude;
import java.time.Instant;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record ApiResponse<T>(
        boolean success,
        String message,
        T data,
        ErrorDetail error,
        Instant timestamp
) {

    // ── MÉTODOS DE FÁBRICA PARA ÉXITO ────────────────────────────────────────

    public static <T> ApiResponse<T> ok(T data) {
        return new ApiResponse<>(true, "Operación exitosa", data, null, Instant.now());
    }

    public static <T> ApiResponse<T> ok(String message, T data) {
        return new ApiResponse<>(true, message, data, null, Instant.now());
    }

    // ── MÉTODOS DE FÁBRICA PARA ERRORES ──────────────────────────────────────

    // 1. Método básico (Mantiene compatibilidad con código antiguo)
    public static <T> ApiResponse<T> error(String message) {
        return new ApiResponse<>(false, message, null, null, Instant.now());
    }

    // 2. Método con código de error (Estilo moderno)
    public static <T> ApiResponse<T> error(String code, String message) {
        return new ApiResponse<>(false, "Operación fallida", null, new ErrorDetail(code, message, null, null), Instant.now());
    }

    // 3. Método para errores con trazabilidad
    public static <T> ApiResponse<T> error(String code, String message, String correlationId) {
        return new ApiResponse<>(false, "Operación fallida", null, new ErrorDetail(code, message, correlationId, null), Instant.now());
    }

    // 4. Método para errores de validación (El más importante para GlobalExceptionHandler)
    public static <T> ApiResponse<T> errorValidacion(String code, String message, Object validationDetails) {
        return new ApiResponse<>(false, "Error de validación", null, new ErrorDetail(code, message, null, validationDetails), Instant.now());
    }

    // ── RECORD INTERNO PARA EL DETALLE DEL ERROR ─────────────────────────────

    @JsonInclude(JsonInclude.Include.NON_NULL)
    public record ErrorDetail(
            String code,
            String message,
            String correlationId,
            Object details
    ) {}
}