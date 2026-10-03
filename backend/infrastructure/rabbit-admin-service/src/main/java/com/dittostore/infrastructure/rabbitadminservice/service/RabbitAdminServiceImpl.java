package com.dittostore.infrastructure.rabbitadminservice.service;

import com.dittostore.infrastructure.rabbitadminservice.dto.BindingRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.ExchangeRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.QueueRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.exception.RabbitAdminException;
import org.springframework.amqp.core.*;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class RabbitAdminServiceImpl implements RabbitAdminService {

    private final AmqpAdmin amqpAdmin;

    public RabbitAdminServiceImpl(AmqpAdmin amqpAdmin) {
        this.amqpAdmin = amqpAdmin;
    }

    @Override
    public void crearCola(QueueRequestDTO request) {
        try {
            Map<String, Object> args = new HashMap<>();
            if (request.getDeadLetterExchange() != null && !request.getDeadLetterExchange().isBlank()) {
                args.put("x-dead-letter-exchange", request.getDeadLetterExchange());
            }
            Queue queue = new Queue(request.getNombre(), request.isDurable(),
                    false, request.isAutoDelete(), args);
            amqpAdmin.declareQueue(queue);
        } catch (Exception ex) {
            throw new RabbitAdminException("No se pudo crear la cola " + request.getNombre(), ex);
        }
    }

    @Override
    public void eliminarCola(String nombre) {
        try {
            amqpAdmin.deleteQueue(nombre);
        } catch (Exception ex) {
            throw new RabbitAdminException("No se pudo eliminar la cola " + nombre, ex);
        }
    }

    @Override
    public void crearExchange(ExchangeRequestDTO request) {
        try {
            Exchange exchange = switch (request.getTipo()) {
                case "topic" -> new TopicExchange(request.getNombre(), request.isDurable(), request.isAutoDelete());
                case "fanout" -> new FanoutExchange(request.getNombre(), request.isDurable(), request.isAutoDelete());
                case "headers" -> new HeadersExchange(request.getNombre(), request.isDurable(), request.isAutoDelete());
                default -> new DirectExchange(request.getNombre(), request.isDurable(), request.isAutoDelete());
            };
            amqpAdmin.declareExchange(exchange);
        } catch (Exception ex) {
            throw new RabbitAdminException("No se pudo crear el exchange " + request.getNombre(), ex);
        }
    }

    @Override
    public void eliminarExchange(String nombre) {
        try {
            amqpAdmin.deleteExchange(nombre);
        } catch (Exception ex) {
            throw new RabbitAdminException("No se pudo eliminar el exchange " + nombre, ex);
        }
    }

    @Override
    public void crearBinding(BindingRequestDTO request) {
        try {
            Binding binding = new Binding(request.getCola(), Binding.DestinationType.QUEUE,
                    request.getExchange(), request.getRoutingKey(), null);
            amqpAdmin.declareBinding(binding);
        } catch (Exception ex) {
            throw new RabbitAdminException("No se pudo crear el binding entre " + request.getCola()
                    + " y " + request.getExchange(), ex);
        }
    }

    @Override
    public void eliminarBinding(BindingRequestDTO request) {
        try {
            Binding binding = new Binding(request.getCola(), Binding.DestinationType.QUEUE,
                    request.getExchange(), request.getRoutingKey(), null);
            amqpAdmin.removeBinding(binding);
        } catch (Exception ex) {
            throw new RabbitAdminException("No se pudo eliminar el binding entre " + request.getCola()
                    + " y " + request.getExchange(), ex);
        }
    }
}