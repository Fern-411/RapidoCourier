package com.rapidocourier.servicio_clientes.client;

import com.rapidocourier.servicio_clientes.dto.response.ReniecResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;

@FeignClient(name = "reniec-client", url = "https://api.decolecta.com")
public interface ReniecClient {

    @GetMapping(value = "/v1/reniec/dni")
    ReniecResponse getNombreCompleto(
            @RequestParam("numero") String dni,
            @RequestHeader("Authorization") String token);
}