package com.rapidocourier.servicio_auth.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "historial_accesos", indexes = {
        @Index(name = "idx_historial_usuario", columnList = "usuario_id"),
        @Index(name = "idx_historial_fecha", columnList = "fecha_intento")
})
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class HistorialAcceso {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "usuario_id")
    private Usuario usuario; // Puede ser null si el intento fue con un email inexistente

    @Column(name = "email_intentado", length = 150)
    private String emailIntentado;

    @Column(name = "ip_address", length = 50)
    private String ipAddress;

    @Column(name = "user_agent", length = 500)
    private String userAgent;

    @Column(name = "estado", length = 20, nullable = false)
    private String estado; // EXITO, FALLO, BLOQUEADO

    @Column(name = "motivo_fallo", length = 255)
    private String motivoFallo;

    @CreationTimestamp
    @Column(name = "fecha_intento", nullable = false, updatable = false)
    private LocalDateTime fechaIntento;
}
