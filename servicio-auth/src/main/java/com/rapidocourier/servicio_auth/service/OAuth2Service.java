package com.rapidocourier.servicio_auth.service;

import com.rapidocourier.servicio_auth.dto.response.AuthResponse;
import com.rapidocourier.servicio_auth.entity.OAuth2Provider;
import com.rapidocourier.servicio_auth.entity.Rol;
import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.exception.ErrorCode;
import com.rapidocourier.servicio_auth.repository.RolRepository;
import com.rapidocourier.servicio_auth.repository.UsuarioRepository;
import com.rapidocourier.servicio_auth.security.JwtProvider;
import com.rapidocourier.shared_kernel.exception.BaseException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.client.RestClient;

import java.util.List;
import java.util.Map;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class OAuth2Service {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @Value("${app.oauth2.google.client-id:}")
    private String googleClientId;

    @Value("${app.oauth2.github.client-id:}")
    private String githubClientId;

    @Value("${app.oauth2.github.client-secret:}")
    private String githubClientSecret;

    // ── GOOGLE LOGIN ──────────────────────────────────────────────

    @Transactional
    public AuthResponse loginConGoogle(String idToken, String ipAddress, String userAgent) {
        Map<String, Object> payload = verificarGoogleToken(idToken);

        String email = (String) payload.get("email");
        String nombre = (String) payload.get("name");
        String googleId = (String) payload.get("sub");
        boolean emailVerified = Boolean.parseBoolean(String.valueOf(payload.get("email_verified")));

        if (email == null || !emailVerified) {
            throw new BaseException(ErrorCode.OAUTH2_EMAIL_NO_VERIFICADO);
        }

        String audience = (String) payload.get("aud");
        if (!googleClientId.isEmpty() && !googleClientId.equals(audience)) {
            log.warn("[servicio-auth] Google token con aud incorrecto: {}", audience);
            throw new BaseException(ErrorCode.OAUTH2_TOKEN_INVALIDO);
        }

        Usuario usuario = encontrarOCrearUsuarioOAuth2(email, nombre, OAuth2Provider.GOOGLE, googleId);
        return construirRespuestaAuth(usuario, ipAddress, userAgent);
    }

    // ── GITHUB LOGIN ──────────────────────────────────────────────

    @Transactional
    public AuthResponse loginConGitHub(String code, String ipAddress, String userAgent) {

        // 1. Intercambiar el CODE por el ACCESS TOKEN
        String tokenUrl = "https://github.com/login/oauth/access_token"
                + "?client_id=" + githubClientId
                + "&client_secret=" + githubClientSecret
                + "&code=" + code;

        RestClient restClient = RestClient.create();
        Map<String, Object> tokenResponse = restClient.post()
                .uri(tokenUrl)
                .header(HttpHeaders.ACCEPT, "application/json")
                .retrieve()
                .body(new ParameterizedTypeReference<>() {});

        if (tokenResponse == null || tokenResponse.get("access_token") == null) {
            throw new BaseException(ErrorCode.OAUTH2_TOKEN_INVALIDO);
        }

        String githubAccessToken = (String) tokenResponse.get("access_token");

        // 2. Usar access_token para obtener perfil
        Map<String, Object> userInfo = obtenerInfoGitHub(githubAccessToken);

        String email = (String) userInfo.get("email");
        String nombre = (String) userInfo.get("name");
        String githubId = String.valueOf(userInfo.get("id"));

        if (email == null) {
            email = obtenerEmailPrimarioGitHub(githubAccessToken);
        }

        if (email == null) {
            throw new BaseException(ErrorCode.OAUTH2_EMAIL_NO_VERIFICADO);
        }

        if (nombre == null || nombre.isBlank()) {
            nombre = (String) userInfo.get("login");
        }

        Usuario usuario = encontrarOCrearUsuarioOAuth2(email, nombre, OAuth2Provider.GITHUB, githubId);
        return construirRespuestaAuth(usuario, ipAddress, userAgent);
    }

    // ── LÓGICA COMPARTIDA ─────────────────────────────────────────

    private Usuario encontrarOCrearUsuarioOAuth2(String email, String nombre,
                                                 OAuth2Provider provider, String oauth2Id) {
        
        Optional<Usuario> existenteByProvider = usuarioRepository.findByEmail(email); // O podemos agregar findByOauth2ProviderAndOauth2Id
        
        if (existenteByProvider.isPresent()) {
            Usuario existente = existenteByProvider.get();
            existente.setOauth2Provider(provider);
            existente.setOauth2Id(oauth2Id);
            existente.setIsEmailVerified(true); // Google/GitHub ya validó este correo
            log.info("[servicio-auth] Cuenta vinculada con {}: {}", provider, email);
            return usuarioRepository.save(existente);
        } else {
            Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                    .orElseGet(() -> rolRepository.save(new Rol("CLIENTE")));

            Usuario nuevo = new Usuario();
            nuevo.setEmail(email);
            nuevo.setNombre(nombre);
            nuevo.setRol(rolCliente);
            nuevo.setOauth2Provider(provider);
            nuevo.setOauth2Id(oauth2Id);
            nuevo.setIsEmailVerified(true); // Google/GitHub ya validó este correo
            // Password no se setea, quedará null que está permitido ahora.

            log.info("[servicio-auth] Nuevo usuario OAuth2 con {}: {}", provider, email);
            Usuario guardado = usuarioRepository.save(nuevo);
            
            // Emitir evento de bienvenida para nuevo usuario
            com.rapidocourier.shared_kernel.event.NotificacionEvent event = new com.rapidocourier.shared_kernel.event.NotificacionEvent(
                    null,
                    guardado.getEmail(),
                    null,
                    "WELCOME_EMAIL",
                    "¡Bienvenido a Rapido Courier!",
                    "Hola " + guardado.getNombre() + ", nos alegra tenerte aquí. Tu cuenta ha sido creada exitosamente mediante " + provider.name() + "."
            );
            rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.routing.key", event);

            return guardado;
        }
    }

    // ── VERIFICACIÓN CON PROVEEDORES ──────────────────────────────

    private Map<String, Object> verificarGoogleToken(String idToken) {
        try {
            RestClient client = RestClient.create();
            return client.get()
                    .uri("https://oauth2.googleapis.com/tokeninfo?id_token={token}", idToken)
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.warn("[servicio-auth] Error verificando token de Google: {}", e.getMessage());
            throw new BaseException(ErrorCode.OAUTH2_TOKEN_INVALIDO);
        }
    }

    private Map<String, Object> obtenerInfoGitHub(String accessToken) {
        try {
            RestClient client = RestClient.create();
            return client.get()
                    .uri("https://api.github.com/user")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});
        } catch (Exception e) {
            log.warn("[servicio-auth] Error verificando token de GitHub: {}", e.getMessage());
            throw new BaseException(ErrorCode.OAUTH2_TOKEN_INVALIDO);
        }
    }

    private String obtenerEmailPrimarioGitHub(String accessToken) {
        try {
            RestClient client = RestClient.create();
            List<Map<String, Object>> emails = client.get()
                    .uri("https://api.github.com/user/emails")
                    .header(HttpHeaders.AUTHORIZATION, "Bearer " + accessToken)
                    .header(HttpHeaders.ACCEPT, "application/vnd.github+json")
                    .retrieve()
                    .body(new ParameterizedTypeReference<>() {});

            if (emails != null) {
                return emails.stream()
                        .filter(e -> Boolean.TRUE.equals(e.get("primary"))
                                && Boolean.TRUE.equals(e.get("verified")))
                        .map(e -> (String) e.get("email"))
                        .findFirst()
                        .orElse(null);
            }
        } catch (Exception e) {
            log.warn("[servicio-auth] No se pudo obtener email de GitHub: {}", e.getMessage());
        }
        return null;
    }

    // ── BUILDER AUTH RESPONSE ─────────────────────────────────────

    private AuthResponse construirRespuestaAuth(Usuario usuario, String ipAddress, String userAgent) {
        String token = jwtProvider.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
        String refreshToken = refreshTokenService.createRefreshToken(usuario, ipAddress, userAgent);
        return new AuthResponse(token, refreshToken, usuario.getEmail(), usuario.getRol().getNombre());
    }
}
