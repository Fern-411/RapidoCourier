package com.rapidocourier.servicio_auth.exception;

import com.rapidocourier.shared_kernel.exception.CodigoError;
import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode implements CodigoError {

    // ── Autenticación y Registro ──────────────────────────────────
    CORREO_YA_EXISTE("AUTH_001", "El correo electrónico ya está registrado", HttpStatus.CONFLICT),
    CREDENCIALES_INCORRECTAS("AUTH_003", "Usuario o contraseña incorrectos", HttpStatus.UNAUTHORIZED),
    USUARIO_NO_ENCONTRADO("AUTH_004", "El usuario solicitado no existe", HttpStatus.NOT_FOUND),

    // ── Tokens y Sesión ──────────────────────────────────────────
    TOKEN_INVALIDO("AUTH_002", "El token proporcionado no es válido", HttpStatus.UNAUTHORIZED),
    TOKEN_EXPIRADO("AUTH_005", "La sesión ha expirado, por favor inicie sesión nuevamente", HttpStatus.UNAUTHORIZED),
    TOKEN_REVOCADO("AUTH_006", "El token ha sido invalidado (logout)", HttpStatus.UNAUTHORIZED),
    REFRESH_TOKEN_REQUERIDO("AUTH_007", "Se requiere un token de refresco válido", HttpStatus.BAD_REQUEST),

    // ── Verificación de Cuenta ───────────────────────────────────
    CUENTA_YA_VERIFICADA("AUTH_010", "Esta cuenta ya ha sido verificada previamente", HttpStatus.BAD_REQUEST),
    CODIGO_INVALIDO("AUTH_011", "El código de verificación ingresado es incorrecto", HttpStatus.BAD_REQUEST),
    CODIGO_EXPIRADO("AUTH_012", "El código ha expirado, por favor solicite uno nuevo", HttpStatus.GONE),
    CUENTA_NO_VERIFICADA("AUTH_016", "Su cuenta no ha sido verificada. Revise su correo electrónico.", HttpStatus.FORBIDDEN),

    // ── v1.5: Account Lockout ────────────────────────────────────
    CUENTA_BLOQUEADA("AUTH_013", "Cuenta bloqueada temporalmente por múltiples intentos fallidos. Intente en 15 minutos.", HttpStatus.LOCKED),

    // ── v1.5: Token Families ─────────────────────────────────────
    TOKEN_FAMILY_COMPROMETIDA("AUTH_014", "Se detectó uso sospechoso de sesión. Por seguridad, inicie sesión nuevamente.", HttpStatus.UNAUTHORIZED),

    // ── v1.5: Max Verification Attempts ──────────────────────────
    MAX_INTENTOS_VERIFICACION("AUTH_015", "Demasiados intentos de verificación. Solicite un nuevo código.", HttpStatus.TOO_MANY_REQUESTS),

    // ── v1.5: OAuth2 Social Login ────────────────────────────────
    OAUTH2_TOKEN_INVALIDO("AUTH_020", "El token del proveedor externo no es válido o ha expirado", HttpStatus.UNAUTHORIZED),
    OAUTH2_EMAIL_NO_VERIFICADO("AUTH_021", "El proveedor externo no proporcionó un email verificado", HttpStatus.BAD_REQUEST),

    // ── Autorización (Roles) ─────────────────────────────────────
    ACCESO_DENEGADO("AUTH_008", "No tiene permisos suficientes para realizar esta acción", HttpStatus.FORBIDDEN),
    CUENTA_DESACTIVADA("AUTH_009", "Su cuenta se encuentra desactivada", HttpStatus.FORBIDDEN),

    // ── Errores de Cliente y Formato ─────────────────────────────
    SOLICITUD_MAL_FORMADA("AUTH_400", "La solicitud contiene errores de formato o campos inválidos", HttpStatus.BAD_REQUEST),
    METODO_NO_PERMITIDO("AUTH_405", "El método HTTP no está permitido para este endpoint", HttpStatus.METHOD_NOT_ALLOWED),

    // ── Servidor ─────────────────────────────────────────────────
    ERROR_INTERNO("AUTH_500", "Ha ocurrido un error inesperado en el servidor", HttpStatus.INTERNAL_SERVER_ERROR),
    ACCION_NO_PERMITIDA("AUTH_501", "Esta acción no está permitida en este momento", HttpStatus.UNAUTHORIZED);

    private final String codigo;
    private final String mensajeDefault;
    private final HttpStatus httpStatus;

    ErrorCode(String codigo, String mensajeDefault, HttpStatus httpStatus) {
        this.codigo = codigo;
        this.mensajeDefault = mensajeDefault;
        this.httpStatus = httpStatus;
    }
}