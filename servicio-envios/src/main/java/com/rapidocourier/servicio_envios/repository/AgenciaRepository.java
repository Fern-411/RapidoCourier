package com.rapidocourier.servicio_envios.repository;

import com.rapidocourier.servicio_envios.entity.Agencia;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface AgenciaRepository extends JpaRepository<Agencia, UUID> {
    Optional<Agencia> findByNombre(String nombre);
}
