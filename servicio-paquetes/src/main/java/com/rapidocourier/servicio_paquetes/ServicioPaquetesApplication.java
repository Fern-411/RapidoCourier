package com.rapidocourier.servicio_paquetes;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients
@EnableAsync
@ComponentScan(basePackages = {
		"com.rapidocourier.servicio_paquetes",
		"com.rapidocourier.shared_kernel"
})
public class ServicioPaquetesApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioPaquetesApplication.class, args);
	}

}

