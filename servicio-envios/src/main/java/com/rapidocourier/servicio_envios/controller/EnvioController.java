package com.rapidocourier.servicio_envios.controller;

import com.rapidocourier.servicio_envios.dto.request.EnvioRequest;
import com.rapidocourier.servicio_envios.dto.response.EnvioResponse;
import com.rapidocourier.servicio_envios.dto.response.HistorialEstadoEnvioResponse;
import com.rapidocourier.servicio_envios.entity.EstadoEnvio;
import com.rapidocourier.servicio_envios.service.EnvioService;
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
@RequestMapping("/api/v1/envios")
@RequiredArgsConstructor
@Tag(name = "Envios", description = "Gestión de Envíos Físicos")
public class EnvioController {
    
    private final EnvioService envioService;

    @Operation(summary = "Obtener todos los envíos")
    @GetMapping
    public ResponseEntity<ApiResponse<List<EnvioResponse>>> obtenerTodos() {
        return ResponseEntity.ok(ApiResponse.ok("Lista de envíos", envioService.listarEnvios()));
    }

    @Operation(summary = "Obtener envíos paginados y filtrados")
    @GetMapping("/paginado")
    public ResponseEntity<ApiResponse<com.rapidocourier.shared_kernel.dto.response.PaginaResponse<EnvioResponse>>> obtenerPaginados(
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String busqueda,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaInicio,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) java.time.LocalDateTime fechaFin,
            @RequestParam(required = false) String estado,
            @RequestParam(defaultValue = "createdAt") String sortBy,
            @RequestParam(defaultValue = "DESC") String sortDir
    ) {
        org.springframework.data.domain.Sort sort = sortDir.equalsIgnoreCase("ASC") ? 
                org.springframework.data.domain.Sort.by(sortBy).ascending() : 
                org.springframework.data.domain.Sort.by(sortBy).descending();
        org.springframework.data.domain.Pageable pageable = org.springframework.data.domain.PageRequest.of(page, size, sort);
        
        return ResponseEntity.ok(ApiResponse.ok("Envíos paginados", 
                envioService.buscarEnviosPaginados(busqueda, fechaInicio, fechaFin, estado, pageable)));
    }

    @Operation(summary = "Registrar envío")
    @PostMapping
    public ResponseEntity<ApiResponse<EnvioResponse>> registrarEnvio(@Valid @RequestBody EnvioRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.ok("Envío registrado", envioService.registrarEnvio(request)));
    }

    @Operation(summary = "Actualizar estado")
    @PutMapping("/{id}/estado")
    public ResponseEntity<ApiResponse<EnvioResponse>> actualizarEstado(
            @PathVariable UUID id,
            @RequestParam EstadoEnvio nuevoEstado,
            @RequestHeader(value = "X-User-Id", defaultValue = "SISTEMA") String usuario) {
        return ResponseEntity.ok(ApiResponse.ok("Estado actualizado", envioService.actualizarEstado(id, nuevoEstado, usuario)));
    }

    @Operation(summary = "Entregar paquete con PIN y DNI")
    @PostMapping("/{id}/entregar")
    public ResponseEntity<ApiResponse<Void>> entregarEnvio(
            @PathVariable UUID id,
            @RequestParam String pin,
            @RequestParam String dniDestinatario,
            @RequestHeader(value = "X-User-Id", defaultValue = "SISTEMA") String usuario) {
        envioService.entregarEnvio(id, pin, dniDestinatario, usuario);
        return ResponseEntity.ok(ApiResponse.ok("Paquete entregado correctamente", null));
    }

    @Operation(summary = "Buscar por número de orden y código de rastreo")
    @GetMapping("/rastreo/{numeroOrden}/{codigoRastreo}")
    public ResponseEntity<ApiResponse<EnvioResponse>> buscarPorRastreo(
            @PathVariable String numeroOrden,
            @PathVariable String codigoRastreo) {
        return ResponseEntity.ok(ApiResponse.ok("Envío encontrado", envioService.buscarPorRastreo(numeroOrden, codigoRastreo)));
    }

    @Operation(summary = "Ver historial de envío")
    @GetMapping("/{id}/historial")
    public ResponseEntity<ApiResponse<List<HistorialEstadoEnvioResponse>>> obtenerHistorial(@PathVariable UUID id) {
        return ResponseEntity.ok(ApiResponse.ok("Historial de envío", envioService.obtenerHistorial(id)));
    }

    @Operation(summary = "Estadísticas del día", description = "Obtiene cantidad de envíos creados/actualizados hoy y agrupados por estado.")
    @GetMapping("/estadisticas/resumen-diario")
    public ResponseEntity<ApiResponse<com.rapidocourier.servicio_envios.dto.response.EstadisticasEnvioResponse>> obtenerEstadisticasDiarias() {
        return ResponseEntity.ok(ApiResponse.ok("Estadísticas diarias de envíos obtenidas", envioService.obtenerEstadisticasDiarias()));
    }

    @Operation(summary = "Subir Boleta", description = "Sube la boleta (PDF) a Cloudflare R2 y guarda la URL en el envío.")
    @PostMapping(value = "/{id}/boleta", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> subirBoleta(
            @PathVariable UUID id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String urlBoleta = envioService.guardarBoleta(id, file);
        return ResponseEntity.ok(ApiResponse.ok("Boleta subida exitosamente", urlBoleta));
    }

    @Operation(summary = "Subir Guía", description = "Sube la guía de envío (PDF) a Cloudflare R2 y guarda la URL en el envío.")
    @PostMapping(value = "/{id}/guia", consumes = "multipart/form-data")
    public ResponseEntity<ApiResponse<String>> subirGuia(
            @PathVariable UUID id,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        String urlGuia = envioService.guardarGuia(id, file);
        return ResponseEntity.ok(ApiResponse.ok("Guía subida exitosamente", urlGuia));
    }

    @Operation(summary = "Obtener datos completos para la Boleta")
    @GetMapping("/rastreo/{numeroOrden}/{codigoRastreo}/boleta-datos")
    public ResponseEntity<ApiResponse<com.rapidocourier.servicio_envios.dto.response.BoletaDetalleResponse>> obtenerBoletaDetalle(
            @PathVariable String numeroOrden,
            @PathVariable String codigoRastreo) {
        return ResponseEntity.ok(ApiResponse.ok("Datos de boleta obtenidos", envioService.obtenerBoletaDetalle(numeroOrden, codigoRastreo)));
    }

    @Operation(summary = "Solicitar desbloqueo de paquete")
    @PostMapping("/{id}/solicitar-desbloqueo")
    public ResponseEntity<ApiResponse<Void>> solicitarDesbloqueo(@PathVariable UUID id) {
        envioService.solicitarDesbloqueo(id);
        return ResponseEntity.ok(ApiResponse.ok("Se ha enviado un código de recuperación al correo del remitente.", null));
    }

    @Operation(summary = "Desbloquear paquete y asignar nueva clave")
    @PostMapping("/{id}/desbloquear")
    public ResponseEntity<ApiResponse<Void>> desbloquearEnvio(
            @PathVariable UUID id,
            @Valid @RequestBody com.rapidocourier.servicio_envios.dto.request.DesbloqueoRequest request) {
        envioService.desbloquearEnvio(id, request.otp(), request.nuevaClaveRecojo());
        return ResponseEntity.ok(ApiResponse.ok("Paquete desbloqueado exitosamente. La nueva clave de recojo ha sido configurada.", null));
    }

    @Operation(summary = "Obtener envíos por DNI de destinatario")
    @GetMapping("/destinatario/dni/{dni}")
    public ResponseEntity<ApiResponse<List<EnvioResponse>>> buscarPorDniDestinatario(@PathVariable String dni) {
        return ResponseEntity.ok(ApiResponse.ok("Envíos del cliente obtenidos", envioService.buscarPorDniDestinatario(dni)));
    }
}
