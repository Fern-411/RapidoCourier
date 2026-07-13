package com.rapidocourier.servicio_pagos.service;

import com.rapidocourier.servicio_pagos.dto.response.PagoResponse;
import com.rapidocourier.servicio_pagos.entity.Pago;
import com.rapidocourier.servicio_pagos.exception.ErrorCode;
import com.rapidocourier.shared_kernel.exception.BaseException;
import com.rapidocourier.servicio_pagos.repository.PagoRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PagoServiceTest {

    @Mock
    private PagoRepository pagoRepository;

    @InjectMocks
    private PagoService pagoService;

    @Test
    void procesarPago_HappyPath() {
        // Arrange
        UUID paqueteId = UUID.randomUUID();
        UUID pagoId = UUID.randomUUID();
        BigDecimal monto = new BigDecimal("100.00");

        when(pagoRepository.findByPaqueteId(paqueteId)).thenReturn(Optional.empty());

        // Simulamos la entidad que nos devuelve la BD al guardar
        Pago guardado = new Pago();
        guardado.setId(pagoId);
        guardado.setPaqueteId(paqueteId);
        guardado.setMonto(monto);
        guardado.setEstadoPago("COMPLETADO");
        guardado.setFechaPago(LocalDateTime.now());

        when(pagoRepository.save(any(Pago.class))).thenReturn(guardado);

        // Act
        PagoResponse result = pagoService.procesarPago(paqueteId, monto);

        // Assert (Ahora validamos contra el Record/DTO)
        assertNotNull(result);
        assertEquals("COMPLETADO", result.estadoPago());
        assertEquals(pagoId, result.id());
        assertEquals(monto, result.monto());
        verify(pagoRepository, times(1)).save(any(Pago.class));
    }

    @Test
    void procesarPago_YaCompletado_ExceptionPath() {
        // Arrange
        UUID paqueteId = UUID.randomUUID();
        Pago existente = new Pago();
        existente.setEstadoPago("COMPLETADO");
        when(pagoRepository.findByPaqueteId(paqueteId)).thenReturn(Optional.of(existente));

        // Act & Assert
        BaseException ex = assertThrows(BaseException.class,
                () -> pagoService.procesarPago(paqueteId, new BigDecimal("100.00")));

        // Validamos que se lance exactamente el código de error esperado
        assertEquals(ErrorCode.PAGO_YA_PROCESADO, ex.getCodigoError());
        verify(pagoRepository, never()).save(any(Pago.class));
    }

    @Test
    void procesarPago_MontoInvalido_ExceptionPath() {
        // Arrange
        UUID paqueteId = UUID.randomUUID();

        // Act & Assert (Probamos con monto 0 o negativo)
        BaseException ex = assertThrows(BaseException.class,
                () -> pagoService.procesarPago(paqueteId, new BigDecimal("-10.00")));

        // Validamos que salte la validación inicial sin tocar la BD
        assertEquals(ErrorCode.MONTO_INVALIDO, ex.getCodigoError());
        verify(pagoRepository, never()).findByPaqueteId(any());
        verify(pagoRepository, never()).save(any(Pago.class));
    }
}