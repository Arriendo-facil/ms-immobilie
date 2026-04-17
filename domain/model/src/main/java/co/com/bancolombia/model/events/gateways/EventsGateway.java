package co.com.bancolombia.model.events.gateways;

import co.com.bancolombia.model.events.DomainEvent;
import reactor.core.publisher.Mono;

public interface EventsGateway {
    Mono<Void> emit(DomainEvent event);
    Mono<Void> notify(DomainEvent event);
}
