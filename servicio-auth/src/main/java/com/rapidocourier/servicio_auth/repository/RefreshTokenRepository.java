package com.rapidocourier.servicio_auth.repository;

import com.rapidocourier.servicio_auth.entity.RefreshToken;
import com.rapidocourier.servicio_auth.entity.Usuario;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;

import java.util.Optional;
import java.util.UUID;

public interface RefreshTokenRepository extends JpaRepository<RefreshToken, UUID> {

    Optional<RefreshToken> findByTokenHash(String tokenHash);

    java.util.List<RefreshToken> findByUsuarioEmailAndRevokedFalse(String email);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.usuario = :usuario")
    void revokeAllUserTokens(Usuario usuario);

    @Modifying
    @Query("UPDATE RefreshToken r SET r.revoked = true WHERE r.familyId = :familyId")
    void revokeTokenFamily(UUID familyId);

    int deleteByExpiresAtBefore(java.time.LocalDateTime dateTime);

}
