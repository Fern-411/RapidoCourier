package com.rapidocourier.servicio_envios.service;

import com.rapidocourier.servicio_envios.dto.request.EnvioRequest;
import com.rapidocourier.servicio_envios.dto.response.EnvioResponse;
import com.rapidocourier.servicio_envios.dto.response.HistorialEstadoEnvioResponse;
import com.rapidocourier.servicio_envios.entity.Agencia;
import com.rapidocourier.servicio_envios.entity.Envio;
import com.rapidocourier.servicio_envios.entity.EstadoEnvio;
import com.rapidocourier.servicio_envios.entity.HistorialEstadoEnvio;
import com.rapidocourier.servicio_envios.entity.TipoPago;
import com.rapidocourier.servicio_envios.repository.AgenciaRepository;
import com.rapidocourier.servicio_envios.repository.EnvioRepository;
import com.rapidocourier.servicio_envios.repository.HistorialEstadoEnvioRepository;
import com.rapidocourier.shared_kernel.exception.BaseException;
import com.rapidocourier.servicio_envios.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Random;
import java.util.UUID;
import java.util.stream.Collectors;
import java.time.LocalDateTime;

import com.rapidocourier.servicio_envios.client.ClienteClient;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import com.rapidocourier.shared_kernel.event.NotificacionEvent;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@RequiredArgsConstructor
public class EnvioService {

    private final EnvioRepository envioRepository;
    private final AgenciaRepository agenciaRepository;
    private final HistorialEstadoEnvioRepository historialRepository;
    private final RabbitTemplate rabbitTemplate;
    private final ClienteClient clienteClient;
    private final com.rapidocourier.servicio_envios.client.PaqueteClient paqueteClient;
    private final com.rapidocourier.servicio_envios.client.PagoClient pagoClient;
    private final StorageService storageService;

    @Transactional
    public EnvioResponse registrarEnvio(EnvioRequest request) {
        Agencia origen = agenciaRepository.findById(request.agenciaOrigenId())
                .orElseThrow(() -> new BaseException(ErrorCode.AGENCIA_NO_ENCONTRADA));
        Agencia destino = agenciaRepository.findById(request.agenciaDestinoId())
                .orElseThrow(() -> new BaseException(ErrorCode.AGENCIA_NO_ENCONTRADA));

        if (envioRepository.findByPaqueteId(request.paqueteId()).isPresent()) {
            throw new BaseException(ErrorCode.PAQUETE_YA_TIENE_ENVIO);
        }

        ApiResponse<com.rapidocourier.servicio_envios.client.PaqueteClient.PaqueteResponse> paqueteRes = paqueteClient.buscarPorId(request.paqueteId());
        if (paqueteRes == null || !paqueteRes.success() || paqueteRes.data() == null) {
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }

        Envio envio = new Envio();
        envio.setPaqueteId(request.paqueteId());
        envio.setDestinatarioId(paqueteRes.data().getDestinatarioId());
        envio.setAgenciaOrigen(origen);
        envio.setAgenciaDestino(destino);
        envio.setNumeroOrden(generarNumeroOrden());
        envio.setCodigoRastreo(generarCodigoCorto());
        envio.setClaveRecojo(request.claveRecojo());
        envio.setEstadoActual(EstadoEnvio.EN_AGENCIA_ORIGEN);
        
        if (request.tipoPago() != null && !request.tipoPago().isBlank()) {
            try {
                envio.setTipoPago(TipoPago.valueOf(request.tipoPago().toUpperCase()));
            } catch (IllegalArgumentException e) {
                envio.setTipoPago(TipoPago.ORIGEN);
            }
        } else {
            envio.setTipoPago(TipoPago.ORIGEN);
        }
        
        envio = envioRepository.save(envio);
        
        HistorialEstadoEnvio historial = new HistorialEstadoEnvio(envio, EstadoEnvio.EN_AGENCIA_ORIGEN, "SISTEMA");
        historialRepository.save(historial);

        return mapToResponse(envio);
    }

    @Transactional
    public EnvioResponse actualizarEstado(UUID envioId, EstadoEnvio nuevoEstado, String usuario) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));

        if (envio.getEstadoActual() == EstadoEnvio.ENTREGADO) {
            throw new BaseException(ErrorCode.ENVIO_YA_ENTREGADO);
        }

        if (nuevoEstado == EstadoEnvio.EN_AGENCIA_DESTINO) {
            try {
                ApiResponse<ClienteClient.ClienteResponse> response = clienteClient.buscarPorId(envio.getDestinatarioId());
                if (response != null && response.success() && response.data() != null) {
                    String destinatarioEmail = response.data().email();
                    NotificacionEvent event = NotificacionEvent.builder()
                            .paqueteId(envio.getPaqueteId())
                            .destinatario(destinatarioEmail)
                            .tipoNotificacion("PAQUETE_LISTO")
                            .asunto("Tu paquete está listo para recoger")
                            .mensaje("Acércate a la agencia. Recuerda que necesitas tu DNI físico y la Clave de Recojo proporcionada por el remitente.")
                            .build();
                    rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.enviar", event);
                    log.info("Evento de notificación de paquete listo enviado a {}", destinatarioEmail);
                }
            } catch (Exception e) {
                log.error("No se pudo obtener el correo del destinatario para enviar notificación", e);
            }
        }

        envio.setEstadoActual(nuevoEstado);
        envio = envioRepository.save(envio);

        HistorialEstadoEnvio historial = new HistorialEstadoEnvio(envio, nuevoEstado, usuario);
        historialRepository.save(historial);

        return mapToResponse(envio);
    }

    public EnvioResponse buscarPorRastreo(String numeroOrden, String codigoRastreo) {
        return envioRepository.findByNumeroOrdenAndCodigoRastreo(numeroOrden, codigoRastreo)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));
    }
    
    public EnvioResponse buscarPorPaqueteId(UUID paqueteId) {
        return envioRepository.findByPaqueteId(paqueteId)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));
    }

    public List<HistorialEstadoEnvioResponse> obtenerHistorial(UUID envioId) {
        return historialRepository.findByEnvioIdOrderByFechaCambioDesc(envioId).stream()
                .map(h -> new HistorialEstadoEnvioResponse(h.getId(), h.getEstado().name(), h.getFechaCambio(), h.getUsuarioResponsable()))
                .collect(Collectors.toList());
    }
    
    @Transactional
    public void entregarEnvio(UUID envioId, String pinProvided, String dniDestinatario, String usuario) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));

        if (Boolean.TRUE.equals(envio.getRecojoBloqueado())) {
            throw new BaseException(ErrorCode.RECOJO_BLOQUEADO);
        }
                
        if (envio.getEstadoActual() != EstadoEnvio.EN_AGENCIA_DESTINO) {
            throw new BaseException(ErrorCode.ESTADO_INVALIDO_PARA_ENTREGA);
        }
        
        // Verificar que el pago esté completado
        try {
            ApiResponse<Boolean> pagoResponse = pagoClient.verificarPagoCompletado(envio.getPaqueteId());
            if (pagoResponse == null || !pagoResponse.success() || !Boolean.TRUE.equals(pagoResponse.data())) {
                throw new BaseException(ErrorCode.PAGO_PENDIENTE);
            }
        } catch (BaseException e) {
            throw e;
        } catch (Exception e) {
            log.error("Error al verificar el estado de pago del paquete {}", envio.getPaqueteId(), e);
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
        
        boolean failed = false;

        if (envio.getClaveRecojo() == null || !envio.getClaveRecojo().equals(pinProvided)) {
            failed = true;
        } else {
            // Validar DNI solo si el PIN es correcto
            try {
                ApiResponse<ClienteClient.ClienteResponse> response = clienteClient.buscarPorId(envio.getDestinatarioId());
                if (response == null || !response.success() || response.data() == null) {
                    throw new BaseException(ErrorCode.ERROR_INTERNO);
                }
                if (!response.data().dni().equals(dniDestinatario)) {
                    failed = true;
                }
            } catch (BaseException e) {
                throw e;
            } catch (Exception e) {
                log.error("No se pudo validar el DNI del destinatario", e);
                throw new BaseException(ErrorCode.ERROR_INTERNO);
            }
        }

        if (failed) {
            envio.setIntentosFallidosRecojo(envio.getIntentosFallidosRecojo() != null ? envio.getIntentosFallidosRecojo() + 1 : 1);
            if (envio.getIntentosFallidosRecojo() >= 3) {
                envio.setRecojoBloqueado(true);
            }
            envioRepository.save(envio);

            if (Boolean.TRUE.equals(envio.getRecojoBloqueado())) {
                throw new BaseException(ErrorCode.RECOJO_BLOQUEADO);
            } else {
                throw new BaseException(ErrorCode.PIN_INVALIDO); // O DNI inválido, agrupamos el error
            }
        }
        
        // Si todo está bien, reiniciamos intentos y entregamos
        envio.setIntentosFallidosRecojo(0);
        envioRepository.save(envio);
        actualizarEstado(envioId, EstadoEnvio.ENTREGADO, usuario);
    }

    @Transactional
    public void solicitarDesbloqueo(UUID envioId) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));

        if (!Boolean.TRUE.equals(envio.getRecojoBloqueado())) {
            throw new BaseException(ErrorCode.ERROR_INTERNO); // No está bloqueado
        }

        // Obtener correo del remitente
        try {
            ApiResponse<com.rapidocourier.servicio_envios.client.PaqueteClient.PaqueteResponse> paqueteRes = paqueteClient.buscarPorId(envio.getPaqueteId());
            if (paqueteRes == null || !paqueteRes.success() || paqueteRes.data() == null) throw new BaseException(ErrorCode.ERROR_INTERNO);
            
            ApiResponse<ClienteClient.ClienteResponse> remitenteRes = clienteClient.buscarPorId(paqueteRes.data().getRemitenteId());
            if (remitenteRes == null || !remitenteRes.success() || remitenteRes.data() == null) throw new BaseException(ErrorCode.ERROR_INTERNO);

            String emailRemitente = remitenteRes.data().email();
            String otp = generarPin(); // Genera 6 dígitos
            
            envio.setOtpDesbloqueo(otp);
            envio.setOtpDesbloqueoExpiracion(LocalDateTime.now().plusMinutes(15));
            envioRepository.save(envio);

            NotificacionEvent event = NotificacionEvent.builder()
                    .paqueteId(envio.getPaqueteId())
                    .destinatario(emailRemitente)
                    .tipoNotificacion("DESBLOQUEO_RECOJO")
                    .asunto("Código de Recuperación de Paquete")
                    .mensaje("Usa este código OTP para desbloquear tu paquete: " + otp + ". Válido por 15 minutos.")
                    .build();
            rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.enviar", event);
            log.info("OTP de desbloqueo enviado al remitente: {}", emailRemitente);

        } catch (Exception e) {
            log.error("Error enviando OTP de desbloqueo", e);
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
    }

    @Transactional
    public void desbloquearEnvio(UUID envioId, String otp, String nuevaClave) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));

        if (!Boolean.TRUE.equals(envio.getRecojoBloqueado())) {
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }

        if (envio.getOtpDesbloqueo() == null || 
            !envio.getOtpDesbloqueo().equals(otp) || 
            envio.getOtpDesbloqueoExpiracion().isBefore(LocalDateTime.now())) {
            throw new BaseException(ErrorCode.OTP_INVALIDO);
        }

        envio.setClaveRecojo(nuevaClave);
        envio.setRecojoBloqueado(false);
        envio.setIntentosFallidosRecojo(0);
        envio.setOtpDesbloqueo(null);
        envio.setOtpDesbloqueoExpiracion(null);
        envioRepository.save(envio);
    }

    public com.rapidocourier.servicio_envios.dto.response.EstadisticasEnvioResponse obtenerEstadisticasDiarias() {
        java.time.LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        List<Object[]> results = envioRepository.countByEstadoDesde(startOfDay);
        
        long total = 0;
        java.util.Map<String, Long> porEstado = new java.util.HashMap<>();
        
        for (Object[] row : results) {
            EstadoEnvio estado = (EstadoEnvio) row[0];
            Long count = (Long) row[1];
            porEstado.put(estado.name(), count);
            total += count;
        }
        
        return new com.rapidocourier.servicio_envios.dto.response.EstadisticasEnvioResponse(total, porEstado);
    }

    private String generarNumeroOrden() {
        return String.valueOf(new java.util.Random().nextInt(90000000) + 10000000); // 8 dígitos
    }

    private String generarCodigoCorto() {
        String chars = "ABCDEFGHIJKLMNOPQRSTUVWXYZ0123456789";
        StringBuilder code = new StringBuilder();
        java.util.Random rnd = new java.util.Random();
        for (int i = 0; i < 4; i++) {
            code.append(chars.charAt(rnd.nextInt(chars.length())));
        }
        return code.toString(); // 4 chars
    }

    private String generarPin() {
        return String.format("%06d", new Random().nextInt(999999));
    }

    private EnvioResponse mapToResponse(Envio envio) {
        return new EnvioResponse(
                envio.getId(),
                envio.getNumeroOrden(),
                envio.getCodigoRastreo(),
                envio.getPaqueteId(),
                envio.getAgenciaOrigen().getNombre(),
                envio.getAgenciaDestino().getNombre(),
                envio.getEstadoActual().name(),
                envio.getUrlBoleta(),
                envio.getUrlGuia(),
                envio.getTipoPago() != null ? envio.getTipoPago().name() : "ORIGEN",
                envio.getCreatedAt(),
                envio.getUpdatedAt()
        );
    }

    @Transactional
    public String guardarBoleta(UUID envioId, org.springframework.web.multipart.MultipartFile file) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));
        
        String urlBoleta = storageService.subirDocumento(file, "rapidocourier-boletas/boleta-" + envio.getCodigoRastreo());
        envio.setUrlBoleta(urlBoleta);
        envioRepository.save(envio);

        // Notificar al remitente con la boleta
        try {
            ApiResponse<com.rapidocourier.servicio_envios.client.PaqueteClient.PaqueteResponse> paqueteRes = paqueteClient.buscarPorId(envio.getPaqueteId());
            if (paqueteRes != null && paqueteRes.success() && paqueteRes.data() != null) {
                ApiResponse<ClienteClient.ClienteResponse> remitenteRes = clienteClient.buscarPorId(paqueteRes.data().getRemitenteId());
                if (remitenteRes != null && remitenteRes.success() && remitenteRes.data() != null) {
                    String emailRemitente = remitenteRes.data().email();
                    if (emailRemitente != null && !emailRemitente.isEmpty()) {
                        NotificacionEvent event = NotificacionEvent.builder()
                                .paqueteId(envio.getPaqueteId())
                                .destinatario(emailRemitente)
                                .tipoNotificacion("BOLETA_EMAIL")
                                .asunto("Tu Boleta de Venta - Rápido Courier")
                                .mensaje(urlBoleta)
                                .build();
                        rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.enviar", event);
                        log.info("Evento BOLETA_EMAIL enviado a {}", emailRemitente);
                    }
                }
            }
        } catch (Exception e) {
            log.error("No se pudo enviar la boleta al remitente del envío {}: {}", envioId, e.getMessage());
        }

        return urlBoleta;
    }

    @Transactional
    public String guardarGuia(UUID envioId, org.springframework.web.multipart.MultipartFile file) {
        Envio envio = envioRepository.findById(envioId)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));
        
        String urlGuia = storageService.subirDocumento(file, "rapidocourier-guias/guia-" + envio.getCodigoRastreo());
        envio.setUrlGuia(urlGuia);
        envioRepository.save(envio);

        // Notificar al remitente con la guía
        try {
            ApiResponse<com.rapidocourier.servicio_envios.client.PaqueteClient.PaqueteResponse> paqueteRes = paqueteClient.buscarPorId(envio.getPaqueteId());
            if (paqueteRes != null && paqueteRes.success() && paqueteRes.data() != null) {
                ApiResponse<ClienteClient.ClienteResponse> remitenteRes = clienteClient.buscarPorId(paqueteRes.data().getRemitenteId());
                if (remitenteRes != null && remitenteRes.success() && remitenteRes.data() != null) {
                    String emailRemitente = remitenteRes.data().email();
                    if (emailRemitente != null && !emailRemitente.isEmpty()) {
                        NotificacionEvent event = NotificacionEvent.builder()
                                .paqueteId(envio.getPaqueteId())
                                .destinatario(emailRemitente)
                                .tipoNotificacion("GUIA_EMAIL")
                                .asunto("Tu Guía de Envío - Rápido Courier")
                                .mensaje(urlGuia)
                                .build();
                        rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.enviar", event);
                        log.info("Evento GUIA_EMAIL enviado a {}", emailRemitente);
                    }
                }
            }
        } catch (Exception e) {
            log.error("Error al enviar notificación de guía al remitente del envío {}: {}", envioId, e.getMessage());
        }

        return urlGuia;
    }

    public com.rapidocourier.servicio_envios.dto.response.BoletaDetalleResponse obtenerBoletaDetalle(String numeroOrden, String codigoRastreo) {
        Envio envio = envioRepository.findByNumeroOrdenAndCodigoRastreo(numeroOrden, codigoRastreo)
                .orElseThrow(() -> new BaseException(ErrorCode.ENVIO_NO_ENCONTRADO));

        // 1. Obtener datos del paquete
        ApiResponse<com.rapidocourier.servicio_envios.client.PaqueteClient.PaqueteResponse> paqueteRes = paqueteClient.buscarPorId(envio.getPaqueteId());
        if (paqueteRes == null || !paqueteRes.success() || paqueteRes.data() == null) {
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
        var paquete = paqueteRes.data();

        // 2. Obtener datos de los clientes
        ApiResponse<ClienteClient.ClienteResponse> remitenteRes = clienteClient.buscarPorId(paquete.getRemitenteId());
        ApiResponse<ClienteClient.ClienteResponse> destinatarioRes = clienteClient.buscarPorId(envio.getDestinatarioId());
        
        if (remitenteRes == null || !remitenteRes.success() || remitenteRes.data() == null ||
            destinatarioRes == null || !destinatarioRes.success() || destinatarioRes.data() == null) {
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
        var remitente = remitenteRes.data();
        var destinatario = destinatarioRes.data();

        // 3. Obtener datos del pago (Opcional si solo se está generando la Guía)
        java.math.BigDecimal monto = java.math.BigDecimal.ZERO;
        String estadoPago = "PENDIENTE";
        try {
            ApiResponse<com.rapidocourier.servicio_envios.client.PagoClient.PagoResponse> pagoRes = pagoClient.buscarPorPaqueteId(envio.getPaqueteId());
            if (pagoRes != null && pagoRes.success() && pagoRes.data() != null) {
                var pago = pagoRes.data();
                monto = pago.monto();
                estadoPago = pago.estadoPago();
            }
        } catch (Exception e) {
            log.warn("Pago no encontrado para el paquete {} - Esto es normal al generar Guía", envio.getPaqueteId());
        }

        return new com.rapidocourier.servicio_envios.dto.response.BoletaDetalleResponse(
                envio.getNumeroOrden(),
                envio.getCodigoRastreo(),
                envio.getCreatedAt(),
                envio.getAgenciaOrigen().getNombre(),
                envio.getAgenciaOrigen().getDireccion(),
                envio.getAgenciaDestino().getNombre(),
                envio.getAgenciaDestino().getDireccion(),
                new com.rapidocourier.servicio_envios.dto.response.BoletaDetalleResponse.ClienteDetalle(remitente.nombreCompleto(), remitente.dni(), remitente.telefono()),
                new com.rapidocourier.servicio_envios.dto.response.BoletaDetalleResponse.ClienteDetalle(destinatario.nombreCompleto(), destinatario.dni(), destinatario.telefono()),
                paquete.getPesoKg(),
                paquete.getDescripcion() != null ? paquete.getDescripcion() : "Paqueteria",
                monto,
                estadoPago
        );
    }

    public java.util.List<EnvioResponse> buscarPorDniDestinatario(String dni) {
        // 1. Obtener destinatario por DNI
        com.rapidocourier.shared_kernel.dto.response.ApiResponse<ClienteClient.ClienteResponse> responseCliente = clienteClient.buscarPorDni(dni);
        if (responseCliente == null || responseCliente.data() == null) {
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
        
        UUID destinatarioId = responseCliente.data().id();
        
        // 2. Buscar envíos
        java.util.List<Envio> envios = envioRepository.findByDestinatarioIdOrderByCreatedAtDesc(destinatarioId);
        
        // 3. Mapear a DTO
        return envios.stream().map(this::mapToResponse).toList();
    }

    public List<EnvioResponse> listarEnvios() {
        return envioRepository.findAll(org.springframework.data.domain.Sort.by(org.springframework.data.domain.Sort.Direction.DESC, "createdAt"))
                .stream().map(this::mapToResponse).toList();
    }

    public com.rapidocourier.shared_kernel.dto.response.PaginaResponse<EnvioResponse> buscarEnviosPaginados(
            String busqueda, java.time.LocalDateTime fechaInicio, java.time.LocalDateTime fechaFin, String estado, org.springframework.data.domain.Pageable pageable) {
        org.springframework.data.jpa.domain.Specification<Envio> spec = (root, query, cb) -> {
            java.util.List<jakarta.persistence.criteria.Predicate> predicates = new java.util.ArrayList<>();
            if (busqueda != null && !busqueda.isBlank()) {
                String term = "%" + busqueda.toLowerCase() + "%";
                predicates.add(cb.or(
                        cb.like(cb.lower(root.get("codigoRastreo")), term),
                        cb.like(cb.lower(root.get("numeroOrden")), term)
                ));
            }
            if (fechaInicio != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("createdAt"), fechaInicio));
            }
            if (fechaFin != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("createdAt"), fechaFin));
            }
            if (estado != null && !estado.isBlank()) {
                try {
                    EstadoEnvio estadoEnum = EstadoEnvio.valueOf(estado.toUpperCase());
                    predicates.add(cb.equal(root.get("estadoActual"), estadoEnum));
                } catch (IllegalArgumentException e) {
                    // Estado ignorado si no es válido
                }
            }
            return cb.and(predicates.toArray(new jakarta.persistence.criteria.Predicate[0]));
        };

        org.springframework.data.domain.Page<Envio> pagina = envioRepository.findAll(spec, pageable);
        java.util.List<EnvioResponse> respuestas = pagina.getContent().stream().map(this::mapToResponse).toList();
        return com.rapidocourier.shared_kernel.dto.response.PaginaResponse.de(
                respuestas, pagina.getNumber(), pagina.getSize(), pagina.getTotalElements(), pagina.getTotalPages());
    }
}
