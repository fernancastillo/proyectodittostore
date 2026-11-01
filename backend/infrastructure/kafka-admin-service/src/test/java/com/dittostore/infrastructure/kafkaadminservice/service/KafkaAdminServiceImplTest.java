package com.dittostore.infrastructure.kafkaadminservice.service;

import com.dittostore.infrastructure.kafkaadminservice.dto.GrupoDetalleDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicDetalleDTO;
import com.dittostore.infrastructure.kafkaadminservice.dto.TopicRequestDTO;
import com.dittostore.infrastructure.kafkaadminservice.exception.RecursoExistenteException;
import com.dittostore.infrastructure.kafkaadminservice.exception.RecursoNoEncontradoException;
import com.dittostore.infrastructure.kafkaadminservice.exception.SolicitudInvalidaException;
import org.apache.kafka.clients.admin.Admin;
import org.apache.kafka.clients.admin.AlterConfigOp;
import org.apache.kafka.clients.admin.AlterConfigsResult;
import org.apache.kafka.clients.admin.Config;
import org.apache.kafka.clients.admin.ConfigEntry;
import org.apache.kafka.clients.admin.ConsumerGroupDescription;
import org.apache.kafka.clients.admin.ConsumerGroupListing;
import org.apache.kafka.clients.admin.CreatePartitionsResult;
import org.apache.kafka.clients.admin.CreateTopicsResult;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.DescribeConfigsResult;
import org.apache.kafka.clients.admin.DescribeConsumerGroupsResult;
import org.apache.kafka.clients.admin.DescribeTopicsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupOffsetsResult;
import org.apache.kafka.clients.admin.ListConsumerGroupsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult;
import org.apache.kafka.clients.admin.ListOffsetsResult.ListOffsetsResultInfo;
import org.apache.kafka.clients.admin.ListTopicsOptions;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.apache.kafka.clients.admin.MemberAssignment;
import org.apache.kafka.clients.admin.MemberDescription;
import org.apache.kafka.clients.admin.NewPartitions;
import org.apache.kafka.clients.admin.NewTopic;
import org.apache.kafka.clients.admin.TopicDescription;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.KafkaFuture;
import org.apache.kafka.common.Node;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.TopicPartitionInfo;
import org.apache.kafka.common.config.ConfigResource;
import org.apache.kafka.common.errors.InvalidPartitionsException;
import org.apache.kafka.common.errors.TopicExistsException;
import org.apache.kafka.common.errors.UnknownTopicOrPartitionException;
import org.apache.kafka.common.internals.KafkaFutureImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyCollection;
import static org.mockito.ArgumentMatchers.anyMap;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class KafkaAdminServiceImplTest {

    @Mock
    private Admin admin;

    @Captor
    private ArgumentCaptor<Collection<NewTopic>> topicsCaptor;

    @Captor
    private ArgumentCaptor<Map<ConfigResource, Collection<AlterConfigOp>>> configCaptor;

    @Captor
    private ArgumentCaptor<Map<String, NewPartitions>> particionesCaptor;

    @Captor
    private ArgumentCaptor<Collection<String>> nombresCaptor;

    private KafkaAdminServiceImpl service;

    @BeforeEach
    void setUp() {
        service = new KafkaAdminServiceImpl(admin, 1000);
    }

    private static <T> KafkaFuture<T> fallo(Throwable error) {
        KafkaFutureImpl<T> futuro = new KafkaFutureImpl<>();
        futuro.completeExceptionally(error);
        return futuro;
    }


    @Test
    void crearTopic_enviaNombreParticionesReplicasYConfiguraciones() {
        CreateTopicsResult resultado = mock(CreateTopicsResult.class);
        when(admin.createTopics(anyCollection())).thenReturn(resultado);
        when(resultado.all()).thenReturn(KafkaFuture.completedFuture(null));

        service.crearTopic(new TopicRequestDTO("pedidos.eventos", 3, 3, Map.of("retention.ms", "604800000")));

        verify(admin).createTopics(topicsCaptor.capture());
        NewTopic nuevo = topicsCaptor.getValue().iterator().next();
        assertThat(nuevo.name()).isEqualTo("pedidos.eventos");
        assertThat(nuevo.numPartitions()).isEqualTo(3);
        assertThat(nuevo.replicationFactor()).isEqualTo((short) 3);
        assertThat(nuevo.configs()).containsEntry("retention.ms", "604800000");
    }

    @Test
    void crearTopic_conConfiguracionNoPermitida_lanzaSolicitudInvalidaSinLlamarAKafka() {
        TopicRequestDTO request = new TopicRequestDTO("prueba", 1, 3, Map.of("foo", "1"));

        assertThatThrownBy(() -> service.crearTopic(request))
                .isInstanceOf(SolicitudInvalidaException.class)
                .hasMessageContaining("foo");
        verifyNoInteractions(admin);
    }

    @Test
    void crearTopic_siElTopicYaExiste_lanzaRecursoExistente() {
        CreateTopicsResult resultado = mock(CreateTopicsResult.class);
        when(admin.createTopics(anyCollection())).thenReturn(resultado);
        when(resultado.all()).thenReturn(fallo(new TopicExistsException("ya existe")));

        assertThatThrownBy(() -> service.crearTopic(new TopicRequestDTO("pedidos.eventos", 3, 3, null)))
                .isInstanceOf(RecursoExistenteException.class);
    }

    @Test
    void listarTopics_devuelveLosNombresOrdenados() {
        ListTopicsResult resultado = mock(ListTopicsResult.class);
        when(admin.listTopics(any(ListTopicsOptions.class))).thenReturn(resultado);
        when(resultado.names()).thenReturn(KafkaFuture.completedFuture(Set.of("pedidos.eventos", "pagos.alertas")));

        assertThat(service.listarTopics()).containsExactly("pagos.alertas", "pedidos.eventos");
    }

    @Test
    void describirTopic_devuelveParticionesLiderIsrYSoloLasConfiguracionesAdministrables() {
        Node n1 = new Node(1, "kafka-1", 9092);
        Node n2 = new Node(2, "kafka-2", 9093);
        Node n3 = new Node(3, "kafka-3", 9094);
        TopicPartitionInfo p0 = new TopicPartitionInfo(0, n1, List.of(n1, n2, n3), List.of(n1, n2, n3));
        TopicDescription descripcion = new TopicDescription("pedidos.eventos", false, List.of(p0));

        DescribeTopicsResult resTopics = mock(DescribeTopicsResult.class);
        when(admin.describeTopics(anyCollection())).thenReturn(resTopics);
        when(resTopics.allTopicNames())
                .thenReturn(KafkaFuture.completedFuture(Map.of("pedidos.eventos", descripcion)));

        ConfigResource recurso = new ConfigResource(ConfigResource.Type.TOPIC, "pedidos.eventos");
        Config config = new Config(List.of(
                new ConfigEntry("retention.ms", "604800000"),
                new ConfigEntry("cleanup.policy", "delete"),
                new ConfigEntry("otra.config.interna", "x")));
        DescribeConfigsResult resConfig = mock(DescribeConfigsResult.class);
        when(admin.describeConfigs(anyCollection())).thenReturn(resConfig);
        when(resConfig.all()).thenReturn(KafkaFuture.completedFuture(Map.of(recurso, config)));

        TopicDetalleDTO detalle = service.describirTopic("pedidos.eventos");

        assertThat(detalle.particiones()).isEqualTo(1);
        assertThat(detalle.factorReplicacion()).isEqualTo(3);
        assertThat(detalle.detalleParticiones().get(0).lider()).isEqualTo(1);
        assertThat(detalle.detalleParticiones().get(0).isr()).containsExactly(1, 2, 3);
        assertThat(detalle.configuraciones())
                .containsEntry("retention.ms", "604800000")
                .containsEntry("cleanup.policy", "delete")
                .doesNotContainKey("otra.config.interna");
    }

    @Test
    void describirTopic_siNoExiste_lanzaRecursoNoEncontrado() {
        DescribeTopicsResult resultado = mock(DescribeTopicsResult.class);
        when(admin.describeTopics(anyCollection())).thenReturn(resultado);
        when(resultado.allTopicNames()).thenReturn(fallo(new UnknownTopicOrPartitionException("no existe")));

        assertThatThrownBy(() -> service.describirTopic("noexiste"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }

    @Test
    void modificarConfiguracion_usaOperacionSetSobreElTopic() {
        AlterConfigsResult resultado = mock(AlterConfigsResult.class);
        when(admin.incrementalAlterConfigs(anyMap())).thenReturn(resultado);
        when(resultado.all()).thenReturn(KafkaFuture.completedFuture(null));

        service.modificarConfiguracion("pagos.alertas", Map.of("retention.ms", "3600000"));

        verify(admin).incrementalAlterConfigs(configCaptor.capture());
        Map.Entry<ConfigResource, Collection<AlterConfigOp>> cambio =
                configCaptor.getValue().entrySet().iterator().next();
        assertThat(cambio.getKey().name()).isEqualTo("pagos.alertas");
        AlterConfigOp operacion = cambio.getValue().iterator().next();
        assertThat(operacion.configEntry().name()).isEqualTo("retention.ms");
        assertThat(operacion.configEntry().value()).isEqualTo("3600000");
        assertThat(operacion.opType()).isEqualTo(AlterConfigOp.OpType.SET);
    }

    @Test
    void aumentarParticiones_pideElNuevoTotalDeParticiones() {
        CreatePartitionsResult resultado = mock(CreatePartitionsResult.class);
        when(admin.createPartitions(anyMap())).thenReturn(resultado);
        when(resultado.all()).thenReturn(KafkaFuture.completedFuture(null));

        service.aumentarParticiones("prueba.tmp", 2);

        verify(admin).createPartitions(particionesCaptor.capture());
        assertThat(particionesCaptor.getValue().get("prueba.tmp").totalCount()).isEqualTo(2);
    }

    @Test
    void aumentarParticiones_siSeIntentaReducir_lanzaSolicitudInvalida() {
        CreatePartitionsResult resultado = mock(CreatePartitionsResult.class);
        when(admin.createPartitions(anyMap())).thenReturn(resultado);
        when(resultado.all()).thenReturn(fallo(new InvalidPartitionsException("no se puede reducir")));

        assertThatThrownBy(() -> service.aumentarParticiones("prueba.tmp", 1))
                .isInstanceOf(SolicitudInvalidaException.class);
    }

    @Test
    void eliminarTopic_pideLaEliminacionDelTopicIndicado() {
        DeleteTopicsResult resultado = mock(DeleteTopicsResult.class);
        when(admin.deleteTopics(anyCollection())).thenReturn(resultado);
        when(resultado.all()).thenReturn(KafkaFuture.completedFuture(null));

        service.eliminarTopic("prueba.tmp");

        verify(admin).deleteTopics(nombresCaptor.capture());
        assertThat(nombresCaptor.getValue()).containsExactly("prueba.tmp");
    }


    @Test
    void listarGrupos_devuelveLosIdsOrdenados() {
        ListConsumerGroupsResult resultado = mock(ListConsumerGroupsResult.class);
        when(admin.listConsumerGroups()).thenReturn(resultado);
        when(resultado.all()).thenReturn(KafkaFuture.<Collection<ConsumerGroupListing>>completedFuture(List.of(
                new ConsumerGroupListing("pedidos-alertas-group", false),
                new ConsumerGroupListing("producto-ventas-group", false))));

        assertThat(service.listarGrupos()).containsExactly("pedidos-alertas-group", "producto-ventas-group");
    }

    @Test
    void describirGrupo_calculaElLagPorParticionYElTotal() {
        TopicPartition particion = new TopicPartition("pedidos.eventos", 0);

        MemberDescription miembro = mock(MemberDescription.class);
        when(miembro.consumerId()).thenReturn("consumidor-1");
        when(miembro.clientId()).thenReturn("console-consumer");
        when(miembro.host()).thenReturn("/172.18.0.6");
        when(miembro.assignment()).thenReturn(new MemberAssignment(Set.of(particion)));

        ConsumerGroupDescription descripcion = mock(ConsumerGroupDescription.class);
        when(descripcion.members()).thenReturn(List.of(miembro));

        DescribeConsumerGroupsResult resDescripcion = mock(DescribeConsumerGroupsResult.class);
        when(admin.describeConsumerGroups(anyCollection())).thenReturn(resDescripcion);
        when(resDescripcion.all()).thenReturn(KafkaFuture.completedFuture(Map.of("grupo-prueba", descripcion)));

        ListConsumerGroupOffsetsResult resOffsets = mock(ListConsumerGroupOffsetsResult.class);
        when(admin.listConsumerGroupOffsets("grupo-prueba")).thenReturn(resOffsets);
        when(resOffsets.partitionsToOffsetAndMetadata())
                .thenReturn(KafkaFuture.completedFuture(Map.of(particion, new OffsetAndMetadata(3L))));

        ListOffsetsResult resFinales = mock(ListOffsetsResult.class);
        when(admin.listOffsets(anyMap())).thenReturn(resFinales);
        when(resFinales.all()).thenReturn(KafkaFuture.completedFuture(
                Map.of(particion, new ListOffsetsResultInfo(10L, 0L, Optional.empty()))));

        GrupoDetalleDTO detalle = service.describirGrupo("grupo-prueba");

        assertThat(detalle.totalMiembros()).isEqualTo(1);
        assertThat(detalle.miembros().get(0).particiones()).containsExactly("pedidos.eventos-0");
        assertThat(detalle.offsets().get(0).offsetActual()).isEqualTo(3);
        assertThat(detalle.offsets().get(0).offsetFinal()).isEqualTo(10);
        assertThat(detalle.offsets().get(0).lag()).isEqualTo(7);
        assertThat(detalle.lagTotal()).isEqualTo(7);
    }

    @Test
    void describirGrupo_sinMiembrosNiOffsets_lanzaRecursoNoEncontrado() {
        ConsumerGroupDescription descripcion = mock(ConsumerGroupDescription.class);
        when(descripcion.members()).thenReturn(List.of());

        DescribeConsumerGroupsResult resDescripcion = mock(DescribeConsumerGroupsResult.class);
        when(admin.describeConsumerGroups(anyCollection())).thenReturn(resDescripcion);
        when(resDescripcion.all()).thenReturn(KafkaFuture.completedFuture(Map.of("fantasma", descripcion)));

        ListConsumerGroupOffsetsResult resOffsets = mock(ListConsumerGroupOffsetsResult.class);
        when(admin.listConsumerGroupOffsets("fantasma")).thenReturn(resOffsets);
        when(resOffsets.partitionsToOffsetAndMetadata()).thenReturn(KafkaFuture.completedFuture(Map.of()));

        assertThatThrownBy(() -> service.describirGrupo("fantasma"))
                .isInstanceOf(RecursoNoEncontradoException.class);
    }
}