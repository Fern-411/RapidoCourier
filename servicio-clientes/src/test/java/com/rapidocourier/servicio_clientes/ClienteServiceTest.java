package com.rapidocourier.servicio_clientes;

import com.rapidocourier.servicio_clientes.client.ReniecClient;
import com.rapidocourier.servicio_clientes.dto.request.ClienteRequest;
import com.rapidocourier.servicio_clientes.dto.response.ClienteResponse;
import com.rapidocourier.servicio_clientes.dto.response.ReniecResponse;
import com.rapidocourier.servicio_clientes.entity.Cliente;
import com.rapidocourier.shared_kernel.exception.BaseException;
import com.rapidocourier.servicio_clientes.exception.ErrorCode;
import com.rapidocourier.servicio_clientes.repository.ClienteRepository;
import com.rapidocourier.servicio_clientes.service.ClienteService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClienteServiceTest {

    @Mock
    private ClienteRepository clienteRepository;

    @Mock
    private ReniecClient reniecClient;

    @Spy
    @InjectMocks
    private ClienteService clienteService;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(clienteService, "reniecToken", "dummy-token");
        ReflectionTestUtils.setField(clienteService, "self", clienteService);
    }

    @Test
    void registrarCliente_HappyPath() {
        // Arrange
        ClienteRequest request = new ClienteRequest("12345678", "test@test.com", "999888777");
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByDni(anyString())).thenReturn(false);

        // Ajuste: estructura plana sin DataResponse
        ReniecResponse rr = new ReniecResponse(true, "KATIA", "CARPIO", "ZEBALLOS", "Juan Perez", "12345678");
        when(reniecClient.getNombreCompleto(anyString(), anyString())).thenReturn(rr);

        Cliente mockSaved = new Cliente();
        ReflectionTestUtils.setField(mockSaved, "id", UUID.randomUUID());
        mockSaved.setDni("12345678");
        mockSaved.setEmail("test@test.com");
        mockSaved.setTelefono("999888777");
        mockSaved.setNombreCompleto("Juan Perez");

        when(clienteRepository.save(any(Cliente.class))).thenReturn(mockSaved);

        // Act
        ClienteResponse response = clienteService.registrarCliente(request);

        // Assert
        assertNotNull(response);
        assertEquals("Juan Perez", response.nombreCompleto());
        assertEquals("12345678", response.dni());
        verify(clienteRepository, times(1)).save(any(Cliente.class));
    }

    @Test
    void registrarCliente_EmailDuplicado_ExceptionPath() {
        ClienteRequest request = new ClienteRequest("12345678", "test@test.com", "999888777");
        when(clienteRepository.existsByEmail(anyString())).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class, () -> clienteService.registrarCliente(request));
        assertEquals(ErrorCode.EMAIL_YA_REGISTRADO.getCodigo(), ex.getCodigoError().getCodigo());
    }

    @Test
    void registrarCliente_DniDuplicado_ExceptionPath() {
        ClienteRequest request = new ClienteRequest("12345678", "test@test.com", "999888777");
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByDni(anyString())).thenReturn(true);

        BaseException ex = assertThrows(BaseException.class, () -> clienteService.registrarCliente(request));
        assertEquals(ErrorCode.DNI_YA_REGISTRADO.getCodigo(), ex.getCodigoError().getCodigo());
    }

    @Test
    void registrarCliente_ReniecVacio_EmptyResultPath() {
        ClienteRequest request = new ClienteRequest("12345678", "test@test.com", "999888777");
        when(clienteRepository.existsByEmail(anyString())).thenReturn(false);
        when(clienteRepository.existsByDni(anyString())).thenReturn(false);

        // Ajuste: estructura plana con full_name vacío
        ReniecResponse rr = new ReniecResponse(true, "", "", "", "", "");
        when(reniecClient.getNombreCompleto(anyString(), anyString())).thenReturn(rr);

        BaseException ex = assertThrows(BaseException.class, () -> clienteService.registrarCliente(request));
        assertEquals(ErrorCode.RENIEC_RESPUESTA_VACIA.getCodigo(), ex.getCodigoError().getCodigo());
    }
}