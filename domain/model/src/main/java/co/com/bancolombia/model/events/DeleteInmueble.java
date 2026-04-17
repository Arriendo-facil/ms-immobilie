package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteInmueble implements DomainEvent{
    private static final String TYPE = "co.arriendo.facil.inmueble.updated";

    private final String inmuebleId;

    @Override
    public String eventType() {
        return TYPE;
    }
}
