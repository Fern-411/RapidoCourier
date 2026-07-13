package com.rapidocourier.servicio_paquetes.service;

import com.rapidocourier.servicio_paquetes.client.ClienteClient;
import com.rapidocourier.servicio_paquetes.client.NotificacionesClient;
import com.rapidocourier.servicio_paquetes.dto.request.PaqueteRequest;
import com.rapidocourier.servicio_paquetes.dto.response.ClienteResponse;
import com.rapidocourier.servicio_paquetes.dto.response.PaqueteResponse;
import com.rapidocourier.servicio_paquetes.entity.Categoria;
import com.rapidocourier.servicio_paquetes.entity.Paquete;
import com.rapidocourier.servicio_paquetes.exception.ErrorCode;
import com.rapidocourier.servicio_paquetes.repository.CategoriaRepository;
import com.rapidocourier.servicio_paquetes.repository.PaqueteRepository;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import com.rapidocourier.shared_kernel.exception.BaseException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Lazy;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class PaqueteService {

    private final PaqueteRepository paqueteRepository;
    private final CategoriaRepository categoriaRepository;
    private final ClienteClient clienteClient;
    private final NotificacionesClient notificacionesClient;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Autowired
    @Lazy
    private PaqueteService self;

    @Transactional
    public PaqueteResponse registrarPaquete(PaqueteRequest request) {
        UUID remitenteId = self.validarDniYObtenerId(request.dniRemitente());
        UUID destinatarioId = self.validarDniYObtenerId(request.dniDestinatario());

        Set<Categoria> categorias = request.categorias().stream()
                .map(nombre -> categoriaRepository.findByNombre(nombre)
                        .orElseGet(() -> categoriaRepository.save(new Categoria(nombre))))
                .collect(Collectors.toSet());

        Paquete paquete = new Paquete();
        paquete.setPesoKg(request.pesoKg());
        paquete.setValorDeclarado(request.valorDeclarado());
        paquete.setAltoCm(request.altoCm());
        paquete.setAnchoCm(request.anchoCm());
        paquete.setLargoCm(request.largoCm());
        paquete.setRemitenteId(remitenteId);
        paquete.setDestinatarioId(destinatarioId);
        paquete.setCategorias(categorias);
        paquete.setCreatedAt(LocalDateTime.now());

        Paquete guardado = paqueteRepository.save(paquete);

        com.rapidocourier.shared_kernel.event.PaqueteRegistradoEvent event = 
                new com.rapidocourier.shared_kernel.event.PaqueteRegistradoEvent(
                        guardado.getId(), remitenteId, destinatarioId);
        rabbitTemplate.convertAndSend("paquetes.exchange", "paquetes.creado", event);

        return mapToResponse(guardado);
    }

    @CircuitBreaker(name = "clientesCB", fallbackMethod = "fallbackValidarDni")
    public UUID validarDniYObtenerId(String dni) {
        try {
            ApiResponse<ClienteResponse> response = clienteClient.buscarPorDni(dni);
            if (response != null && response.success() && response.data() != null) {
                return response.data().id();
            }
        } catch (Exception e) {
            // Ignorar excepción, probablemente 404 Not Found. Intentar crear receptor.
        }

        try {
            ApiResponse<ClienteResponse> newReceptor = clienteClient.crearReceptor(dni);
            if (newReceptor != null && newReceptor.success() && newReceptor.data() != null) {
                return newReceptor.data().id();
            }
        } catch (Exception e) {
            // Si también falla, se lanzará la excepción por defecto abajo
        }

        throw new BaseException(ErrorCode.RECURSO_NO_ENCONTRADO);
    }

    public UUID fallbackValidarDni(String dni, Throwable t) {
        throw new BaseException(ErrorCode.FALLO_EXTERNO);
    }

    private String obtenerNombreClientePorId(UUID id) {
        try {
            ApiResponse<ClienteResponse> response = clienteClient.buscarPorId(id);
            return (response != null && response.data() != null) ? response.data().nombreCompleto() : "Desconocido";
        } catch (Exception e) { return "Desconocido"; }
    }

    public List<PaqueteResponse> buscar(LocalDateTime inicio, LocalDateTime fin) {
        List<Paquete> paquetes;
        if (inicio == null && fin == null) {
            paquetes = paqueteRepository.findAll();
        } else if (inicio != null && fin != null) {
            paquetes = paqueteRepository.findByCreatedAtBetween(inicio, fin);
        } else if (inicio != null) {
            paquetes = paqueteRepository.findByCreatedAtGreaterThanEqual(inicio);
        } else {
            paquetes = paqueteRepository.findByCreatedAtLessThanEqual(fin);
        }
        return paquetes.stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public List<PaqueteResponse> buscarPorRemitenteId(UUID remitenteId) {
        return paqueteRepository.findByRemitenteId(remitenteId).stream().map(this::mapToResponse).collect(Collectors.toList());
    }

    public PaqueteResponse buscarPorId(UUID paqueteId) {
        return mapToResponse(paqueteRepository.findById(paqueteId).orElseThrow(() -> new BaseException(ErrorCode.PAQUETE_NO_ENCONTRADO)));
    }

    public PaqueteResponse agregarCategorias(UUID paqueteId, List<String> nuevasCategorias) {
        Paquete paquete = paqueteRepository.findById(paqueteId).orElseThrow(() -> new BaseException(ErrorCode.PAQUETE_NO_ENCONTRADO));
        nuevasCategorias.forEach(nc -> categoriaRepository.findByNombre(nc).ifPresentOrElse(paquete.getCategorias()::add, () -> {
            Categoria ncObj = categoriaRepository.save(new Categoria(nc));
            paquete.getCategorias().add(ncObj);
        }));
        return mapToResponse(paqueteRepository.save(paquete));
    }

    public void eliminarPaquete(UUID paqueteId) {
        paqueteRepository.delete(paqueteRepository.findById(paqueteId).orElseThrow(() -> new BaseException(ErrorCode.PAQUETE_NO_ENCONTRADO)));
    }

    private PaqueteResponse mapToResponse(Paquete p) {
        String remitente = (p.getRemitenteId() != null) ? obtenerNombreClientePorId(p.getRemitenteId()) : "Desconocido";
        String destinatario = (p.getDestinatarioId() != null) ? obtenerNombreClientePorId(p.getDestinatarioId()) : "Desconocido";

        return new PaqueteResponse(
                p.getId(),
                p.getPesoKg(),
                p.getValorDeclarado(),
                p.getAltoCm(),
                p.getAnchoCm(),
                p.getLargoCm(),
                p.getCreatedAt(),
                remitente,
                destinatario,
                p.getRemitenteId(),
                p.getDestinatarioId()
        );
    }
}