package com.rapidocourier.shared_kernel.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class PaqueteRegistradoEvent {
    private UUID paqueteId;
    private UUID remitenteId;
    private UUID destinatarioId;
}
