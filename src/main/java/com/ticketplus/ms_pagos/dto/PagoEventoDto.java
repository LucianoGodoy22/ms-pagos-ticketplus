package com.ticketplus.ms_pagos.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class PagoEventoDto {
    private Long eventoId;
    private String emailUsuario;
    private Double monto;
    private LocalDateTime fecha;
}