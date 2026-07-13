package com.rapidocourier.servicio_auth.config;

import com.rapidocourier.servicio_auth.repository.RefreshTokenRepository;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class TokenCleanupScheduler {

    private static final Logger logger = LoggerFactory.getLogger(TokenCleanupScheduler.class);
    private final RefreshTokenRepository refreshTokenRepository;

    // Ejecuta todos los días a las 2 AM
    @Scheduled(cron = "0 0 2 * * ?")
    @Transactional
    public void limpiarTokensExpirados() {
        logger.info("Iniciando limpieza de Refresh Tokens expirados/revocados...");
        
        // Asumimos que podemos borrar aquellos expirados
        int eliminadosExpirados = refreshTokenRepository.deleteByExpiresAtBefore(LocalDateTime.now());

        logger.info("Limpieza completada. {} tokens expirados eliminados.", eliminadosExpirados);
    }
}
