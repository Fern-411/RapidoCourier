package com.rapidocourier.servicio_paquetes.repository;

import com.rapidocourier.servicio_paquetes.entity.Categoria;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface CategoriaRepository extends JpaRepository<Categoria, UUID> {
    Optional<Categoria> findByNombre(String nombre);
}
