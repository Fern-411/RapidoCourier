package com.rapidocourier.servicio_paquetes.client;

import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "servicio-pagos")
public interface PagosClient {
    
    @GetMapping("/api/v1/pagos/verificar/{paqueteId}")
    ApiResponse<Boolean> verificarPagoCompletado(@PathVariable("paqueteId") UUID paqueteId);
}
