package com.rapidocourier.servicio_envios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_estado_envios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class HistorialEstadoEnvio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "envio_id", nullable = false)
    private Envio envio;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoEnvio estado;

    @CreationTimestamp
    @Column(name = "fecha_cambio", updatable = false)
    private LocalDateTime fechaCambio;

    @Column(nullable = false, length = 100)
    private String usuarioResponsable;
    
    public HistorialEstadoEnvio(Envio envio, EstadoEnvio estado, String usuarioResponsable) {
        this.envio = envio;
        this.estado = estado;
        this.usuarioResponsable = usuarioResponsable;
    }
}
