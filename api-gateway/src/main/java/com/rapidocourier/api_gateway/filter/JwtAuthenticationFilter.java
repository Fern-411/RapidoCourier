package com.rapidocourier.api_gateway.filter;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.server.reactive.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Component
public class JwtAuthenticationFilter implements GlobalFilter, Ordered {

    @Value("${jwt.secret}")
    private String jwtSecret;

    // Rutas públicas que no requieren JWT
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/auth/login",
            "/api/v1/auth/signup",
            "/api/v1/auth/verify-email",
            "/api/v1/auth/verify-phone",
            "/api/v1/auth/oauth2/",
            "/api/v1/auth/otp/",
            "/api/v1/auth/password/forgot",
            "/api/v1/auth/password/reset",
            "/api/v1/auth/refresh",
            "/api/v1/envios/rastreo/",
            "/actuator"
    );

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        String path = exchange.getRequest().getPath().toString();

        String ip = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (ip == null && exchange.getRequest().getRemoteAddress() != null) {
            ip = exchange.getRequest().getRemoteAddress().getAddress().getHostAddress();
        }
        String finalIp = ip != null ? ip : "unknown";

        String userAgent = exchange.getRequest().getHeaders().getFirst("User-Agent");
        String finalUserAgent = userAgent != null ? userAgent : "unknown";

        ServerWebExchange requestWithClientInfo = exchange.mutate()
                .request(exchange.getRequest().mutate()
                        .header("X-Forwarded-For", finalIp)
                        .header("User-Agent", finalUserAgent)
                        .build())
                .build();
        if (exchange.getRequest().getMethod() == org.springframework.http.HttpMethod.OPTIONS) {
            return chain.filter(requestWithClientInfo);
        }

        if (isPublicPath(path)) {
            return chain.filter(requestWithClientInfo);
        }

        org.springframework.http.HttpCookie jwtCookie = requestWithClientInfo.getRequest().getCookies().getFirst("jwt");

        if (jwtCookie == null || jwtCookie.getValue() == null || jwtCookie.getValue().isBlank()) {
            return respondUnauthorized(exchange, "Token de autenticación requerido");
        }

        String token = jwtCookie.getValue();

        try {
            Claims claims = extractClaims(token);

            ServerWebExchange mutated = requestWithClientInfo.mutate()
                    .request(requestWithClientInfo.getRequest().mutate()
                            .header("X-User-Email", claims.getSubject())
                            .header("X-User-Role", claims.get("role", String.class))
                            .build())
                    .build();
            return chain.filter(mutated);
        } catch (JwtException | IllegalArgumentException e) {
            return respondUnauthorized(requestWithClientInfo, "Token inválido o expirado");
        }
    }

    private Claims extractClaims(String token) {
        // CORRECCIÓN 2: Uso de SecretKey y la nueva sintaxis de JJWT 0.12+
        SecretKey key = Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
        return Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isPublicPath(String path) {
        if (PUBLIC_PATHS.stream().anyMatch(path::startsWith)) {
            return true;
        }
        if (path.startsWith("/api/v1/envios/") && path.endsWith("/historial")) {
            return true;
        }
        return false;
    }

    private Mono<Void> respondUnauthorized(ServerWebExchange exchange, String mensaje) {
        ServerHttpResponse response = exchange.getResponse();
        response.setStatusCode(HttpStatus.UNAUTHORIZED);
        response.getHeaders().setContentType(MediaType.APPLICATION_JSON);
        String body = """
                {"success":false,"message":"%s","data":null}
                """.formatted(mensaje);
        DataBuffer buffer = response.bufferFactory()
                .wrap(body.getBytes(StandardCharsets.UTF_8));
        return response.writeWith(Mono.just(buffer));
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE + 1;
    }

}