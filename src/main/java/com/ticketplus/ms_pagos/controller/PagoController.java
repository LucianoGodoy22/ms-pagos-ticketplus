package com.ticketplus.ms_pagos.controller;

import com.ticketplus.ms_pagos.dto.PagoEventoDto;
import com.ticketplus.ms_pagos.dto.PagoRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/api/v1/pagos")
@Slf4j
public class PagoController {

    private final RabbitTemplate rabbitTemplate;
    private final KafkaTemplate<String, PagoEventoDto> kafkaTemplate;

    // Constructor manual para evitar el bug de Lombok en tu IDE
    public PagoController(RabbitTemplate rabbitTemplate, KafkaTemplate<String, PagoEventoDto> kafkaTemplate) {
        this.rabbitTemplate = rabbitTemplate;
        this.kafkaTemplate = kafkaTemplate;
    }

    @Value("${rabbitmq.exchange.pagos.fanout}")
    private String fanoutExchange;

    @Value("${kafka.topic.pagos}")
    private String kafkaPagosTopic;

    @PostMapping
    public ResponseEntity<String> procesarPago(@RequestBody PagoRequest pago) {
        log.info("Recibiendo solicitud de pago por monto: ${} para el evento: {}", pago.getMonto(), pago.getEventoId());

        boolean pagoExitoso = true;

        if (pagoExitoso) {
            // 1. Comando a RabbitMQ (generación de PDF / correo)
            rabbitTemplate.convertAndSend(fanoutExchange, "", pago);
            log.info("Comando de pago publicado exitosamente en RabbitMQ");

            // 2. Evento a Kafka (analítica y reportes en tiempo real)
            PagoEventoDto eventoKafka = new PagoEventoDto(
                pago.getEventoId(),
                pago.getEmailUsuario(), // Revisa si en tu PagoRequest se llama getEmail() o getCorreo()
                pago.getMonto(),
                LocalDateTime.now()
            );
            kafkaTemplate.send(kafkaPagosTopic, String.valueOf(pago.getEventoId()), eventoKafka);
            log.info("Evento de pago publicado exitosamente en Kafka (tópico: {})", kafkaPagosTopic);

            return ResponseEntity.ok("Pago en proceso. Recibirás un correo y tu ticket (PDF) en breve.");
        } else {
            return ResponseEntity.badRequest().body("El pago ha sido rechazado.");
        }
    }
}