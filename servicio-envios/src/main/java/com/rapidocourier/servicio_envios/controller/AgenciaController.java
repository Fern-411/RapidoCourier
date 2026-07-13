package com.rapidocourier.servicio_envios.controller;

import com.rapidocourier.servicio_envios.dto.request.AgenciaRequest;
import com.rapidocourier.servicio_envios.dto.response.AgenciaResponse;
import com.rapidocourier.servicio_envios.service.AgenciaService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/agencias")
@RequiredArgsConstructor
@Tag(name = "Agencias", description = "Gestión de Agencias")
public class AgenciaController {
    private final AgenciaService agenciaService;

    @Operation(summary = "Crear agencia")
    @PostMapping
    public ResponseEntity<ApiResponse<AgenciaResponse>> crearAgencia(
            @Valid @RequestBody AgenciaRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {
        validarRolAdmin(rol);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Agencia creada", agenciaService.crearAgencia(request)));
    }

    @Operation(summary = "Obtener todas las agencias")
    @GetMapping
    public ResponseEntity<ApiResponse<List<AgenciaResponse>>> obtenerTodas() {
        return ResponseEntity.ok(ApiResponse.ok("Agencias encontradas", agenciaService.obtenerTodas()));
    }

    @Operation(summary = "Obtener agencia por ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AgenciaResponse>> obtenerPorId(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Agencia encontrada", agenciaService.obtenerPorId(id)));
    }

    @Operation(summary = "Actualizar agencia")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<AgenciaResponse>> actualizarAgencia(
            @PathVariable UUID id,
            @Valid @RequestBody AgenciaRequest request,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {
        validarRolAdmin(rol);
        return ResponseEntity.ok(ApiResponse.ok("Agencia actualizada", agenciaService.actualizarAgencia(id, request)));
    }

    @Operation(summary = "Eliminar agencia")
    @DeleteMapping("/{id}")
    public ResponseEntity<ApiResponse<Void>> eliminarAgencia(
            @PathVariable UUID id,
            @RequestHeader(value = "X-User-Role", required = false) String rol) {
        validarRolAdmin(rol);
        agenciaService.eliminarAgencia(id);
        return ResponseEntity.ok(ApiResponse.ok("Agencia eliminada correctamente", null));
    }

    private void validarRolAdmin(String rol) {
        if (!"ADMIN".equalsIgnoreCase(rol)) {
            throw new com.rapidocourier.shared_kernel.exception.BaseException(com.rapidocourier.servicio_envios.exception.ErrorCode.PERMISO_DENEGADO);
        }
    }
}
