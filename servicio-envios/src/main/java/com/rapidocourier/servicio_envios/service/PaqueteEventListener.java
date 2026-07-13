package com.rapidocourier.servicio_envios.service;

import com.rapidocourier.servicio_envios.config.RabbitMQConfig;
import com.rapidocourier.shared_kernel.event.PaqueteRegistradoEvent;
import com.rapidocourier.servicio_envios.entity.Agencia;
import com.rapidocourier.servicio_envios.entity.Envio;
import com.rapidocourier.servicio_envios.entity.EstadoEnvio;
import com.rapidocourier.servicio_envios.entity.HistorialEstadoEnvio;
import com.rapidocourier.servicio_envios.repository.AgenciaRepository;
import com.rapidocourier.servicio_envios.repository.EnvioRepository;
import com.rapidocourier.servicio_envios.repository.HistorialEstadoEnvioRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
// @Component
@RequiredArgsConstructor
public class PaqueteEventListener {

    private final EnvioRepository envioRepository;
    private final AgenciaRepository agenciaRepository;
    private final HistorialEstadoEnvioRepository historialRepository;

    @RabbitListener(queues = RabbitMQConfig.QUEUE_ENVIOS_NUEVO_PAQUETE)
    public void handlePaqueteRegistrado(PaqueteRegistradoEvent event) {
        log.info("Evento recibido: Nuevo paquete registrado con ID: {}", event.getPaqueteId());

        // En un caso real, el remitente podría elegir la agencia de origen y destino al momento de registrar el paquete.
        // Como no tenemos esa información en el evento (para mantenerlo simple), asignaremos la primera agencia por defecto
        // o crearemos una agencia "Borrador" si es necesario.
        // Aquí vamos a crear una lógica simple para asignarlo a una agencia "Virtual" o la primera disponible.

        Agencia agenciaBorrador = agenciaRepository.findByNombre("Agencia Central")
                .orElseGet(() -> {
                    Agencia a = new Agencia();
                    a.setNombre("Agencia Central");
                    a.setDireccion("Direccion Central");
                    return agenciaRepository.save(a);
                });

        Envio envio = new Envio();
        envio.setPaqueteId(event.getPaqueteId());
        envio.setDestinatarioId(event.getDestinatarioId());
        envio.setAgenciaOrigen(agenciaBorrador);
        envio.setAgenciaDestino(agenciaBorrador);
        envio.setCodigoRastreo("RC-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase());
        envio.setEstadoActual(EstadoEnvio.EN_AGENCIA_ORIGEN);

        envio = envioRepository.save(envio);

        HistorialEstadoEnvio historial = new HistorialEstadoEnvio(envio, EstadoEnvio.EN_AGENCIA_ORIGEN, "SISTEMA_MQ");
        historialRepository.save(historial);

        log.info("Envío creado automáticamente para el paquete: {}", event.getPaqueteId());
    }
}
