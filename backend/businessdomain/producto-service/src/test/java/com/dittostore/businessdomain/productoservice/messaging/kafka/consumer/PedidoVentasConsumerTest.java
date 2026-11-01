package com.dittostore.businessdomain.productoservice.messaging.kafka.consumer;

import com.dittostore.businessdomain.productoservice.messaging.kafka.event.PedidoEventoKafka;
import com.dittostore.businessdomain.productoservice.messaging.kafka.support.EventoJson;
import com.dittostore.businessdomain.productoservice.messaging.kafka.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.productoservice.service.VentaService;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

@ExtendWith(MockitoExtension.class)
class PedidoVentasConsumerTest {

    private static final String CONFIRMADO = """
            {"pedidoId":7,"usuarioId":1,"estado":"CONFIRMADO","total":30000,
             "items":[{"productoId":1,"cantidad":2,"subtotal":30000}],
             "fechaEvento":"2026-11-01T10:00:00"}
            """;

    @Mock
    private VentaService ventaService;

    private PedidoVentasConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PedidoVentasConsumer(new EventoJson(), ventaService);
    }

    private ConsumerRecord<String, String> registro(String valor) {
        return new ConsumerRecord<>("pedidos.eventos", 2, 10L, "7", valor);
    }

    @Test
    void recibir_pedidoConfirmado_contabilizaLaVenta() {
        consumer.recibir(registro(CONFIRMADO));

        ArgumentCaptor<PedidoEventoKafka> captor = ArgumentCaptor.forClass(PedidoEventoKafka.class);
        verify(ventaService).registrarVentaPedido(captor.capture());
        assertThat(captor.getValue().pedidoId()).isEqualTo(7L);
        assertThat(captor.getValue().items()).hasSize(1);
    }

    @Test
    void recibir_pedidoPendiente_seIgnora() {
        consumer.recibir(registro(CONFIRMADO.replace("CONFIRMADO", "PENDIENTE")));

        verifyNoInteractions(ventaService);
    }

    @Test
    void recibir_pedidoConfirmadoSinItems_noContabilizaNiFalla() {
        String sinItems = "{\"pedidoId\":7,\"usuarioId\":1,\"estado\":\"CONFIRMADO\",\"items\":[]}";

        consumer.recibir(registro(sinItems));

        verifyNoInteractions(ventaService);
    }

    @Test
    void recibir_conItemDeCantidadCero_lanzaMensajeNoRecuperable() {
        String invalido = CONFIRMADO.replace("\"cantidad\":2", "\"cantidad\":0");

        assertThatThrownBy(() -> consumer.recibir(registro(invalido)))
                .isInstanceOf(MensajeNoRecuperableException.class);
        verifyNoInteractions(ventaService);
    }

    @Test
    void recibir_conJsonInvalido_lanzaMensajeNoRecuperable() {
        assertThatThrownBy(() -> consumer.recibir(registro("{no es json")))
                .isInstanceOf(MensajeNoRecuperableException.class);
        verifyNoInteractions(ventaService);
    }

    @Test
    void recibir_sinPedidoId_lanzaMensajeNoRecuperable() {
        assertThatThrownBy(() -> consumer.recibir(registro("{\"estado\":\"CONFIRMADO\"}")))
                .isInstanceOf(MensajeNoRecuperableException.class);
        verifyNoInteractions(ventaService);
    }
}