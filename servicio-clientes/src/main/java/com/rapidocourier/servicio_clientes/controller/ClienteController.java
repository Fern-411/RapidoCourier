package com.rapidocourier.servicio_clientes.controller;

import com.rapidocourier.servicio_clientes.dto.request.ClienteRequest;
import com.rapidocourier.servicio_clientes.dto.request.ClienteUpdateRequest;
import com.rapidocourier.servicio_clientes.dto.response.ClienteResponse;
import com.rapidocourier.servicio_clientes.service.ClienteService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/clientes")
public class ClienteController {

    private final ClienteService clienteService;

    @PostMapping
    public ResponseEntity<ApiResponse<ClienteResponse>> registrarCliente(@Valid @RequestBody ClienteRequest request) {
        ClienteResponse response = clienteService.registrarCliente(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Cliente registrado", response));
    }

    @PostMapping("/receptor")
    public ResponseEntity<ApiResponse<ClienteResponse>> registrarReceptor(@RequestParam String dni) {
        ClienteResponse response = clienteService.registrarReceptor(dni);
        return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.ok("Receptor registrado", response));
    }

    @GetMapping("/perfil")
    public ResponseEntity<ApiResponse<ClienteResponse>> getMiPerfil(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(ApiResponse.ok("Perfil encontrado", clienteService.buscarPorEmail(email)));
    }


    @GetMapping("/dni/{dni}")
    public ResponseEntity<ApiResponse<ClienteResponse>> getClienteByDni(@PathVariable String dni) {
        ClienteResponse response = clienteService.buscarPorDni(dni);
        return ResponseEntity.ok(ApiResponse.ok("Cliente encontrado", response));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<ClienteResponse>> getClienteById(@PathVariable java.util.UUID id) {
        ClienteResponse response = clienteService.buscarPorId(id);
        return ResponseEntity.ok(ApiResponse.ok("Cliente encontrado", response));
    }

    @GetMapping("/email/{email}")
    public ResponseEntity<ApiResponse<ClienteResponse>> getClienteByEmail(@PathVariable String email) {
        ClienteResponse response = clienteService.buscarPorEmail(email);
        return ResponseEntity.ok(ApiResponse.ok("Cliente encontrado", response));
    }

    @GetMapping("/reniec/{dni}")
    public ResponseEntity<ApiResponse<String>> getNombreReniec(@PathVariable String dni) {
        String nombre = clienteService.obtenerNombreReniec(dni);
        // Si hay fallo, el fallback ya devuelve "Usuario Mock {dni}"
        return ResponseEntity.ok(ApiResponse.ok("Consulta RENIEC exitosa", nombre));
    }

    @PutMapping("/{id}/contacto")
    public ResponseEntity<ApiResponse<ClienteResponse>> actualizarContacto(
            @PathVariable java.util.UUID id,
            @Valid @RequestBody ClienteUpdateRequest request) {
        ClienteResponse response = clienteService.actualizarContacto(id, request);
        return ResponseEntity.ok(ApiResponse.ok("Contacto actualizado", response));
    }
}
