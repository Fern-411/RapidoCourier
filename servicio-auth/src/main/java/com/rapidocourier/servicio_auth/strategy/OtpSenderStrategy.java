package com.rapidocourier.servicio_auth.strategy;

import com.rapidocourier.servicio_auth.entity.Usuario;

public interface OtpSenderStrategy {
    void sendOtp(Usuario usuario, String codigo);
    String getChannel();
}
