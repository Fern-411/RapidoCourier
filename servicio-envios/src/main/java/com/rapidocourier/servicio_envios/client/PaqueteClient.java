package com.rapidocourier.servicio_envios.client;

import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.math.BigDecimal;
import java.util.UUID;

@FeignClient(name = "servicio-paquetes")
public interface PaqueteClient {

    @GetMapping("/api/v1/paquetes/{id}")
    ApiResponse<PaqueteResponse> buscarPorId(@PathVariable("id") UUID id);

    @lombok.Data
    @com.fasterxml.jackson.annotation.JsonIgnoreProperties(ignoreUnknown = true)
    class PaqueteResponse {
        private UUID id;
        private BigDecimal pesoKg;
        private UUID remitenteId;
        private UUID destinatarioId;
        private String descripcion;
    }
}
