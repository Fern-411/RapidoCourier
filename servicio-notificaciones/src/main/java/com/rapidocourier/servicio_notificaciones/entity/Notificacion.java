package com.rapidocourier.servicio_notificaciones.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "notificaciones")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Notificacion {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private UUID paqueteId;

    @Column(nullable = false, length = 500)
    private String mensaje;

    @Column(nullable = false, length = 20)
    private String estado = "ENVIADA";

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime fechaEnvio;

}
