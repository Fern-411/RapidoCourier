package com.rapidocourier.servicio_auth.config;

import com.rapidocourier.servicio_auth.entity.Rol;
import com.rapidocourier.servicio_auth.entity.Usuario;
import com.rapidocourier.servicio_auth.repository.RolRepository;
import com.rapidocourier.servicio_auth.repository.UsuarioRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {

    private final RolRepository rolRepository;
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    public DataInitializer(RolRepository rolRepository, UsuarioRepository usuarioRepository, PasswordEncoder passwordEncoder) {
        this.rolRepository = rolRepository;
        this.usuarioRepository = usuarioRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) throws Exception {
        // Inicializar Roles
        Rol rolAdmin =  crearRolSiNoExiste("ADMIN");
                        crearRolSiNoExiste("EMPLEADO");
                        crearRolSiNoExiste("CLIENTE");
                        crearRolSiNoExiste("REPARTIDOR");
                        crearRolSiNoExiste("SUPERVISOR");

        // Inicializar Usuario Administrador Maestro
        String adminEmail = "admin@rapidocourier.com";
        Usuario admin = usuarioRepository.findByEmail(adminEmail).orElseGet(Usuario::new);
        admin.setNombre("Administrador Maestro");
        if (admin.getId() == null) {
            admin.setEmail(adminEmail);
            admin.setPassword(passwordEncoder.encode("admin123"));
        }
        admin.setRol(rolAdmin);
        admin.setNumeroContacto("999999999");
        admin.setIsEmailVerified(true);
        admin.setIsPhoneVerified(true);
        usuarioRepository.save(admin);
        System.out.println("Usuario Administrador Maestro inicializado exitosamente.");

        // Inicializar Operador
        Rol rolOperador = rolRepository.findByNombre("EMPLEADO").orElseGet(() -> rolRepository.save(new Rol("EMPLEADO")));
        String operadorEmail = "operador@rapidocourier.com";
        Usuario operador = usuarioRepository.findByEmail(operadorEmail).orElseGet(Usuario::new);
        operador.setNombre("Operador Logistico");
        if (operador.getId() == null) {
            operador.setEmail(operadorEmail);
            operador.setPassword(passwordEncoder.encode("operador123"));
        }
        operador.setRol(rolOperador);
        operador.setNumeroContacto("988888888");
        operador.setIsEmailVerified(true);
        operador.setIsPhoneVerified(true);
        usuarioRepository.save(operador);
        System.out.println("Usuario Operador inicializado exitosamente.");

        // Inicializar Cliente
        Rol rolCliente = rolRepository.findByNombre("CLIENTE").orElseGet(() -> rolRepository.save(new Rol("CLIENTE")));
        String clienteEmail = "cliente@rapidocourier.com";
        Usuario cliente = usuarioRepository.findByEmail(clienteEmail).orElseGet(Usuario::new);
        cliente.setNombre("Cliente Frecuente");
        if (cliente.getId() == null) {
            cliente.setEmail(clienteEmail);
            cliente.setPassword(passwordEncoder.encode("cliente123"));
        }
        cliente.setRol(rolCliente);
        cliente.setNumeroContacto("977777777");
        cliente.setIsEmailVerified(true);
        cliente.setIsPhoneVerified(true);
        usuarioRepository.save(cliente);
        System.out.println("Usuario Cliente inicializado exitosamente.");
    }

    private Rol crearRolSiNoExiste(String nombre) {
        return rolRepository.findByNombre(nombre)
                .orElseGet(() -> rolRepository.save(new Rol(nombre)));
    }
}
