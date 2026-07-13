package com.rapidocourier.servicio_envios.client;

import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "servicio-pagos")
public interface PagoClient {

    @GetMapping("/api/v1/pagos/paquete/{paqueteId}")
    ApiResponse<PagoResponse> buscarPorPaqueteId(@PathVariable("paqueteId") UUID paqueteId);

    @GetMapping("/api/v1/pagos/verificar")
    ApiResponse<Boolean> verificarPagoCompletado(@RequestParam("paqueteId") UUID paqueteId);

    record PagoResponse(
            UUID id,
            BigDecimal monto,
            String estadoPago
    ) {}
}
