package com.rapidocourier.servicio_pagos.controller;

import com.rapidocourier.servicio_pagos.dto.request.PagoRequest;
import com.rapidocourier.servicio_pagos.dto.response.PagoResponse;
import com.rapidocourier.servicio_pagos.service.PagoService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/pagos")
@RequiredArgsConstructor
@Tag(name = "Pagos", description = "API para el procesamiento y verificación de pagos")
public class PagoController {

    private final PagoService pagoService;

    @Operation(summary = "Procesar pago", description = "Registra un pago usando un objeto JSON.")
    @PostMapping("/procesar")
    public ResponseEntity<?> procesarPago(@RequestBody PagoRequest request) {
        System.out.println("Procesando pago para paquete: " + request.paqueteId());
        PagoResponse response = pagoService.procesarPago(request.paqueteId(), request.monto());
        return ResponseEntity.ok(ApiResponse.ok("Pago procesado con exito", response));
    }

    @Operation(summary = "Verificar estado de pago", description = "Consulta si el pago está COMPLETADO.")
    @GetMapping("/verificar")
    public ResponseEntity<ApiResponse<Boolean>> verificarPagoCompletado(@RequestParam UUID paqueteId) {
        boolean completado = pagoService.verificarPagoCompletado(paqueteId);
        return ResponseEntity.ok(ApiResponse.ok("Verificacion de pago", completado));
    }

    @Operation(summary = "Buscar pago por ID de paquete")
    @GetMapping("/paquete/{paqueteId}")
    public ResponseEntity<ApiResponse<PagoResponse>> buscarPorPaqueteId(@PathVariable UUID paqueteId) {
        return ResponseEntity.ok(ApiResponse.ok("Pago encontrado", pagoService.buscarPorPaqueteId(paqueteId)));
    }

    @Operation(summary = "Estadísticas de ingresos", description = "Devuelve el total de ingresos cobrados hoy y en el mes actual.")
    @GetMapping("/estadisticas/ingresos")
    public ResponseEntity<ApiResponse<com.rapidocourier.servicio_pagos.dto.response.EstadisticasPagoResponse>> obtenerEstadisticas() {
        return ResponseEntity.ok(ApiResponse.ok("Estadísticas de ingresos obtenidas", pagoService.obtenerEstadisticas()));
    }
}