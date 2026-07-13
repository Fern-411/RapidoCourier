package com.rapidocourier.servicio_clientes.repository;

import com.rapidocourier.servicio_clientes.entity.Cliente;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface ClienteRepository extends JpaRepository<Cliente, UUID> {
    Optional<Cliente> findByEmail(String email);
    Optional<Cliente> findByDni(String dni);
    boolean existsByEmail(String email);
    boolean existsByDni(String dni);
}
