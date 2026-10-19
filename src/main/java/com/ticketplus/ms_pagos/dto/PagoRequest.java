package com.ticketplus.ms_pagos.dto;

import lombok.Data;

@Data
public class PagoRequest {
    private Long eventoId;
    private String emailUsuario;
    private Double monto;
}