package com.rapidocourier.servicio_auth;

import com.rapidocourier.servicio_auth.dto.request.LoginRequest;
import com.rapidocourier.servicio_auth.dto.request.RegisterRequest;
import com.rapidocourier.servicio_auth.dto.response.AuthResponse;
import com.rapidocourier.servicio_auth.entity.Rol;
import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.repository.RolRepository;
import com.rapidocourier.servicio_auth.repository.UsuarioRepository;
import com.rapidocourier.servicio_auth.security.JwtProvider;
import com.rapidocourier.servicio_auth.service.AuthService;
import com.rapidocourier.shared_kernel.exception.BaseException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UsuarioRepository usuarioRepository;

    @Mock
    private RolRepository rolRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtProvider jwtProvider;

    @Mock
    private com.rapidocourier.servicio_auth.service.RefreshTokenService refreshTokenService;

    @Mock
    private org.springframework.amqp.rabbit.core.RabbitTemplate rabbitTemplate;

    @InjectMocks
    private AuthService authService;

    private Usuario testUser;
    private Rol testRol;

    @BeforeEach
    void setUp() {
        testRol = new Rol("ADMIN");
        testUser = new Usuario();
        testUser.setEmail("admin@test.com");
        testUser.setPassword("encoded_password");
        testUser.setRol(testRol);
    }

    @Test
    void login_Success() {
        LoginRequest request = new LoginRequest("admin@test.com", "password");

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("password", "encoded_password")).thenReturn(true);
        when(jwtProvider.generarToken("admin@test.com", "ADMIN")).thenReturn("fake-jwt-token");
        when(refreshTokenService.createRefreshToken(testUser, "127.0.0.1", "Test-Agent")).thenReturn("fake-refresh-token");

        AuthResponse response = authService.login(request, "127.0.0.1", "Test-Agent");

        assertNotNull(response);
        assertEquals("fake-jwt-token", response.token());
        assertEquals("fake-refresh-token", response.refreshToken());
        assertEquals("admin@test.com", response.email());
        assertEquals("ADMIN", response.rol());
    }

    @Test
    void login_WrongPassword_ThrowsAuthException() {
        LoginRequest request = new LoginRequest("admin@test.com", "wrong_password");

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.of(testUser));
        when(passwordEncoder.matches("wrong_password", "encoded_password")).thenReturn(false);

        assertThrows(BaseException.class, () -> authService.login(request, "127.0.0.1", "Test-Agent"));

    }

    @Test
    void register_AsAdmin_Success() {
        RegisterRequest request = new RegisterRequest("Juan", "juan@test.com", "pass123", "CLIENTE", "123456789");
        Rol clienteRol = new Rol("CLIENTE");

        when(usuarioRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(rolRepository.findByNombre("CLIENTE")).thenReturn(Optional.of(clienteRol));
        when(passwordEncoder.encode("pass123")).thenReturn("encoded_pass123");
        when(usuarioRepository.save(any(Usuario.class))).thenReturn(new Usuario());

        authService.register(request, "ADMIN");

        verify(rabbitTemplate, times(1)).convertAndSend(anyString(), anyString(), any(com.rapidocourier.shared_kernel.event.NotificacionEvent.class));
    }

    @Test
    void register_NotAdmin_ThrowsAuthException() {
        RegisterRequest request = new RegisterRequest("Juan", "juan@test.com", "pass123", "CLIENTE", "123456789");

        assertThrows(BaseException.class, () -> authService.register(request, "CLIENTE"));
    }
}
