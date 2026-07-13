package com.rapidocourier.servicio_paquetes.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

@Entity
@Table(name = "paquetes")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Paquete {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal pesoKg;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal valorDeclarado;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal altoCm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal anchoCm;

    @Column(nullable = false, precision = 10, scale = 2)
    private BigDecimal largoCm;

    @Column(nullable = false)
    private UUID remitenteId;

    @Column(nullable = false)
    private UUID destinatarioId;

    @ManyToMany(fetch = FetchType.LAZY)
    @JoinTable(
            name = "paquetes_categorias",
            joinColumns = @JoinColumn(name = "paquete_id"),
            inverseJoinColumns = @JoinColumn(name = "categoria_id")
    )
    private Set<Categoria> categorias = new HashSet<>();

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}