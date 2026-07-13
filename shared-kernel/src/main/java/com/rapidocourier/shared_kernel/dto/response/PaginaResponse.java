package com.rapidocourier.shared_kernel.dto.response;

import org.springframework.data.domain.Page;
import java.util.List;

/**
 * Wrapper estándar, inmutable y optimizado para representar respuestas paginadas.
 * Refactorizado a Record de Java 21 para asegurar compatibilidad nativa con Jackson y Redis.
 */
public record PaginaResponse<T>(
        List<T> contenido,
        int pagina,
        int tamano,
        long totalElementos,
        int totalPaginas,
        boolean ultima
) {

    /**
     * Constructor compacto para calcular de forma automática el campo 'ultima'.
     */
    public PaginaResponse {
        // Aseguramos que el contenido nunca viaje como nulo
        if (contenido == null) {
            contenido = List.of();
        }
    }

    /**
     * Mapea directamente un objeto Page de Spring Data a nuestra estructura estándar.
     */
    public static <T> PaginaResponse<T> desde(Page<T> dePagina) {
        return new PaginaResponse<>(
                dePagina.getContent(),
                dePagina.getNumber(),
                dePagina.getSize(),
                dePagina.getTotalElements(),
                dePagina.getTotalPages(),
                dePagina.isLast() // Aprovecha el método nativo de Spring Data
        );
    }

    /**
     * Permite la creación manual en caso de paginaciones personalizadas hechas a mano.
     * Mantiene compatibilidad exacta con las llamadas previas de tus servicios (Fix MarcaService).
     */
    public static <T> PaginaResponse<T> de(List<T> contenido, int pagina, int tamano,
                                           long totalElementos, int totalPaginas) {
        boolean esUltima = (pagina + 1) >= totalPaginas;
        return new PaginaResponse<>(contenido, pagina, tamano, totalElementos, totalPaginas, esUltima);
    }
}