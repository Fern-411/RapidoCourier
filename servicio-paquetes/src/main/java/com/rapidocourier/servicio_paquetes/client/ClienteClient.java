package com.rapidocourier.servicio_paquetes.client;

import com.rapidocourier.servicio_paquetes.dto.response.ClienteResponse;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;


@FeignClient(name = "servicio-clientes")
public interface ClienteClient {

    @GetMapping("/api/v1/clientes/dni/{dni}")
    ApiResponse<ClienteResponse> buscarPorDni(@PathVariable("dni") String dni);

    @GetMapping("/api/v1/clientes/{id}")
    ApiResponse<ClienteResponse> buscarPorId(@PathVariable("id") java.util.UUID id);

    @org.springframework.web.bind.annotation.PostMapping("/api/v1/clientes/receptor")
    ApiResponse<ClienteResponse> crearReceptor(@org.springframework.web.bind.annotation.RequestParam("dni") String dni);
}

