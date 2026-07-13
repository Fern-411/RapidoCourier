package com.rapidocourier.servicio_envios.service;

import com.rapidocourier.servicio_envios.dto.request.AgenciaRequest;
import com.rapidocourier.servicio_envios.dto.response.AgenciaResponse;
import com.rapidocourier.servicio_envios.entity.Agencia;
import com.rapidocourier.servicio_envios.repository.AgenciaRepository;
import com.rapidocourier.shared_kernel.exception.BaseException;
import com.rapidocourier.servicio_envios.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AgenciaService {

    private final AgenciaRepository agenciaRepository;

    public AgenciaResponse crearAgencia(AgenciaRequest request) {
        if (agenciaRepository.findByNombre(request.nombre()).isPresent()) {
            throw new BaseException(ErrorCode.AGENCIA_YA_EXISTE);
        }
        Agencia agencia = new Agencia();
        agencia.setNombre(request.nombre());
        agencia.setDireccion(request.direccion());
        return mapToResponse(agenciaRepository.save(agencia));
    }

    public List<AgenciaResponse> obtenerTodas() {
        return agenciaRepository.findAll().stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    public AgenciaResponse obtenerPorId(UUID id) {
        return agenciaRepository.findById(id)
                .map(this::mapToResponse)
                .orElseThrow(() -> new BaseException(ErrorCode.AGENCIA_NO_ENCONTRADA));
    }

    public AgenciaResponse actualizarAgencia(UUID id, AgenciaRequest request) {
        Agencia agencia = agenciaRepository.findById(id)
                .orElseThrow(() -> new BaseException(ErrorCode.AGENCIA_NO_ENCONTRADA));

        if (!agencia.getNombre().equals(request.nombre()) && 
            agenciaRepository.findByNombre(request.nombre()).isPresent()) {
            throw new BaseException(ErrorCode.AGENCIA_YA_EXISTE);
        }

        agencia.setNombre(request.nombre());
        agencia.setDireccion(request.direccion());
        return mapToResponse(agenciaRepository.save(agencia));
    }

    public void eliminarAgencia(UUID id) {
        if (!agenciaRepository.existsById(id)) {
            throw new BaseException(ErrorCode.AGENCIA_NO_ENCONTRADA);
        }
        // TODO: Validar si la agencia tiene envíos asociados antes de eliminarla (opcional, o soft delete)
        agenciaRepository.deleteById(id);
    }

    private AgenciaResponse mapToResponse(Agencia agencia) {
        return new AgenciaResponse(agencia.getId(), agencia.getNombre(), agencia.getDireccion());
    }
}
