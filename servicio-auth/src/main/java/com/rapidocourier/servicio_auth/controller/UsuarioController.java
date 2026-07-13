package com.rapidocourier.servicio_auth.controller;

import com.rapidocourier.servicio_auth.dto.request.ActualizarPerfilRequest;
import com.rapidocourier.servicio_auth.dto.request.EmailChangeRequest;
import com.rapidocourier.servicio_auth.dto.request.EmailChangeVerifyRequest;
import com.rapidocourier.servicio_auth.dto.response.UsuarioDetalleResponse;
import com.rapidocourier.servicio_auth.service.AuthService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/usuarios")
@RequiredArgsConstructor
@Tag(name = "Usuarios", description = "Endpoints para la gestión de perfil de usuario")
public class UsuarioController {

    private final AuthService authService;

    @Operation(summary = "Obtener Perfil", description = "Obtiene los detalles del usuario autenticado.")
    @GetMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioDetalleResponse>> getProfile(@RequestHeader("X-User-Email") String email) {
        UsuarioDetalleResponse response = authService.getProfile(email);
        return ResponseEntity.ok(ApiResponse.ok("Perfil obtenido exitosamente", response));
    }

    @Operation(summary = "Actualizar Perfil", description = "Actualiza la información básica del usuario autenticado.")
    @PutMapping("/me")
    public ResponseEntity<ApiResponse<UsuarioDetalleResponse>> updateProfile(
            @Valid @RequestBody ActualizarPerfilRequest request,
            @RequestHeader("X-User-Email") String email) {
        UsuarioDetalleResponse response = authService.updateProfile(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Perfil actualizado exitosamente", response));
    }

    @Operation(summary = "Solicitar cambio de correo", description = "Inicia el flujo enviando un OTP al nuevo correo.")
    @PostMapping("/me/email/request-change")
    public ResponseEntity<ApiResponse<Void>> requestEmailChange(
            @Valid @RequestBody EmailChangeRequest request,
            @RequestHeader("X-User-Email") String email) {
        authService.requestEmailChange(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Código de verificación enviado al nuevo correo", null));
    }

    @Operation(summary = "Verificar cambio de correo", description = "Valida el OTP enviado al nuevo correo y efectúa el cambio.")
    @PostMapping("/me/email/verify-change")
    public ResponseEntity<ApiResponse<Void>> verifyEmailChange(
            @Valid @RequestBody EmailChangeVerifyRequest request,
            @RequestHeader("X-User-Email") String email) {
        authService.verifyEmailChange(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Correo electrónico actualizado exitosamente", null));
    }
}
