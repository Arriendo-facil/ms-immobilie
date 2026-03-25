package co.com.bancolombia.model.events;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FotoPublicData {
    private final String id;
    private final String url;
    private final Integer order;
}
