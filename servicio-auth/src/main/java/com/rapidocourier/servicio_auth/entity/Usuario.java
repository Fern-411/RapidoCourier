package com.rapidocourier.servicio_auth.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;

@Entity
@Table(name = "usuarios")
@AllArgsConstructor
@NoArgsConstructor
@Getter
@Setter
public class Usuario {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 150)
    private String nombre;

    @Column(nullable = false, unique = true, length = 150)
    private String email;

    @Column(nullable = true)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(name = "oauth2_provider", length = 20)
    private OAuth2Provider oauth2Provider = OAuth2Provider.LOCAL;

    @Column(name = "oauth2_id", length = 150)
    private String oauth2Id;

    @ManyToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "rol_id", nullable = false)
    private Rol rol;

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at")
    private LocalDateTime updatedAt;

    @Column(name = "numero_contacto", length = 20)
    private String numeroContacto;

    @Column(name = "is_email_verified", nullable = false)
    private Boolean isEmailVerified = false;

    @Column(name = "is_phone_verified", nullable = false)
    private Boolean isPhoneVerified = false;

    @Column(name = "intentos_fallidos_login")
    private Integer intentosFallidosLogin = 0;

    @Column(name = "bloqueado_hasta")
    private LocalDateTime bloqueadoHasta;

    @Column(name = "codigo_verificacion", length = 255)
    private String codigoVerificacion;

    @Column(name = "codigo_expiracion")
    private LocalDateTime codigoExpiracion;

    @Column(name = "intentos_verificacion")
    private Integer intentosVerificacion = 0;

    @Column(name = "nuevo_email_pendiente", length = 150)
    private String nuevoEmailPendiente;

    @Column(name = "ultimo_envio_otp")
    private LocalDateTime ultimoEnvioOtp;

}
