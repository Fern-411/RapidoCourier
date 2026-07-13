package com.rapidocourier.servicio_envios.client;

import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.UUID;

@FeignClient(name = "servicio-clientes")
public interface ClienteClient {

    @GetMapping("/api/v1/clientes/{id}")
    ApiResponse<ClienteResponse> buscarPorId(@PathVariable("id") UUID id);
    
    @GetMapping("/api/v1/clientes/dni/{dni}")
    ApiResponse<ClienteResponse> buscarPorDni(@PathVariable("dni") String dni);
    
    record ClienteResponse(
            UUID id,
            String dni,
            String nombreCompleto,
            String email,
            String telefono
    ) {}
}
