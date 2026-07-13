package com.rapidocourier.servicio_paquetes.controller;

import com.rapidocourier.servicio_paquetes.dto.request.PaqueteRequest;
import com.rapidocourier.servicio_paquetes.dto.response.PaqueteResponse;
import com.rapidocourier.servicio_paquetes.service.PaqueteService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/paquetes")
@Tag(name = "Paquetes", description = "Endpoints para la gestión de paquetes físicos")
public class PaqueteController {

    private final PaqueteService paqueteService;

    @Operation(summary = "Registrar nuevo paquete", description = "Crea el registro de un paquete físico con sus dimensiones y peso.")
    @PostMapping
    public ResponseEntity<ApiResponse<PaqueteResponse>> registrarPaquete(@Valid @RequestBody PaqueteRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Paquete registrado", paqueteService.registrarPaquete(request)));
    }

    @Operation(summary = "Buscar paquetes con filtros", description = "Permite buscar paquetes por un rango de fechas.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<PaqueteResponse>>> buscarPaquetes(
            @Parameter(description = "Fecha inicial (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaInicio,
            @Parameter(description = "Fecha límite (ISO-8601)") @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) LocalDateTime fechaFin) {
        List<PaqueteResponse> response = paqueteService.buscar(fechaInicio, fechaFin);
        return ResponseEntity.ok(ApiResponse.ok("Resultados de búsqueda con filtros", response));
    }

    @Operation(summary = "Buscar por ID", description = "Obtiene los detalles físicos de un paquete por su ID.")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<PaqueteResponse>> buscarPorId(
            @Parameter(description = "UUID del paquete") @PathVariable UUID id) {
        PaqueteResponse response = paqueteService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Paquete encontrado", response));
    }

    @Operation(summary = "Historial por remitente", description = "Recupera todos los paquetes enviados por un cliente específico.")
    @GetMapping("/remitente/{remitenteId}")
    public ResponseEntity<ApiResponse<List<PaqueteResponse>>> buscarPorRemitenteId(
            @Parameter(description = "UUID del cliente remitente") @PathVariable UUID remitenteId) {
        List<PaqueteResponse> response = paqueteService.buscarPorRemitenteId(remitenteId);
        return ResponseEntity.ok(ApiResponse.ok("Historial de paquetes del remitente", response));
    }

    @Operation(summary = "Agregar categorías", description = "Añade etiquetas de clasificación a un paquete existente.")
    @PostMapping("/{id}/categorias")
    public ResponseEntity<ApiResponse<PaqueteResponse>> agregarCategorias(
            @Parameter(description = "UUID del paquete") @PathVariable UUID id,
            @RequestBody List<String> categorias) {
        PaqueteResponse response = paqueteService.agregarCategorias(id, categorias);
        return ResponseEntity.ok(ApiResponse.ok("Categorías agregadas", response));
    }

    @Operation(summary = "Eliminar paquete", description = "Elimina físicamente un paquete de la base de datos.")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarPaquete(
            @Parameter(description = "UUID del paquete") @PathVariable UUID id) {
        paqueteService.eliminarPaquete(id);
        return ResponseEntity.ok(ApiResponse.ok("Paquete eliminado exitosamente", null));
    }
}