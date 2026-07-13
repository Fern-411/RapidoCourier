package com.rapidocourier.servicio_clientes.dto.response;

// ReniecResponse ya no necesita DataResponse porque la API de DeColecta
// devuelve los datos en la raíz, no dentro de un objeto "data"
public record ReniecResponse(
        boolean success, // Si la API sigue enviando este campo
        String first_name,
        String first_last_name,
        String second_last_name,
        String full_name,
        String document_number
) { }