package com.rapidocourier.servicio_clientes.service;

import com.rapidocourier.servicio_clientes.client.ReniecClient;
import com.rapidocourier.servicio_clientes.dto.request.ClienteRequest;
import com.rapidocourier.servicio_clientes.dto.request.ClienteUpdateRequest;
import com.rapidocourier.servicio_clientes.dto.response.ClienteResponse;
import com.rapidocourier.servicio_clientes.dto.response.ReniecResponse;
import com.rapidocourier.servicio_clientes.entity.Cliente;
import com.rapidocourier.servicio_clientes.exception.ErrorCode;
import com.rapidocourier.servicio_clientes.repository.ClienteRepository;
import com.rapidocourier.shared_kernel.exception.BaseException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ClienteService {

    private final ClienteRepository clienteRepository;
    private final ReniecClient reniecClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${reniec.api.token}")
    private String reniecToken;

    private final org.springframework.beans.factory.ObjectProvider<ClienteService> clienteServiceProvider;

    public ClienteResponse registrarCliente(ClienteRequest request) {
        if (clienteRepository.existsByEmail(request.email())) {
            enviarRollbackAuth(request.email());
            throw new BaseException(ErrorCode.EMAIL_YA_REGISTRADO);
        }
        if (clienteRepository.existsByDni(request.dni())) {
            enviarRollbackAuth(request.email());
            throw new BaseException(ErrorCode.DNI_YA_REGISTRADO);
        }

        String nombreCompleto;
        boolean verificado = false;
        try {
            // Intentamos llamar a la API
            nombreCompleto = clienteServiceProvider.getObject().obtenerNombreReniec(request.dni());
            verificado = true;
        } catch (Exception e) {
            // Si falla (por red o API), usamos el fallback manualmente aquí
            nombreCompleto = reniecFallback(request.dni(), e);
        }

        // Blindaje final por si acaso
        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            nombreCompleto = "Usuario Mock " + request.dni();
        }

        Cliente cliente = new Cliente();
        cliente.setDni(request.dni());
        cliente.setEmail(request.email());
        cliente.setTelefono(request.telefono());
        cliente.setNombreCompleto(nombreCompleto);
        cliente.setReniecVerificado(verificado);

        Cliente guardado = clienteRepository.save(cliente);

        return new ClienteResponse(
                guardado.getId(),
                guardado.getDni(),
                guardado.getNombreCompleto(),
                guardado.getEmail(),
                guardado.getTelefono(),
                guardado.getCreatedAt(),
                guardado.getUpdatedAt()
        );
    }

    public ClienteResponse registrarReceptor(String dni) {
        if (clienteRepository.existsByDni(dni)) {
            return buscarPorDni(dni);
        }

        String nombreCompleto;
        boolean verificado = false;
        try {
            nombreCompleto = clienteServiceProvider.getObject().obtenerNombreReniec(dni);
            verificado = true;
        } catch (Exception e) {
            nombreCompleto = reniecFallback(dni, e);
        }

        if (nombreCompleto == null || nombreCompleto.isBlank()) {
            nombreCompleto = "Usuario Mock " + dni;
        }

        Cliente cliente = new Cliente();
        cliente.setDni(dni);
        cliente.setEmail(dni + "@receptor.mock");
        cliente.setNombreCompleto(nombreCompleto);
        cliente.setReniecVerificado(verificado);

        Cliente guardado = clienteRepository.save(cliente);

        return new ClienteResponse(
                guardado.getId(),
                guardado.getDni(),
                guardado.getNombreCompleto(),
                guardado.getEmail(),
                guardado.getTelefono(),
                guardado.getCreatedAt(),
                guardado.getUpdatedAt()
        );
    }

    public ClienteResponse buscarPorDni(String dni) {
        Cliente cliente = clienteRepository.findByDni(dni)
                .orElseThrow(() -> new BaseException(ErrorCode.CLIENTE_NO_ENCONTRADO));

        return new ClienteResponse(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombreCompleto(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }

    public ClienteResponse buscarPorId(java.util.UUID id) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.CLIENTE_NO_ENCONTRADO));

        return new ClienteResponse(
                cliente.getId(),
                cliente.getDni(),
                cliente.getNombreCompleto(),
                cliente.getEmail(),
                cliente.getTelefono(),
                cliente.getCreatedAt(),
                cliente.getUpdatedAt()
        );
    }

    public ClienteResponse buscarPorEmail(String email) {
        Cliente cliente = clienteRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.CLIENTE_NO_ENCONTRADO));
        return new ClienteResponse(cliente.getId(), cliente.getDni(), cliente.getNombreCompleto(), cliente.getEmail(), cliente.getTelefono(), cliente.getCreatedAt(), cliente.getUpdatedAt());
    }

    public ClienteResponse actualizarContacto(java.util.UUID id, ClienteUpdateRequest request) {
        Cliente cliente = clienteRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.CLIENTE_NO_ENCONTRADO));
        
        if (request.email() != null && !request.email().isBlank()) {
            cliente.setEmail(request.email());
        }
        if (request.telefono() != null && !request.telefono().isBlank()) {
            cliente.setTelefono(request.telefono());
        }
        
        Cliente saved = clienteRepository.save(cliente);
        return new ClienteResponse(saved.getId(), saved.getDni(), saved.getNombreCompleto(), saved.getEmail(), saved.getTelefono(), saved.getCreatedAt(), saved.getUpdatedAt());
    }

    @CircuitBreaker(name = "reniecCB", fallbackMethod = "reniecFallback")
    public String obtenerNombreReniec(String dni) {
        try {
            System.out.println("DEBUG: Iniciando conexión a DeColecta...");
            ReniecResponse response = reniecClient.getNombreCompleto(dni, "Bearer " + reniecToken);

            if (response == null || response.full_name() == null) {
                System.err.println("DEBUG: Respuesta nula o campo full_name nulo.");
                throw new BaseException(ErrorCode.RENIEC_RESPUESTA_VACIA);
            }
            return response.full_name();
        } catch (Exception e) {
            System.err.println("DEBUG: ERROR CRÍTICO EN LLAMADA A API: " + e.getClass().getSimpleName() + " - " + e.getMessage());
            throw e; // Lanzamos para que CircuitBreaker lo maneje
        }
    }

    public String reniecFallback(String dni, Throwable t) {
        // Fallback seguro: siempre retorna un string no nulo
        return "Usuario Mock " + dni;
    }

    private void enviarRollbackAuth(String email) {
        try {
            System.out.println("DEBUG: Enviando evento de compensación (Rollback) a servicio-auth para el email: " + email);
            rabbitTemplate.convertAndSend("saga.exchange", "saga.auth.rollback.key", email);
        } catch (Exception e) {
            System.err.println("DEBUG: Error al enviar mensaje de Rollback a RabbitMQ: " + e.getMessage());
        }
    }
}