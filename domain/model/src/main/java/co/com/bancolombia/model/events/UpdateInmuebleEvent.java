package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateInmuebleEvent implements DomainEvent {

    public static final String EVENT_TYPE = "co.arriendo.facil.inmueble.updated";

    private final InmueblePublicData inmueble;

    @Override
    public String eventType() {
        return EVENT_TYPE;
    }
}
