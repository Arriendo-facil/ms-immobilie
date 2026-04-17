package co.com.bancolombia.events;

import co.com.bancolombia.model.events.DomainEvent;
import io.cloudevents.CloudEvent;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.reactivecommons.api.domain.DomainEventBus;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class ReactiveEventsGatewayTest {

    @Mock
    private DomainEventBus domainEventBus;

    @Mock
    private JsonMapper objectMapper;

    private ReactiveEventsGateway gateway;

    private static final String TEST_EVENT_TYPE = "co.arriendo-facil.test.event";
    private static final DomainEvent DUMMY_EVENT = () -> TEST_EVENT_TYPE;

    @BeforeEach
    void setUp() {
        when(objectMapper.valueToTree(any())).thenReturn(mock(ObjectNode.class));
        when(domainEventBus.emit(any(CloudEvent.class))).thenReturn(Mono.empty());
        gateway = new ReactiveEventsGateway(domainEventBus, objectMapper);
    }

    // -------------------------------------------------------------------------
    // emit()
    // -------------------------------------------------------------------------

    @Test
    void emit_delegatesToDomainEventBus() {
        gateway.emit(DUMMY_EVENT).block();

        verify(domainEventBus, times(1)).emit(any(CloudEvent.class));
    }

    @Test
    void emit_cloudEvent_hasCorrectType() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.emit(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(TEST_EVENT_TYPE);
    }

    @Test
    void emit_cloudEvent_hasCorrectSource() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.emit(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getSource().toString())
                .isEqualTo(ReactiveEventsGateway.EVENT_SOURCE);
    }

    @Test
    void emit_cloudEvent_hasNonNullId() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.emit(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getId()).isNotBlank();
    }

    @Test
    void emit_cloudEvent_hasNonNullTime() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.emit(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getTime()).isNotNull();
    }

    @Test
    void emit_cloudEvent_hasJsonData() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.emit(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getData()).isNotNull();
    }

    @Test
    void emit_returnsMonoVoid() {
        StepVerifier.create(gateway.emit(DUMMY_EVENT))
                .verifyComplete();
    }

    @Test
    void emit_whenDomainEventBusFails_propagatesError() {
        when(domainEventBus.emit(any(CloudEvent.class)))
                .thenReturn(Mono.error(new RuntimeException("bus error")));

        StepVerifier.create(gateway.emit(DUMMY_EVENT))
                .expectErrorMatches(ex -> ex instanceof RuntimeException
                        && "bus error".equals(ex.getMessage()))
                .verify();
    }

    // -------------------------------------------------------------------------
    // notify()
    // -------------------------------------------------------------------------

    @Test
    void notify_delegatesToDomainEventBus() {
        gateway.notify(DUMMY_EVENT).block();

        verify(domainEventBus, times(1)).emit(any(CloudEvent.class));
    }

    @Test
    void notify_cloudEvent_hasCorrectType() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.notify(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getType()).isEqualTo(TEST_EVENT_TYPE);
    }

    @Test
    void notify_cloudEvent_hasCorrectSource() {
        ArgumentCaptor<CloudEvent> captor = ArgumentCaptor.forClass(CloudEvent.class);

        gateway.notify(DUMMY_EVENT).block();

        verify(domainEventBus).emit(captor.capture());
        assertThat(captor.getValue().getSource().toString())
                .isEqualTo(ReactiveEventsGateway.EVENT_SOURCE);
    }

    @Test
    void notify_returnsMonoVoid() {
        StepVerifier.create(gateway.notify(DUMMY_EVENT))
                .verifyComplete();
    }
}
