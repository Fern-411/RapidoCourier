package com.rapidocourier.servicio_auth.service;

import com.rapidocourier.servicio_auth.dto.response.AuthResponse;
import com.rapidocourier.servicio_auth.dto.response.SessionResponse;
import com.rapidocourier.servicio_auth.entity.RefreshToken;
import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.exception.ErrorCode;
import com.rapidocourier.servicio_auth.repository.RefreshTokenRepository;
import com.rapidocourier.servicio_auth.repository.UsuarioRepository;
import com.rapidocourier.servicio_auth.security.JwtProvider;
import com.rapidocourier.shared_kernel.exception.BaseException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;
    private final UsuarioRepository usuarioRepository;
    private final JwtProvider jwtProvider;

    private final long refreshExpiration = 604800000L; // 7 días en ms

    @Transactional
    public String createRefreshToken(Usuario usuario, String ipAddress, String userAgent) {
        String familia = UUID.randomUUID().toString();
        return generateAndSaveToken(usuario, familia, ipAddress, userAgent);
    }

    private String generateAndSaveToken(Usuario usuario, String familia, String ipAddress, String userAgent) {
        byte[] randomBytes = new byte[64];
        new SecureRandom().nextBytes(randomBytes);
        String tokenStr = Base64.getUrlEncoder().withoutPadding().encodeToString(randomBytes);
        String tokenHash = hashToken(tokenStr);

        RefreshToken token = new RefreshToken();
        token.setUsuario(usuario);
        token.setTokenHash(tokenHash);
        token.setFamilyId(UUID.fromString(familia));
        token.setRevoked(false);
        token.setIpAddress(ipAddress);
        token.setUserAgent(userAgent);
        token.setExpiresAt(LocalDateTime.now().plusNanos(refreshExpiration * 1_000_000));
        refreshTokenRepository.save(token);

        return tokenStr;
    }

    @Transactional
    public AuthResponse refreshAccessToken(String tokenPlano, String ipAddress, String userAgent) {
        String hash = hashToken(tokenPlano);
        RefreshToken refreshToken = refreshTokenRepository.findByTokenHash(hash)
                .orElseThrow(() -> new BaseException(ErrorCode.ACCESO_DENEGADO)); // Token inválido

        if (refreshToken.isRevoked()) {
            // Anti-replay: Se intentó usar un token revocado. Invalidar toda la familia.
            refreshTokenRepository.revokeTokenFamily(refreshToken.getFamilyId());
            throw new BaseException(ErrorCode.ACCESO_DENEGADO); // Por reuso
        }

        if (refreshToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            refreshToken.setRevoked(true);
            refreshTokenRepository.save(refreshToken);
            throw new BaseException(ErrorCode.ACCESO_DENEGADO); // Token expirado
        }

        // Rotar el token actual (revocarlo)
        refreshToken.setRevoked(true);
        refreshTokenRepository.save(refreshToken);

        Usuario usuario = refreshToken.getUsuario();

        // Emitir nuevo Access Token
        String newAccessToken = jwtProvider.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
        // Emitir nuevo Refresh Token usando la misma familia
        String newRefreshToken = generateAndSaveToken(usuario, refreshToken.getFamilyId().toString(), ipAddress, userAgent);

        return new AuthResponse(newAccessToken, newRefreshToken, usuario.getEmail(), usuario.getRol().getNombre());
    }

    @Transactional
    public void revokeTokenFamily(String tokenPlano) {
        String hash = hashToken(tokenPlano);
        refreshTokenRepository.findByTokenHash(hash).ifPresent(token -> {
            refreshTokenRepository.revokeTokenFamily(token.getFamilyId());
        });
    }

    @Transactional
    public void revokeAllUserTokens(Usuario usuario) {
        refreshTokenRepository.revokeAllUserTokens(usuario);
    }

    public java.util.List<SessionResponse> getActiveSessions(String email) {
        return refreshTokenRepository.findByUsuarioEmailAndRevokedFalse(email).stream()
                .filter(t -> t.getExpiresAt().isAfter(LocalDateTime.now()))
                .map(t -> new SessionResponse(t.getFamilyId(), t.getIpAddress(), t.getUserAgent(), t.getCreatedAt()))
                .toList();
    }

    @Transactional
    public void revokeSession(String email, UUID familyId) {
        refreshTokenRepository.findByUsuarioEmailAndRevokedFalse(email).stream()
                .filter(t -> t.getFamilyId().equals(familyId))
                .findFirst()
                .ifPresent(token -> refreshTokenRepository.revokeTokenFamily(familyId));
    }

    private String hashToken(String token) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes = digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getEncoder().encodeToString(hashBytes);
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Error hashing token", e);
        }
    }
}
