package com.dittostore.businessdomain.reviewsservice.messaging.consumer;

import com.dittostore.businessdomain.reviewsservice.exception.PedidoNoRegistradoException;
import com.dittostore.businessdomain.reviewsservice.messaging.consumer.pago.PagoAprobadoConsumer;
import com.dittostore.businessdomain.reviewsservice.messaging.consumer.pedido.PedidoCreadoConsumer;
import com.dittostore.businessdomain.reviewsservice.messaging.event.PagoAprobadoEvent;
import com.dittostore.businessdomain.reviewsservice.messaging.event.PedidoCreadoEvent;
import com.dittostore.businessdomain.reviewsservice.messaging.support.MensajeAckHandler;
import com.dittostore.businessdomain.reviewsservice.service.CompraVerificadaService;
import com.rabbitmq.client.Channel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumersTest {

    private static final long TAG = 7L;

    @Mock
    private CompraVerificadaService compraVerificadaService;

    @Mock
    private Channel channel;

    private PedidoCreadoConsumer pedidoConsumer;
    private PagoAprobadoConsumer pagoConsumer;

    @BeforeEach
    void setUp() {
        MensajeAckHandler ackHandler = new MensajeAckHandler();
        pedidoConsumer = new PedidoCreadoConsumer(compraVerificadaService, ackHandler);
        pagoConsumer = new PagoAprobadoConsumer(compraVerificadaService, ackHandler);
    }

    @Test
    void pedidoValido_registraItemsYHaceAck() throws IOException {
        PedidoCreadoEvent evento = new PedidoCreadoEvent(101L, 1L, List.of(
            new PedidoCreadoEvent.Item(10L, 2),
            new PedidoCreadoEvent.Item(11L, 1)));

        pedidoConsumer.consumir(evento, channel, TAG, false);

        verify(compraVerificadaService).registrarCompra(101L, 1L, 10L, 2);
        verify(compraVerificadaService).registrarCompra(101L, 1L, 11L, 1);
        verify(channel).basicAck(TAG, false);
        verify(channel, never()).basicNack(anyLong(), anyBoolean(), anyBoolean());
    }

    @Test
    void pedidoSinItems_esNoRecuperableYVaADlq() throws IOException {
        PedidoCreadoEvent evento = new PedidoCreadoEvent(102L, 1L, List.of());

        pedidoConsumer.consumir(evento, channel, TAG, false);

        verify(channel).basicNack(TAG, false, false);
        verify(channel, never()).basicAck(anyLong(), anyBoolean());
        verifyNoInteractions(compraVerificadaService);
    }

    @Test
    void pagoAprobado_marcaPagadoYHaceAck() throws IOException {
        PagoAprobadoEvent evento = new PagoAprobadoEvent(501L, 101L, new BigDecimal("45990"), "APROBADO", "TXN-ABC12345");

        pagoConsumer.consumir(evento, channel, TAG, false);

        verify(compraVerificadaService).marcarPagado(101L);
        verify(channel).basicAck(TAG, false);
    }

    @Test
    void pagoNoAprobado_esNoRecuperableYVaADlq() throws IOException {
        PagoAprobadoEvent evento = new PagoAprobadoEvent(502L, 101L, new BigDecimal("45990"), "RECHAZADO", "TXN-XYZ98765");

        pagoConsumer.consumir(evento, channel, TAG, false);

        verify(channel).basicNack(TAG, false, false);
        verifyNoInteractions(compraVerificadaService);
    }

    @Test
    void errorRecuperable_primerIntentoHaceRequeue() throws IOException {
        PagoAprobadoEvent evento = new PagoAprobadoEvent(503L, 999L, new BigDecimal("1000"), "APROBADO", "TXN-1");
        doThrow(new PedidoNoRegistradoException(999L)).when(compraVerificadaService).marcarPagado(999L);

        pagoConsumer.consumir(evento, channel, TAG, false);

        verify(channel).basicNack(TAG, false, true);
    }

    @Test
    void errorRecuperable_reintentoAgotadoVaADlq() throws IOException {
        PagoAprobadoEvent evento = new PagoAprobadoEvent(503L, 999L, new BigDecimal("1000"), "APROBADO", "TXN-1");
        doThrow(new PedidoNoRegistradoException(999L)).when(compraVerificadaService).marcarPagado(999L);

        pagoConsumer.consumir(evento, channel, TAG, true);

        verify(channel).basicNack(TAG, false, false);
    }
}
