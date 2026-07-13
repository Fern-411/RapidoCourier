package com.rapidocourier.servicio_pagos.service;

import com.rapidocourier.servicio_pagos.dto.response.PagoResponse;
import com.rapidocourier.servicio_pagos.entity.Pago;
import com.rapidocourier.servicio_pagos.exception.ErrorCode;
import com.rapidocourier.servicio_pagos.repository.PagoRepository;
import com.rapidocourier.shared_kernel.exception.BaseException;
import lombok.AllArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class PagoService {

    private final PagoRepository pagoRepository;

    @Transactional
    public PagoResponse procesarPago(UUID paqueteId, BigDecimal monto) {
        if (monto == null || monto.compareTo(BigDecimal.ZERO) <= 0) {
            throw new BaseException(ErrorCode.MONTO_INVALIDO);
        }

        Pago pago = pagoRepository.findByPaqueteId(paqueteId)
                .orElse(new Pago());

        if ("COMPLETADO".equals(pago.getEstadoPago())) {
            throw new BaseException(ErrorCode.PAGO_YA_PROCESADO);
        }

        pago.setPaqueteId(paqueteId);
        pago.setMonto(monto);
        pago.setEstadoPago("COMPLETADO");
        pago.setFechaPago(LocalDateTime.now());
        Pago guardado = pagoRepository.save(pago);
        pagoRepository.flush();

        // Mapeamos la entidad al DTO antes de devolverla
        return new PagoResponse(
                guardado.getId(),
                guardado.getPaqueteId(),
                guardado.getMonto(),
                guardado.getEstadoPago(),
                guardado.getFechaPago()
        );
    }

    public boolean verificarPagoCompletado(UUID paqueteId) {
        return pagoRepository.findByPaqueteId(paqueteId)
                .map(p -> "COMPLETADO".equals(p.getEstadoPago()))
                .orElse(false);
    }

    public PagoResponse buscarPorPaqueteId(UUID paqueteId) {
        Pago pago = pagoRepository.findByPaqueteId(paqueteId)
                .orElseThrow(() -> new BaseException(ErrorCode.PAGO_NO_ENCONTRADO));
        return new PagoResponse(
                pago.getId(),
                pago.getPaqueteId(),
                pago.getMonto(),
                pago.getEstadoPago(),
                pago.getFechaPago()
        );
    }

    public com.rapidocourier.servicio_pagos.dto.response.EstadisticasPagoResponse obtenerEstadisticas() {
        LocalDateTime startOfDay = java.time.LocalDate.now().atStartOfDay();
        LocalDateTime endOfDay = startOfDay.plusDays(1);
        LocalDateTime startOfMonth = java.time.YearMonth.now().atDay(1).atStartOfDay();
        LocalDateTime endOfMonth = startOfMonth.plusMonths(1);

        BigDecimal ingresosHoy = pagoRepository.sumMontoByEstadoAndFechaBetween("COMPLETADO", startOfDay, endOfDay);
        BigDecimal ingresosMes = pagoRepository.sumMontoByEstadoAndFechaBetween("COMPLETADO", startOfMonth, endOfMonth);

        if (ingresosHoy == null) ingresosHoy = BigDecimal.ZERO;
        if (ingresosMes == null) ingresosMes = BigDecimal.ZERO;

        return new com.rapidocourier.servicio_pagos.dto.response.EstadisticasPagoResponse(ingresosHoy, ingresosMes);
    }
}