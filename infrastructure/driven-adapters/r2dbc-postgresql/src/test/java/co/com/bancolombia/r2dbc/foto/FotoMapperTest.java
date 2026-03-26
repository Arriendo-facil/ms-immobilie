package co.com.bancolombia.r2dbc.foto;

import co.com.bancolombia.model.foto.Foto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class FotoMapperTest {

    private FotoMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new FotoMapperImpl();
    }

    @Test
    void toEntity_mapsAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 25, 10, 0);

        Foto foto = Foto.builder()
                .id("foto-001")
                .propertyId("prop-123")
                .url("https://cdn.example.com/foto1.jpg")
                .order(1)
                .createdAt(now)
                .build();

        FotoEntity entity = mapper.toEntity(foto);

        assertThat(entity.getId()).isEqualTo("foto-001");
        assertThat(entity.getPropertyId()).isEqualTo("prop-123");
        assertThat(entity.getUrl()).isEqualTo("https://cdn.example.com/foto1.jpg");
        assertThat(entity.getOrder()).isEqualTo(1);
        assertThat(entity.getCreatedAt()).isEqualTo(now);
    }

    @Test
    void toDomain_mapsAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 25, 10, 0);

        FotoEntity entity = FotoEntity.builder()
                .id("foto-002")
                .version(0L)
                .propertyId("prop-456")
                .url("https://cdn.example.com/foto2.jpg")
                .order(3)
                .createdAt(now)
                .build();

        Foto foto = mapper.toDomain(entity);

        assertThat(foto.getId()).isEqualTo("foto-002");
        assertThat(foto.getPropertyId()).isEqualTo("prop-456");
        assertThat(foto.getUrl()).isEqualTo("https://cdn.example.com/foto2.jpg");
        assertThat(foto.getOrder()).isEqualTo(3);
        assertThat(foto.getCreatedAt()).isEqualTo(now);
    }
}
