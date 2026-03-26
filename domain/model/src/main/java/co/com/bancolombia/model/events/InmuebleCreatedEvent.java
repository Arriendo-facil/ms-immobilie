package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

import java.util.Map;

@Getter
@Builder
public class InmuebleCreatedEvent {
    private final Map<String, Object> user;
    private final InmueblePublicData inmueble;
}
