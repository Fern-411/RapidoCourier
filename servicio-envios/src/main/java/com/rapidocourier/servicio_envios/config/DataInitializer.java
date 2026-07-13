package com.rapidocourier.servicio_envios.config;

import com.rapidocourier.servicio_envios.entity.Agencia;
import com.rapidocourier.servicio_envios.repository.AgenciaRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import java.util.Arrays;

@Component
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final AgenciaRepository agenciaRepository;

    @Override
    public void run(String... args) {
        if (agenciaRepository.count() == 0) {
            log.info("Inicializando Agencias por defecto...");
            
            Agencia agencia1 = new Agencia();
            agencia1.setNombre("Agencia Central - Lima");
            agencia1.setDireccion("Av. Arequipa 1234, Lima");

            Agencia Agencia2 = new Agencia();
            Agencia2.setNombre("Sucursal Arequipa");
            Agencia2.setDireccion("Calle Mercaderes 567, Arequipa");

            Agencia Agencia3 = new Agencia();
            Agencia3.setNombre("Sucursal Trujillo");
            Agencia3.setDireccion("Jr. Pizarro 789, Trujillo");

            agenciaRepository.saveAll(Arrays.asList(agencia1, Agencia2, Agencia3));
            log.info("Agencias inicializadas exitosamente.");
        }
    }
}
