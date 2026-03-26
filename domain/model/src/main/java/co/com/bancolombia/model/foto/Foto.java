package co.com.bancolombia.model.foto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

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
}
