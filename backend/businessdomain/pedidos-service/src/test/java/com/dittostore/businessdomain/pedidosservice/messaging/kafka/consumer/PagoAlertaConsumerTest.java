package com.dittostore.businessdomain.pedidosservice.messaging.kafka.consumer;

import com.dittostore.businessdomain.pedidosservice.messaging.kafka.event.AlertaPagoKafkaEvent;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.support.EventoJson;
import com.dittostore.businessdomain.pedidosservice.messaging.kafka.support.MensajeNoRecuperableException;
import com.dittostore.businessdomain.pedidosservice.service.AlertaService;
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
class PagoAlertaConsumerTest {

    private static final String ALERTA_VALIDA = """
            {"pagoId":3,"pedidoId":7,"monto":500000,"metodoPago":"TARJETA","estado":"PENDIENTE",
             "transaccionId":"TXN-1","motivo":"Monto inusualmente alto","severidad":"ALTA",
             "fechaEvento":"2026-11-01T10:00:00"}
            """;

    @Mock
    private AlertaService alertaService;

    private PagoAlertaConsumer consumer;

    @BeforeEach
    void setUp() {
        consumer = new PagoAlertaConsumer(new EventoJson(), alertaService);
    }

    private ConsumerRecord<String, String> registro(String valor) {
        return new ConsumerRecord<>("pagos.alertas", 1, 5L, "7", valor);
    }

    @Test
    void recibir_conAlertaValida_laRegistraEnElServicio() {
        consumer.recibir(registro(ALERTA_VALIDA));

        ArgumentCaptor<AlertaPagoKafkaEvent> captor = ArgumentCaptor.forClass(AlertaPagoKafkaEvent.class);
        verify(alertaService).registrarAlerta(captor.capture());
        assertThat(captor.getValue().pedidoId()).isEqualTo(7L);
        assertThat(captor.getValue().pagoId()).isEqualTo(3L);
        assertThat(captor.getValue().severidad()).isEqualTo("ALTA");
    }

    @Test
    void recibir_conJsonInvalido_lanzaMensajeNoRecuperable() {
        assertThatThrownBy(() -> consumer.recibir(registro("esto no es json")))
                .isInstanceOf(MensajeNoRecuperableException.class);
        verifyNoInteractions(alertaService);
    }

    @Test
    void recibir_sinCamposObligatorios_lanzaMensajeNoRecuperable() {
        assertThatThrownBy(() -> consumer.recibir(registro("{\"pagoId\":3}")))
                .isInstanceOf(MensajeNoRecuperableException.class);
        verifyNoInteractions(alertaService);
    }

    @Test
    void recibir_conMensajeVacio_lanzaMensajeNoRecuperable() {
        assertThatThrownBy(() -> consumer.recibir(registro(null)))
                .isInstanceOf(MensajeNoRecuperableException.class);
        verifyNoInteractions(alertaService);
    }
}