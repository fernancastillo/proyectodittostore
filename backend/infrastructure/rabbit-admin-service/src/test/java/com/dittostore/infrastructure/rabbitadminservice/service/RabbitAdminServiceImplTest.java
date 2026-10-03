package com.dittostore.infrastructure.rabbitadminservice.service;

import com.dittostore.infrastructure.rabbitadminservice.dto.BindingRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.ExchangeRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.dto.QueueRequestDTO;
import com.dittostore.infrastructure.rabbitadminservice.exception.RabbitAdminException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.core.Binding;
import org.springframework.amqp.core.Exchange;
import org.springframework.amqp.core.Queue;
import org.springframework.amqp.core.TopicExchange;
import org.springframework.amqp.core.DirectExchange;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class RabbitAdminServiceImplTest {

    @Mock
    private AmqpAdmin amqpAdmin;

    @InjectMocks
    private RabbitAdminServiceImpl service;

    @Test
    void crearCola_declaraColaDurableConDlx() {
        service.crearCola(QueueRequestDTO.builder()
                .nombre("cola.prueba").deadLetterExchange("dlx.prueba").build());

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());
        Queue cola = captor.getValue();
        assertThat(cola.getName()).isEqualTo("cola.prueba");
        assertThat(cola.isDurable()).isTrue();
        assertThat(cola.isExclusive()).isFalse();
        assertThat(cola.getArguments()).containsEntry("x-dead-letter-exchange", "dlx.prueba");
    }

    @Test
    void crearCola_sinDlx_noAgregaArgumentoDeDeadLetter() {
        service.crearCola(QueueRequestDTO.builder().nombre("cola.simple").build());

        ArgumentCaptor<Queue> captor = ArgumentCaptor.forClass(Queue.class);
        verify(amqpAdmin).declareQueue(captor.capture());
        assertThat(captor.getValue().getArguments()).doesNotContainKey("x-dead-letter-exchange");
    }

    @Test
    void crearCola_siFallaElBroker_lanzaRabbitAdminException() {
        doThrow(new AmqpException("broker caído")).when(amqpAdmin).declareQueue(any(Queue.class));

        assertThatThrownBy(() -> service.crearCola(QueueRequestDTO.builder().nombre("cola.x").build()))
                .isInstanceOf(RabbitAdminException.class)
                .hasMessageContaining("cola.x");
    }

    @Test
    void eliminarCola_delegaEnAmqpAdmin() {
        service.eliminarCola("cola.vieja");

        verify(amqpAdmin).deleteQueue("cola.vieja");
    }

    @Test
    void crearExchange_topic_declaraTopicExchange() {
        service.crearExchange(ExchangeRequestDTO.builder().nombre("ex.topic").tipo("topic").build());

        ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
        verify(amqpAdmin).declareExchange(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(TopicExchange.class);
        assertThat(captor.getValue().getName()).isEqualTo("ex.topic");
        assertThat(captor.getValue().isDurable()).isTrue();
    }

    @Test
    void crearExchange_porDefectoEsDirect() {
        service.crearExchange(ExchangeRequestDTO.builder().nombre("ex.direct").build());

        ArgumentCaptor<Exchange> captor = ArgumentCaptor.forClass(Exchange.class);
        verify(amqpAdmin).declareExchange(captor.capture());
        assertThat(captor.getValue()).isInstanceOf(DirectExchange.class);
    }

    @Test
    void eliminarExchange_delegaEnAmqpAdmin() {
        service.eliminarExchange("ex.viejo");

        verify(amqpAdmin).deleteExchange("ex.viejo");
    }

    @Test
    void crearBinding_unaColaConUnExchangeYRoutingKey() {
        service.crearBinding(BindingRequestDTO.builder()
                .cola("cola.a").exchange("ex.a").routingKey("pago.estado.*").build());

        ArgumentCaptor<Binding> captor = ArgumentCaptor.forClass(Binding.class);
        verify(amqpAdmin).declareBinding(captor.capture());
        Binding binding = captor.getValue();
        assertThat(binding.getDestination()).isEqualTo("cola.a");
        assertThat(binding.getDestinationType()).isEqualTo(Binding.DestinationType.QUEUE);
        assertThat(binding.getExchange()).isEqualTo("ex.a");
        assertThat(binding.getRoutingKey()).isEqualTo("pago.estado.*");
    }

    @Test
    void eliminarBinding_delegaEnAmqpAdmin() {
        service.eliminarBinding(BindingRequestDTO.builder()
                .cola("cola.a").exchange("ex.a").routingKey("k").build());

        verify(amqpAdmin).removeBinding(any(Binding.class));
    }
}