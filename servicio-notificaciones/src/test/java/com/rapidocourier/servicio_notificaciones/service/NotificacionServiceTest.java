package com.rapidocourier.servicio_notificaciones.service;

import com.rapidocourier.servicio_notificaciones.dto.request.NotificacionRequest;
import com.rapidocourier.servicio_notificaciones.dto.response.NotificacionResponse;
import com.rapidocourier.servicio_notificaciones.entity.Notificacion;
import com.rapidocourier.servicio_notificaciones.exception.ErrorCode;
import com.rapidocourier.servicio_notificaciones.repository.NotificacionRepository;
import com.rapidocourier.shared_kernel.exception.BaseException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NotificacionServiceTest {

    @Mock
    private NotificacionRepository notificacionRepository;

    @InjectMocks
    private NotificacionService notificacionService;

    @Test
    void enviarNotificacion_HappyPath() {
        // Arrange
        UUID paqueteId = UUID.randomUUID();
        String mensaje = "Paquete en transito";
        NotificacionRequest request = new NotificacionRequest(paqueteId, mensaje);
        UUID notificacionId = UUID.randomUUID();

        Notificacion guardada = new Notificacion(notificacionId, paqueteId, mensaje, "ENVIADA", LocalDateTime.now());

        when(notificacionRepository.save(any(Notificacion.class))).thenReturn(guardada);

        // Act - Ahora pasamos el request (1 argumento)
        NotificacionResponse result = notificacionService.enviarNotificacion(request);

        // Assert
        assertNotNull(result);
        assertEquals(notificacionId, result.id());
        assertEquals(paqueteId, result.paqueteId());
        assertEquals(mensaje, result.mensaje());
        assertEquals("ENVIADA", result.estado());
        assertNotNull(result.fechaEnvio());

        verify(notificacionRepository, times(1)).save(any(Notificacion.class));
    }

    @Test
    void enviarNotificacion_PaqueteIdNulo_ThrowsBaseException() {
        // Arrange
        NotificacionRequest request = new NotificacionRequest(null, "Mensaje válido");

        // Act & Assert
        BaseException ex = assertThrows(BaseException.class,
                () -> notificacionService.enviarNotificacion(request));

        assertEquals(ErrorCode.PAQUETE_ID_NULO, ex.getCodigoError());
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void enviarNotificacion_MensajeVacio_ThrowsBaseException() {
        // Arrange: Crear request con mensaje vacío
        NotificacionRequest request = new NotificacionRequest(UUID.randomUUID(), "   ");

        // Act & Assert
        BaseException ex = assertThrows(BaseException.class,
                () -> notificacionService.enviarNotificacion(request));

        assertEquals(ErrorCode.MENSAJE_VACIO, ex.getCodigoError());
        verify(notificacionRepository, never()).save(any());
    }

    @Test
    void enviarNotificacion_DBError_ExceptionPath() {
        // Arrange
        NotificacionRequest request = new NotificacionRequest(UUID.randomUUID(), "Mensaje válido");
        when(notificacionRepository.save(any(Notificacion.class))).thenThrow(new RuntimeException("Error en DB"));

        // Act & Assert
        RuntimeException ex = assertThrows(RuntimeException.class,
                () -> notificacionService.enviarNotificacion(request));

        assertEquals("Error en DB", ex.getMessage());
    }
}