package com.rapidocourier.servicio_auth.service;

import com.rapidocourier.servicio_auth.dto.request.*;
import com.rapidocourier.servicio_auth.dto.response.AuthResponse;
import com.rapidocourier.servicio_auth.dto.response.UsuarioAdminResponse;
import com.rapidocourier.servicio_auth.dto.response.UsuarioDetalleResponse;
import com.rapidocourier.servicio_auth.entity.HistorialAcceso;
import com.rapidocourier.servicio_auth.entity.Rol;
import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.exception.ErrorCode;
import org.springframework.context.ApplicationEventPublisher;
import com.rapidocourier.servicio_auth.event.UserLoginEvent;
import com.rapidocourier.servicio_auth.repository.RolRepository;
import com.rapidocourier.servicio_auth.repository.UsuarioRepository;
import com.rapidocourier.servicio_auth.security.JwtProvider;
import com.rapidocourier.shared_kernel.event.NotificacionEvent;
import com.rapidocourier.shared_kernel.exception.BaseException;
import lombok.AllArgsConstructor;
import lombok.RequiredArgsConstructor;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class AuthService {

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtProvider jwtProvider;
    private final RefreshTokenService refreshTokenService;
    private final ApplicationEventPublisher eventPublisher;
    private final com.rapidocourier.servicio_auth.strategy.OtpSenderFactory otpSenderFactory;

    public AuthResponse login(LoginRequest request, String ipAddress, String userAgent) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElse(null);

        if (usuario == null) {
            registrarHistorial(null, request.email(), ipAddress, userAgent, "FALLO", "Usuario no encontrado");
            throw new BaseException(ErrorCode.CREDENCIALES_INCORRECTAS);
        }

        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(java.time.LocalDateTime.now())) {
            registrarHistorial(usuario, request.email(), ipAddress, userAgent, "BLOQUEADO", "Cuenta bloqueada temporalmente");
            throw new BaseException(ErrorCode.ACCESO_DENEGADO);
        }

        if (!passwordEncoder.matches(request.password(), usuario.getPassword())) {
            usuario.setIntentosFallidosLogin((usuario.getIntentosFallidosLogin() != null ? usuario.getIntentosFallidosLogin() : 0) + 1);
            if (usuario.getIntentosFallidosLogin() >= 5) {
                usuario.setBloqueadoHasta(java.time.LocalDateTime.now().plusMinutes(15));
                registrarHistorial(usuario, request.email(), ipAddress, userAgent, "BLOQUEADO", "Bloqueo por exceder intentos");
            } else {
                registrarHistorial(usuario, request.email(), ipAddress, userAgent, "FALLO", "Contraseña incorrecta");
            }
            usuarioRepository.save(usuario);
            throw new BaseException(ErrorCode.ACCESO_DENEGADO);
        }

        if (usuario.getIsEmailVerified() != null && !usuario.getIsEmailVerified()) {
            registrarHistorial(usuario, request.email(), ipAddress, userAgent, "FALLO", "Email no verificado");
            throw new BaseException(ErrorCode.CUENTA_NO_VERIFICADA);
        }

        if (usuario.getNumeroContacto() != null && !usuario.getNumeroContacto().isEmpty() && (usuario.getIsPhoneVerified() != null && !usuario.getIsPhoneVerified())) {
            registrarHistorial(usuario, request.email(), ipAddress, userAgent, "FALLO", "Teléfono no verificado");
            throw new BaseException(ErrorCode.CUENTA_NO_VERIFICADA);
        }

        usuario.setIntentosFallidosLogin(0);
        usuario.setBloqueadoHasta(null);
        usuarioRepository.save(usuario);

        String token = jwtProvider.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
        String refreshToken = refreshTokenService.createRefreshToken(usuario, ipAddress, userAgent);
        
        registrarHistorial(usuario, request.email(), ipAddress, userAgent, "EXITO", "Login exitoso");
        
        return new AuthResponse(token, refreshToken, usuario.getEmail(), usuario.getRol().getNombre());
    }

    private void registrarHistorial(Usuario usuario, String email, String ip, String userAgent, String estado, String motivo) {
        eventPublisher.publishEvent(new UserLoginEvent(usuario, email, ip, userAgent, estado, motivo));
    }

    public void register(RegisterRequest request, String executorRole) {
        if (!"ADMIN".equalsIgnoreCase(executorRole)) {
            throw new BaseException(ErrorCode.ACCESO_DENEGADO); // Usando tu código AUTH_008
        }

        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new BaseException(ErrorCode.CORREO_YA_EXISTE); // Usando tu código AUTH_001
        }

        Rol rol = rolRepository.findByNombre(request.rol().toUpperCase())
                .orElseThrow(() -> new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA));

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.nombre());
        nuevo.setEmail(request.email());
        nuevo.setPassword(passwordEncoder.encode(request.password()));
        nuevo.setRol(rol);
        nuevo.setNumeroContacto(request.numeroContacto());
        
        nuevo.setIsEmailVerified(false);
        nuevo.setIsPhoneVerified(false);
        String tokenVerificacion = String.format("%06d", new java.util.Random().nextInt(999999));
        nuevo.setCodigoVerificacion(tokenVerificacion);
        nuevo.setCodigoExpiracion(java.time.LocalDateTime.now().plusMinutes(10));
        nuevo.setIntentosVerificacion(0);

        usuarioRepository.save(nuevo);

        // Disparar OTP automáticamente
        try {
            sendOtp(new OtpSendRequest(nuevo.getEmail(), "EMAIL"));
        } catch (Exception e) {
            log.error("No se pudo enviar el OTP inicial para el usuario: " + request.email(), e);
        }
    }

    public void signup(SignupRequest request) {
        if (usuarioRepository.findByEmail(request.email()).isPresent()) {
            throw new BaseException(ErrorCode.CORREO_YA_EXISTE);
        }

        Rol rolCliente = rolRepository.findByNombre("CLIENTE")
                .orElseGet(() -> rolRepository.save(new Rol("CLIENTE")));

        Usuario nuevo = new Usuario();
        nuevo.setNombre(request.nombre());
        nuevo.setEmail(request.email());
        nuevo.setPassword(passwordEncoder.encode(request.password()));
        nuevo.setRol(rolCliente);
        nuevo.setNumeroContacto(request.numeroContacto());
        
        nuevo.setIsEmailVerified(false);
        nuevo.setIsPhoneVerified(false);
        String tokenVerificacion = String.format("%06d", new java.util.Random().nextInt(999999));
        nuevo.setCodigoVerificacion(tokenVerificacion);
        nuevo.setCodigoExpiracion(java.time.LocalDateTime.now().plusMinutes(10));
        nuevo.setIntentosVerificacion(0);

        usuarioRepository.save(nuevo);

        // Disparar OTP automáticamente
        try {
            sendOtp(new OtpSendRequest(nuevo.getEmail(), "EMAIL"));
        } catch (Exception e) {
            log.error("No se pudo enviar el OTP inicial para el usuario: " + request.email(), e);
        }
    }

    public void verifyEmail(VerifyEmailRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        if (usuario.getIsEmailVerified() != null && usuario.getIsEmailVerified()) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpiracion() == null) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        validarYRegistrarFalloOtp(usuario, request.codigo());

        usuario.setIsEmailVerified(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        usuario.setIntentosVerificacion(0);
        usuario.setUltimoEnvioOtp(null); // Resetear cooldown para permitir siguiente OTP inmediatamente
        usuarioRepository.save(usuario);
    }

    public void verifyPhone(VerifyEmailRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        if (usuario.getIsPhoneVerified() != null && usuario.getIsPhoneVerified()) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA); // Ya verificado
        }

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpiracion() == null) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        validarYRegistrarFalloOtp(usuario, request.codigo());

        usuario.setIsPhoneVerified(true);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        usuario.setIntentosVerificacion(0);
        usuarioRepository.save(usuario);
    }

    // ── OTP METHODS ──────────────────────────────────────────────

    private final RabbitTemplate rabbitTemplate;

    public void sendOtp(OtpSendRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BaseException(ErrorCode.CREDENCIALES_INCORRECTAS));

        // Verificar cooldown de 60 segundos
        if (usuario.getUltimoEnvioOtp() != null && usuario.getUltimoEnvioOtp().plusSeconds(60).isAfter(java.time.LocalDateTime.now())) {
            throw new BaseException(ErrorCode.ACCESO_DENEGADO); // Puedes crear un error TOO_MANY_REQUESTS si prefieres
        }

        // Generar código de 6 dígitos
        String codigo = String.format("%06d", new java.util.Random().nextInt(999999));
        
        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiracion(java.time.LocalDateTime.now().plusMinutes(10));
        usuario.setIntentosVerificacion(0);
        usuario.setUltimoEnvioOtp(java.time.LocalDateTime.now());
        usuarioRepository.save(usuario);

        String canal = request.canal() != null ? request.canal() : "EMAIL";
        com.rapidocourier.servicio_auth.strategy.OtpSenderStrategy strategy = otpSenderFactory.getStrategy(canal);
        strategy.sendOtp(usuario, codigo);
    }

    public AuthResponse verifyOtp(OtpVerifyRequest request, String ipAddress, String userAgent) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BaseException(ErrorCode.CREDENCIALES_INCORRECTAS));

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpiracion() == null) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        validarYRegistrarFalloOtp(usuario, request.codigo());

        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        usuario.setIntentosVerificacion(0);
        usuarioRepository.save(usuario);

        String token = jwtProvider.generarToken(usuario.getEmail(), usuario.getRol().getNombre());
        String refreshToken = refreshTokenService.createRefreshToken(usuario, ipAddress, userAgent);
        return new AuthResponse(token, refreshToken, usuario.getEmail(), usuario.getRol().getNombre());
    }

    public void logout(String refreshToken) {
        refreshTokenService.revokeTokenFamily(refreshToken);
    }

    @org.springframework.cache.annotation.CacheEvict(value = "usuarios", key = "#email")
    public void changePassword(String email, CambiarPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        if (!passwordEncoder.matches(request.passwordActual(), usuario.getPassword())) {
            throw new BaseException(ErrorCode.CREDENCIALES_INCORRECTAS);
        }

        usuario.setPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuarioRepository.save(usuario);
        
        // Revocamos todos los tokens activos
        refreshTokenService.revokeAllUserTokens(usuario);
    }

    public void resetPassword(ResetPasswordRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(request.email())
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpiracion() == null) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        validarYRegistrarFalloOtp(usuario, request.resetToken());

        usuario.setPassword(passwordEncoder.encode(request.nuevaPassword()));
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        usuario.setIntentosVerificacion(0);
        usuarioRepository.save(usuario);
        
        // Revocamos todos los tokens activos
        refreshTokenService.revokeAllUserTokens(usuario);
    }

    @org.springframework.cache.annotation.Cacheable(value = "usuarios", key = "#email")
    public UsuarioDetalleResponse getProfile(String email) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));
        return new com.rapidocourier.servicio_auth.dto.response.UsuarioDetalleResponse(
                usuario.getNombre(), usuario.getEmail(), usuario.getNumeroContacto(), usuario.getRol().getNombre()
        );
    }

    @org.springframework.cache.annotation.CachePut(value = "usuarios", key = "#email")
    public UsuarioDetalleResponse updateProfile(String email, ActualizarPerfilRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));
        
        usuario.setNombre(request.nombre());
        usuario.setNumeroContacto(request.numeroContacto());
        usuarioRepository.save(usuario);
        
        return new UsuarioDetalleResponse(
                usuario.getNombre(), usuario.getEmail(), usuario.getNumeroContacto(), usuario.getRol().getNombre()
        );
    }

    public void requestEmailChange(String email, EmailChangeRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        if (usuarioRepository.findByEmail(request.nuevoEmail()).isPresent()) {
            throw new BaseException(ErrorCode.CORREO_YA_EXISTE);
        }

        String codigo = String.format("%06d", new java.util.Random().nextInt(999999));
        
        usuario.setCodigoVerificacion(codigo);
        usuario.setCodigoExpiracion(java.time.LocalDateTime.now().plusMinutes(10));
        usuario.setIntentosVerificacion(0);
        usuario.setNuevoEmailPendiente(request.nuevoEmail());
        usuarioRepository.save(usuario);

        NotificacionEvent event = new NotificacionEvent();
        event.setDestinatario(request.nuevoEmail()); // Send to new email!
        event.setTipoNotificacion("OTP_AUTH");
        event.setAsunto("Confirma tu nuevo correo - Rapido Courier");
        event.setMensaje("Tu código para confirmar este correo es: " + codigo + ". Expirará en 10 minutos.");

        try {
            rabbitTemplate.convertAndSend("notificaciones.exchange", "notificaciones.routing.key", event);
        } catch (Exception e) {
            System.err.println("Error al enviar OTP de cambio de email por RabbitMQ: " + e.getMessage());
            throw new BaseException(ErrorCode.ERROR_INTERNO);
        }
    }

    public void verifyEmailChange(String email, EmailChangeVerifyRequest request) {
        Usuario usuario = usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        if (usuario.getCodigoVerificacion() == null || usuario.getCodigoExpiracion() == null || usuario.getNuevoEmailPendiente() == null) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        validarYRegistrarFalloOtp(usuario, request.codigo());

        usuario.setEmail(usuario.getNuevoEmailPendiente());
        usuario.setNuevoEmailPendiente(null);
        usuario.setCodigoVerificacion(null);
        usuario.setCodigoExpiracion(null);
        usuario.setIntentosVerificacion(0);
        usuarioRepository.save(usuario);
    }

    public java.util.List<UsuarioAdminResponse> listarUsuarios(String executorRole) {
        if (!"ADMIN".equalsIgnoreCase(executorRole)) {
            throw new BaseException(ErrorCode.ACCESO_DENEGADO);
        }
        return usuarioRepository.findAll().stream()
                .map(u -> new UsuarioAdminResponse(
                        u.getId(), u.getNombre(), u.getEmail(), u.getNumeroContacto(), u.getRol().getNombre()
                )).toList();
    }

    public void cambiarRolUsuario(java.util.UUID usuarioId, CambiarRolRequest request, String executorRole) {
        if (!"ADMIN".equalsIgnoreCase(executorRole)) {
            throw new BaseException(ErrorCode.ACCESO_DENEGADO);
        }

        Usuario usuario = usuarioRepository.findById(usuarioId)
                .orElseThrow(() -> new BaseException(ErrorCode.USUARIO_NO_ENCONTRADO));

        Rol nuevoRol = rolRepository.findByNombre(request.nuevoRol().toUpperCase())
                .orElseThrow(() -> new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA));

        usuario.setRol(nuevoRol);
        usuarioRepository.save(usuario);
        
        // Revocamos todos los tokens para obligarlo a loguearse con el nuevo rol
        refreshTokenService.revokeAllUserTokens(usuario);
    }

    private void validarYRegistrarFalloOtp(Usuario usuario, String codigoIngresado) {
        if (usuario.getBloqueadoHasta() != null && usuario.getBloqueadoHasta().isAfter(java.time.LocalDateTime.now())) {
            throw new BaseException(ErrorCode.ACCESO_DENEGADO);
        }
        
        if (usuario.getCodigoExpiracion().isBefore(java.time.LocalDateTime.now())) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA);
        }

        if (!usuario.getCodigoVerificacion().equals(codigoIngresado)) {
            int intentos = (usuario.getIntentosVerificacion() != null ? usuario.getIntentosVerificacion() : 0) + 1;
            usuario.setIntentosVerificacion(intentos);
            
            if (intentos >= 3) {
                usuario.setBloqueadoHasta(java.time.LocalDateTime.now().plusMinutes(15));
                usuarioRepository.save(usuario);
                throw new BaseException(ErrorCode.ACCESO_DENEGADO); // Bloqueado
            }
            usuarioRepository.save(usuario);
            throw new BaseException(ErrorCode.CODIGO_INVALIDO);
        }
    }
}
