package com.rapidocourier.servicio_notificaciones.controller;

import com.rapidocourier.servicio_notificaciones.dto.request.NotificacionRequest;
import com.rapidocourier.servicio_notificaciones.dto.response.NotificacionResponse;
import com.rapidocourier.servicio_notificaciones.service.NotificacionService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/notificaciones")
@RequiredArgsConstructor
@Tag(name = "Notificaciones", description = "API para la gestión y registro de notificaciones de paquetes")
public class NotificacionController {

    private final NotificacionService notificacionService;

    @Operation(
            summary = "Enviar notificación",
            description = "Registra y procesa una nueva notificación de estado para un paquete específico en el sistema."
    )
    @PostMapping("/enviar")
    public ResponseEntity<ApiResponse<NotificacionResponse>> enviarNotificacion(@RequestBody NotificacionRequest request) {
        NotificacionResponse response = notificacionService.enviarNotificacion(request);
        return ResponseEntity.ok(ApiResponse.ok("Notificación enviada correctamente", response));
    }

    @Operation(
            summary = "Listar notificaciones por paquete",
            description = "Obtiene todo el historial de notificaciones enviadas a un paquete específico."
    )
    @GetMapping("/paquete/{paqueteId}")
    public ResponseEntity<ApiResponse<List<NotificacionResponse>>> obtenerNotificacionesPorPaquete(
            @Parameter(description = "UUID del paquete", required = true)
            @PathVariable UUID paqueteId) {

        List<NotificacionResponse> notificaciones = notificacionService.obtenerPorPaquete(paqueteId);
        return ResponseEntity.ok(ApiResponse.ok("Historial de notificaciones recuperado", notificaciones));
    }
}