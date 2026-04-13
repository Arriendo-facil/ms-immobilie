package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class UpdateInmuebleEvent {
    private final InmueblePublicData inmueble;
}
