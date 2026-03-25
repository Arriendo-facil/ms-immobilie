package co.com.bancolombia.events;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import reactor.core.publisher.Mono;
import org.reactivecommons.api.domain.DomainEventBus;

import io.cloudevents.CloudEvent;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.ObjectNode;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.assertEquals;

class ReactiveEventsGatewayTest {

    @Mock
    private DomainEventBus domainEventBus;

    @Mock
    private JsonMapper objectMapper;

    private ReactiveEventsGateway gateway;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
        gateway = new ReactiveEventsGateway(domainEventBus, objectMapper);
        when(domainEventBus.emit(any(CloudEvent.class))).thenReturn(Mono.empty());
    }

    @Test
    void testEmitLogsEvent() {
        Object event = new Object() {
            @Override
            public String toString() {
                return "testEvent";
            }
        };

        when(objectMapper.valueToTree(event)).thenReturn(mock(ObjectNode.class));

        gateway.emit(event).block();

        verify(domainEventBus, times(1)).emit(any(CloudEvent.class));
    }

   @Test
    void testEmitConstructsCloudEvent() {
        Object event = new Object() {
            public String toString() { return "testEvent"; }
        };

        when(objectMapper.valueToTree(event)).thenReturn(mock(ObjectNode.class));

        gateway.emit(event).block();

        ArgumentCaptor<CloudEvent> eventCaptor = ArgumentCaptor.forClass(CloudEvent.class);
        verify(domainEventBus, times(1)).emit(eventCaptor.capture());

        CloudEvent cloudEvent = eventCaptor.getValue();
        assertEquals(ReactiveEventsGateway.SOME_EVENT_NAME, cloudEvent.getType());
        assertEquals("https://reactive-commons.org/foos", cloudEvent.getSource().toString());
    }


        @Test
        void testNotifyLogsEvent() {
            Object event = new Object() {
                @Override
                public String toString() {
                    return "testNotificationEvent";
                }
            };

            when(objectMapper.valueToTree(event)).thenReturn(mock(ObjectNode.class));

            gateway.notify(event).block();

            verify(domainEventBus, times(1)).emit(any(CloudEvent.class));
        }

       @Test
        void testNotifyConstructsCloudEvent() {
            Object event = new Object() {
                public String toString() { return "testNotificationEvent"; }
            };

            when(objectMapper.valueToTree(event)).thenReturn(mock(ObjectNode.class));

            gateway.notify(event).block();

            ArgumentCaptor<CloudEvent> eventCaptor = ArgumentCaptor.forClass(CloudEvent.class);
            verify(domainEventBus, times(1)).emit(eventCaptor.capture());

            CloudEvent cloudEvent = eventCaptor.getValue();
            assertEquals(ReactiveEventsGateway.SOME_NOTIFICATION_NAME, cloudEvent.getType());
            assertEquals("https://reactive-commons.org/foos", cloudEvent.getSource().toString());
        }





}