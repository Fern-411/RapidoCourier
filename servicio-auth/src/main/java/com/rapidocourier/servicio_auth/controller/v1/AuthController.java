package com.rapidocourier.servicio_auth.controller.v1;

import com.rapidocourier.servicio_auth.dto.request.*;
import com.rapidocourier.servicio_auth.dto.response.AuthResponse;
import com.rapidocourier.servicio_auth.dto.response.SessionResponse;
import com.rapidocourier.servicio_auth.service.AuthService;
import com.rapidocourier.servicio_auth.service.OAuth2Service;
import com.rapidocourier.servicio_auth.service.RefreshTokenService;
import com.rapidocourier.shared_kernel.dto.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Autenticación", description = "API para la gestión de acceso, inicio de sesión y registro de usuarios en el sistema")
public class AuthController {

    private final AuthService authService;
    private final OAuth2Service oauth2Service;
    private final RefreshTokenService refreshTokenService;

    @Operation(summary = "Login con Google", description = "Intercambia un ID Token de Google por un token JWT del sistema.")
    @PostMapping("/oauth2/google")
    public ResponseEntity<ApiResponse<AuthResponse>> loginGoogle(@Valid @RequestBody OAuth2LoginRequest request, HttpServletRequest requestHttp) {
        String ipAddress = requestHttp.getHeader("X-Forwarded-For");
        if (ipAddress == null) ipAddress = requestHttp.getRemoteAddr();
        String userAgent = requestHttp.getHeader("User-Agent");

        AuthResponse response = oauth2Service.loginConGoogle(request.getToken(), ipAddress, userAgent);
        return ResponseEntity.ok()
                .headers(createCookieHeaders(response))
                .body(ApiResponse.ok("Login con Google exitoso", response));
    }

    @Operation(summary = "Login con GitHub", description = "Intercambia un código de autorización de GitHub por un token JWT del sistema.")
    @PostMapping("/oauth2/github")
    public ResponseEntity<ApiResponse<AuthResponse>> loginGithub(@Valid @RequestBody OAuth2LoginRequest request, HttpServletRequest requestHttp) {
        String ipAddress = requestHttp.getHeader("X-Forwarded-For");
        if (ipAddress == null) ipAddress = requestHttp.getRemoteAddr();
        String userAgent = requestHttp.getHeader("User-Agent");

        AuthResponse response = oauth2Service.loginConGitHub(request.getToken(), ipAddress, userAgent);
        return ResponseEntity.ok()
                .headers(createCookieHeaders(response))
                .body(ApiResponse.ok("Login con GitHub exitoso", response));
    }

    @Operation(
            summary = "Iniciar sesión",
            description = "Valida las credenciales del usuario (email y contraseña) y devuelve un token JWT junto con los datos de sesión."
    )
    @PostMapping("/login")
    public ResponseEntity<ApiResponse<AuthResponse>> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest requestHttp) {

        String ipAddress = requestHttp.getHeader("X-Forwarded-For");
        if (ipAddress == null) ipAddress = requestHttp.getRemoteAddr();
        String userAgent = requestHttp.getHeader("User-Agent");

        AuthResponse response = authService.login(request, ipAddress, userAgent);
        return ResponseEntity.ok()
                .headers(createCookieHeaders(response))
                .body(ApiResponse.ok("Login exitoso", response));
    }

    @Operation(
            summary = "Registrar nuevo usuario",
            description = "Crea una nueva cuenta de usuario con el rol especificado. Por reglas de negocio, requiere permisos de administrador."
    )
    @PostMapping("/register")
    public ResponseEntity<ApiResponse<Void>> register(
            @Valid @RequestBody RegisterRequest request,

            @Parameter(description = "Rol del usuario que ejecuta la petición (generalmente inyectado internamente por el API Gateway)", required = false, example = "ADMIN")
            @RequestHeader(value = "X-User-Role", required = false) String executorRole) {

        authService.register(request, executorRole);
        return ResponseEntity.ok(ApiResponse.ok("Usuario registrado exitosamente. Por favor, verifique su correo.", null));
    }

    @Operation(summary = "Registro público", description = "Permite a nuevos usuarios registrarse con el rol predeterminado de CLIENTE.")
    @PostMapping("/signup")
    public ResponseEntity<ApiResponse<Void>> signup(@Valid @RequestBody SignupRequest request) {
        authService.signup(request);
        return ResponseEntity.ok(ApiResponse.ok("Registro de cliente exitoso. Por favor, verise su correo.", null));
    }

    @Operation(summary = "Verificar correo", description = "Valida el código de verificación de correo enviado al usuario.")
    @PostMapping("/verify-email")
    public ResponseEntity<ApiResponse<Void>> verifyEmail(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request);
        return ResponseEntity.ok(ApiResponse.ok("Correo verificado exitosamente. Por favor, ahora solicite y verifique su código por SMS.", null));
    }

    @Operation(summary = "Verificar teléfono", description = "Valida el código de verificación SMS enviado al usuario.")
    @PostMapping("/verify-phone")
    public ResponseEntity<ApiResponse<Void>> verifyPhone(@Valid @RequestBody VerifyEmailRequest request) {
        authService.verifyPhone(request);
        return ResponseEntity.ok(ApiResponse.ok("Teléfono verificado exitosamente. Ya puedes iniciar sesión.", null));
    }

    @Operation(summary = "Refrescar Token", description = "Obtiene un nuevo Access Token usando un Refresh Token válido.")
    @PostMapping("/refresh")
    public ResponseEntity<ApiResponse<AuthResponse>> refresh(@Valid @RequestBody RefreshTokenRequest request, HttpServletRequest requestHttp) {
        String ipAddress = requestHttp.getHeader("X-Forwarded-For");
        if (ipAddress == null) ipAddress = requestHttp.getRemoteAddr();
        String userAgent = requestHttp.getHeader("User-Agent");

        AuthResponse response = refreshTokenService.refreshAccessToken(request.refreshToken(), ipAddress, userAgent);
        return ResponseEntity.ok()
                .headers(createCookieHeaders(response))
                .body(ApiResponse.ok("Token refrescado exitosamente", response));
    }

    @Operation(summary = "Enviar OTP", description = "Genera y envía un código OTP por SMS o Email.")
    @PostMapping("/otp/send")
    public ResponseEntity<ApiResponse<Void>> sendOtp(@Valid @RequestBody OtpSendRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Código OTP enviado", null));
    }

    @Operation(summary = "Verificar Código OTP", description = "Verifica el código enviado al usuario. Si es correcto, devuelve un Access Token y Refresh Token.")
    @PostMapping("/otp/verify")
    public ResponseEntity<ApiResponse<AuthResponse>> verifyOtp(@Valid @RequestBody OtpVerifyRequest request, HttpServletRequest requestHttp) {
        String ipAddress = requestHttp.getHeader("X-Forwarded-For");
        if (ipAddress == null) ipAddress = requestHttp.getRemoteAddr();
        String userAgent = requestHttp.getHeader("User-Agent");

        AuthResponse response = authService.verifyOtp(request, ipAddress, userAgent);
        return ResponseEntity.ok()
                .headers(createCookieHeaders(response))
                .body(ApiResponse.ok("Verificación exitosa", response));
    }

    @Operation(summary = "Cerrar sesión", description = "Revoca el refresh token actual y todos los tokens de la misma familia.")
    @PostMapping("/logout")
    public ResponseEntity<ApiResponse<Void>> logout(@Valid @RequestBody LogoutRequest request) {
        authService.logout(request.refreshToken());
        return ResponseEntity.ok()
                .headers(createClearCookieHeaders())
                .body(ApiResponse.ok("Sesión cerrada correctamente", null));
    }

    @Operation(summary = "Cambiar Contraseña", description = "Cambia la contraseña de un usuario autenticado.")
    @PostMapping("/password/change")
    public ResponseEntity<ApiResponse<Void>> changePassword(
            @Valid @RequestBody CambiarPasswordRequest request,
            @RequestHeader("X-User-Email") String email) {
        authService.changePassword(email, request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña cambiada exitosamente", null));
    }

    @Operation(summary = "Olvidé mi contraseña", description = "Envía un OTP de reseteo al correo o número de contacto del usuario.")
    @PostMapping("/password/forgot")
    public ResponseEntity<ApiResponse<Void>> forgotPassword(@Valid @RequestBody OtpSendRequest request) {
        authService.sendOtp(request);
        return ResponseEntity.ok(ApiResponse.ok("Código de reseteo enviado", null));
    }

    @Operation(summary = "Resetear Contraseña", description = "Restablece la contraseña utilizando el código OTP como token de reseteo.")
    @PostMapping("/password/reset")
    public ResponseEntity<ApiResponse<Void>> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        authService.resetPassword(request);
        return ResponseEntity.ok(ApiResponse.ok("Contraseña restablecida exitosamente", null));
    }

    @Operation(summary = "Obtener sesiones activas", description = "Devuelve una lista de los dispositivos donde el usuario tiene iniciada la sesión.")
    @GetMapping("/sessions")
    public ResponseEntity<ApiResponse<java.util.List<SessionResponse>>> getActiveSessions(
            @RequestHeader("X-User-Email") String email) {
        return ResponseEntity.ok(ApiResponse.ok("Sesiones obtenidas", refreshTokenService.getActiveSessions(email)));
    }

    @Operation(summary = "Cerrar sesión en dispositivo", description = "Cierra sesión remotamente en un dispositivo específico.")
    @DeleteMapping("/sessions/{familyId}")
    public ResponseEntity<ApiResponse<Void>> revokeSession(
            @RequestHeader("X-User-Email") String email,
            @PathVariable java.util.UUID familyId) {
        refreshTokenService.revokeSession(email, familyId);
        return ResponseEntity.ok(ApiResponse.ok("Sesión revocada exitosamente", null));
    }

    // --- Métodos Auxiliares para Cookies ---
    private HttpHeaders createCookieHeaders(AuthResponse response) {
        HttpHeaders headers = new HttpHeaders();
        
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", response.token())
                .httpOnly(true)
                .secure(false) // TODO: Cambiar a true en producción (HTTPS)
                .path("/")
                .maxAge(3600) // 1 hora
                .sameSite("Strict")
                .build();
                
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", response.refreshToken())
                .httpOnly(true)
                .secure(false) // TODO: Cambiar a true en producción (HTTPS)
                .path("/")
                .maxAge(604800) // 7 días
                .sameSite("Strict")
                .build();
                
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }

    private HttpHeaders createClearCookieHeaders() {
        HttpHeaders headers = new HttpHeaders();
        
        ResponseCookie jwtCookie = ResponseCookie.from("jwt", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0) // 0 invalida la cookie inmediatamente
                .sameSite("Strict")
                .build();
                
        ResponseCookie refreshCookie = ResponseCookie.from("refresh_token", "")
                .httpOnly(true)
                .secure(false)
                .path("/")
                .maxAge(0)
                .sameSite("Strict")
                .build();
                
        headers.add(HttpHeaders.SET_COOKIE, jwtCookie.toString());
        headers.add(HttpHeaders.SET_COOKIE, refreshCookie.toString());
        return headers;
    }
}