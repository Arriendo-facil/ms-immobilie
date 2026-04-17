package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class InmuebleCreatedEvent implements DomainEvent {

    public static final String EVENT_TYPE = "co.arriendo.facil.inmueble.created";

    private final Map<String, Object> user;
    private final InmueblePublicData inmueble;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
