package com.rapidocourier.servicio_envios.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "envios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Envio {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, unique = true, length = 50)
    private String codigoRastreo;

    @Column(name = "numero_orden", unique = true, length = 20)
    private String numeroOrden;

    @Column(nullable = false)
    private UUID paqueteId;

    @Column(nullable = false)
    private UUID destinatarioId;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agencia_origen_id", nullable = false)
    private Agencia agenciaOrigen;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "agencia_destino_id", nullable = false)
    private Agencia agenciaDestino;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private EstadoEnvio estadoActual = EstadoEnvio.EN_AGENCIA_ORIGEN;

    @Column(length = 6)
    private String claveRecojo; // PIN de 6 dígitos

    @Column(name = "intentos_fallidos_recojo")
    private Integer intentosFallidosRecojo = 0;

    @Column(name = "recojo_bloqueado")
    private Boolean recojoBloqueado = false;

    @Column(name = "otp_desbloqueo", length = 6)
    private String otpDesbloqueo;

    @Column(name = "otp_desbloqueo_expiracion")
    private LocalDateTime otpDesbloqueoExpiracion;

    @Column(name = "url_boleta", length = 500)
    private String urlBoleta;

    @Column(name = "url_guia", length = 500)
    private String urlGuia;

    @Enumerated(EnumType.STRING)
    @Column(name = "tipo_pago", length = 20)
    private TipoPago tipoPago = TipoPago.ORIGEN;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;
}
