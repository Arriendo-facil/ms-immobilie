package co.com.bancolombia.events;

import co.com.bancolombia.model.events.DomainEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import io.cloudevents.CloudEvent;
import io.cloudevents.core.builder.CloudEventBuilder;
import io.cloudevents.jackson.JsonCloudEventData;
import lombok.RequiredArgsConstructor;
import lombok.extern.java.Log;
import org.reactivecommons.api.domain.DomainEventBus;
import org.reactivecommons.async.impl.config.annotations.EnableDomainEventBus;
import reactor.core.publisher.Mono;
import tools.jackson.databind.json.JsonMapper;

import java.net.URI;
import java.time.OffsetDateTime;
import java.util.UUID;
import java.util.logging.Level;

import static reactor.core.publisher.Mono.from;

@Log
@RequiredArgsConstructor
@EnableDomainEventBus
public class ReactiveEventsGateway implements EventsGateway {

    public static final String EVENT_SOURCE = "ms-immobilie";

    private final DomainEventBus domainEventBus;
    private final JsonMapper mapper;

    @Override
    public Mono<Void> emit(DomainEvent event) {
        log.log(Level.INFO, "Publicando evento: {0}", event.eventType());
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(EVENT_SOURCE))
                .withType(event.eventType())
                .withTime(OffsetDateTime.now())
                .withData("application/json", JsonCloudEventData.wrap(mapper.valueToTree(event)))
                .build();
        return from(domainEventBus.emit(cloudEvent));
    }

    @Override
    public Mono<Void> notify(DomainEvent event) {
        log.log(Level.INFO, "Publicando notificacion: {0}", event.eventType());
        CloudEvent cloudEvent = CloudEventBuilder.v1()
                .withId(UUID.randomUUID().toString())
                .withSource(URI.create(EVENT_SOURCE))
                .withType(event.eventType())
                .withTime(OffsetDateTime.now())
                .withData("application/json", JsonCloudEventData.wrap(mapper.valueToTree(event)))
                .build();
        return from(domainEventBus.emit(cloudEvent));
    }
}
