package co.com.bancolombia.model.events;

import co.com.bancolombia.model.foto.Foto;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class FotoPublicData {
    private final String id;
    private final String url;
    private final Integer order;

    public static FotoPublicData from(Foto foto) {
        return FotoPublicData.builder()
                .id(foto.getId())
                .url(foto.getUrl())
                .order(foto.getOrder())
                .build();
    }
}
