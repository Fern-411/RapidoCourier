package com.rapidocourier.servicio_auth;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableDiscoveryClient
@EnableScheduling
@EnableAsync
@ComponentScan(basePackages = {
		"com.rapidocourier.servicio_auth",
		"com.rapidocourier.shared_kernel"
})
public class ServicioAuthApplication {

	public static void main(String[] args) {
		SpringApplication.run(ServicioAuthApplication.class, args);
	}

}
