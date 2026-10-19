package com.ticketplus.ms_pagos.controller;

import com.ticketplus.ms_pagos.dto.PagoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pagos")
@Slf4j
public class PagoController {

    private final RabbitTemplate rabbitTemplate;

    // Constructor manual para evitar el bug de Lombok en tu IDE
    public PagoController(RabbitTemplate rabbitTemplate) {
        this.rabbitTemplate = rabbitTemplate;
    }

    @Value("${rabbitmq.exchange.pagos.fanout}")
    private String fanoutExchange;

    @PostMapping
    public ResponseEntity<String> procesarPago(@RequestBody PagoRequest pago) {
        log.info("Recibiendo solicitud de pago por monto: ${} para el evento: {}", pago.getMonto(), pago.getEventoId());

        boolean pagoExitoso = true; 

        if (pagoExitoso) {
            rabbitTemplate.convertAndSend(fanoutExchange, "", pago);
            log.info("Evento de pago publicado exitosamente en RabbitMQ");
            return ResponseEntity.ok("Pago en proceso. Recibirás un correo y tu ticket (PDF) en breve.");
        } else {
            return ResponseEntity.badRequest().body("El pago ha sido rechazado.");
        }
    }
}