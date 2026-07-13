package com.rapidocourier.servicio_paquetes.client;

import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.scheduling.annotation.Async;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@FeignClient(name = "servicio-notificaciones")
public interface NotificacionesClient {
    
    // Anotado con @Async para fire-and-forget si Feign lo soporta (requiere proxy, normalmente se llama desde un servicio @Async)
    @PostMapping("/api/v1/notificaciones/enviar")
    ApiResponse<Void> enviarNotificacion(
            @RequestParam("paqueteId") UUID paqueteId,
            @RequestParam("mensaje") String mensaje);
}
