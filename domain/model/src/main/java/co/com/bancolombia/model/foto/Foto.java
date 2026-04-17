package co.com.bancolombia.model.foto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Foto {
    private String id;
    private String propertyId;
    private String url;
    private Integer order;
    private LocalDateTime createdAt;

    public static List<Foto> prepareForSave(List<Foto> fotos, String propertyId) {
        return fotos.stream()
                .map(foto -> foto.toBuilder()
                        .id(UUID.randomUUID().toString())
                        .propertyId(propertyId)
                        .build())
                .toList();
    }
}
