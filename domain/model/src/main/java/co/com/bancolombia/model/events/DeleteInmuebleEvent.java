package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class DeleteInmuebleEvent implements DomainEvent{
    private static final String TYPE = "co.arriendo.facil.inmueble.delete";

    private final String inmuebleId;

    @Override
    public String eventType() {
        return TYPE;
    }
}
