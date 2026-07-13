package com.rapidocourier.servicio_auth.strategy;

import com.rapidocourier.shared_kernel.exception.BaseException;
import com.rapidocourier.servicio_auth.exception.ErrorCode;
import org.springframework.stereotype.Component;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Component
public class OtpSenderFactory {
    
    private final Map<String, OtpSenderStrategy> strategies;

    public OtpSenderFactory(List<OtpSenderStrategy> strategyList) {
        this.strategies = strategyList.stream()
                .collect(Collectors.toMap(s -> s.getChannel().toUpperCase(), s -> s));
    }

    public OtpSenderStrategy getStrategy(String channel) {
        if (channel == null) channel = "EMAIL";
        OtpSenderStrategy strategy = strategies.get(channel.toUpperCase());
        if (strategy == null) {
            throw new BaseException(ErrorCode.SOLICITUD_MAL_FORMADA); // Canal no soportado
        }
        return strategy;
    }
}
