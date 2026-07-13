package com.rapidocourier.servicio_auth.controller;

import com.rapidocourier.servicio_auth.dto.request.CambiarRolRequest;
import com.rapidocourier.servicio_auth.dto.response.UsuarioAdminResponse;
import com.rapidocourier.servicio_auth.service.AuthService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/usuarios")
@RequiredArgsConstructor
@Tag(name = "Administración de Usuarios", description = "Endpoints administrativos para gestionar roles y cuentas")
public class AdminUsuarioController {

    private final AuthService authService;

    @Operation(summary = "Listar todos los usuarios", description = "Devuelve la lista de usuarios. Requiere rol ADMIN.")
    @GetMapping
    public ResponseEntity<ApiResponse<List<UsuarioAdminResponse>>> listarUsuarios(
            @RequestHeader(value = "X-User-Role", defaultValue = "") String executorRole) {
        List<UsuarioAdminResponse> usuarios = authService.listarUsuarios(executorRole);
        return ResponseEntity.ok(ApiResponse.ok("Lista de usuarios obtenida exitosamente", usuarios));
    }

    @Operation(summary = "Cambiar rol de usuario", description = "Asigna un nuevo rol a un usuario existente. Requiere rol ADMIN.")
    @PutMapping("/{id}/rol")
    public ResponseEntity<ApiResponse<Void>> cambiarRolUsuario(
            @PathVariable UUID id,
            @Valid @RequestBody CambiarRolRequest request,
            @RequestHeader(value = "X-User-Role", defaultValue = "") String executorRole) {
        authService.cambiarRolUsuario(id, request, executorRole);
        return ResponseEntity.ok(ApiResponse.ok("Rol cambiado exitosamente y sesiones revocadas", null));
    }
}
