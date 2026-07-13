package com.rapidocourier.servicio_paquetes.config;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.cloud.context.config.annotation.RefreshScope;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Component
@RefreshScope
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "paquetes.tarifa")
public class TarifaProperties {

    private BigDecimal base = new BigDecimal("10.00");
    private BigDecimal costoPeso = new BigDecimal("5.00");

    public BigDecimal getBase() {
        return base;
    }

    public void setBase(BigDecimal base) {
        this.base = base;
    }

    public BigDecimal getCostoPeso() {
        return costoPeso;
    }

    public void setCostoPeso(BigDecimal costoPeso) {
        this.costoPeso = costoPeso;
    }
}
