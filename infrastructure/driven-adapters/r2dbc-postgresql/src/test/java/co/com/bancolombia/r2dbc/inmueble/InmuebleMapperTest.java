package co.com.bancolombia.r2dbc.inmueble;

import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class InmuebleMapperTest {

    private InmuebleMapper mapper;

    @BeforeEach
    void setUp() {
        mapper = new InmuebleMapperImpl();
    }

    // -------------------------------------------------------------------------
    // toEntity()
    // -------------------------------------------------------------------------

    @Test
    void toEntity_mapsAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 25, 10, 0);

        Inmueble inmueble = Inmueble.builder()
                .id("prop-123")
                .userId("user-abc")
                .title("Casa en el campo")
                .description("Hermosa casa")
                .squareMeters(new BigDecimal("200.00"))
                .price(new BigDecimal("500000000"))
                .businessType(BusinessType.SALE)
                .propertyType(PropertyType.HOUSE)
                .status(InmuebleStatus.ACTIVE)
                .department("Cundinamarca")
                .country("Colombia")
                .city("Bogotá")
                .fullAddress("Carrera 7 # 45-20")
                .publishedAt(now)
                .expiresAt(now.plusMonths(6))
                .pausedAt(null)
                .createdAt(now)
                .updatedAt(now)
                .build();

        InmuebleEntity entity = mapper.toEntity(inmueble);

        assertThat(entity.getId()).isEqualTo("prop-123");
        assertThat(entity.getUserId()).isEqualTo("user-abc");
        assertThat(entity.getTitle()).isEqualTo("Casa en el campo");
        assertThat(entity.getDescription()).isEqualTo("Hermosa casa");
        assertThat(entity.getSquareMeters()).isEqualByComparingTo(new BigDecimal("200.00"));
        assertThat(entity.getPrice()).isEqualByComparingTo(new BigDecimal("500000000"));
        assertThat(entity.getBusinessType()).isEqualTo("SALE");
        assertThat(entity.getPropertyType()).isEqualTo("HOUSE");
        assertThat(entity.getStatus()).isEqualTo("ACTIVE");
        assertThat(entity.getDepartment()).isEqualTo("Cundinamarca");
        assertThat(entity.getCountry()).isEqualTo("Colombia");
        assertThat(entity.getCity()).isEqualTo("Bogotá");
        assertThat(entity.getFullAddress()).isEqualTo("Carrera 7 # 45-20");
        assertThat(entity.getPublishedAt()).isEqualTo(now);
        assertThat(entity.getExpiresAt()).isEqualTo(now.plusMonths(6));
        assertThat(entity.getPausedAt()).isNull();
        assertThat(entity.getCreatedAt()).isEqualTo(now);
        assertThat(entity.getUpdatedAt()).isEqualTo(now);
    }

    // -------------------------------------------------------------------------
    // toDomain()
    // -------------------------------------------------------------------------

    @Test
    void toDomain_mapsAllFieldsCorrectly() {
        LocalDateTime now = LocalDateTime.of(2026, 3, 25, 10, 0);

        InmuebleEntity entity = InmuebleEntity.builder()
                .id("prop-456")
                .version(1L)
                .userId("user-xyz")
                .title("Apartamento moderno")
                .description("Con vista al mar")
                .squareMeters(new BigDecimal("90.50"))
                .price(new BigDecimal("800000000"))
                .businessType("SALE")
                .propertyType("APARTMENT")
                .status("INACTIVE")
                .department("Bolívar")
                .country("Colombia")
                .city("Cartagena")
                .fullAddress("Bocagrande Calle 5 # 3-10")
                .publishedAt(now)
                .expiresAt(now.plusMonths(3))
                .pausedAt(now.minusDays(1))
                .createdAt(now.minusDays(10))
                .updatedAt(now)
                .build();

        Inmueble inmueble = mapper.toDomain(entity);

        assertThat(inmueble.getId()).isEqualTo("prop-456");
        assertThat(inmueble.getUserId()).isEqualTo("user-xyz");
        assertThat(inmueble.getTitle()).isEqualTo("Apartamento moderno");
        assertThat(inmueble.getDescription()).isEqualTo("Con vista al mar");
        assertThat(inmueble.getSquareMeters()).isEqualByComparingTo(new BigDecimal("90.50"));
        assertThat(inmueble.getPrice()).isEqualByComparingTo(new BigDecimal("800000000"));
        assertThat(inmueble.getBusinessType()).isEqualTo(BusinessType.SALE);
        assertThat(inmueble.getPropertyType()).isEqualTo(PropertyType.APARTMENT);
        assertThat(inmueble.getStatus()).isEqualTo(InmuebleStatus.INACTIVE);
        assertThat(inmueble.getDepartment()).isEqualTo("Bolívar");
        assertThat(inmueble.getCountry()).isEqualTo("Colombia");
        assertThat(inmueble.getCity()).isEqualTo("Cartagena");
        assertThat(inmueble.getFullAddress()).isEqualTo("Bocagrande Calle 5 # 3-10");
        assertThat(inmueble.getPublishedAt()).isEqualTo(now);
        assertThat(inmueble.getExpiresAt()).isEqualTo(now.plusMonths(3));
        assertThat(inmueble.getPausedAt()).isEqualTo(now.minusDays(1));
        assertThat(inmueble.getCreatedAt()).isEqualTo(now.minusDays(10));
        assertThat(inmueble.getUpdatedAt()).isEqualTo(now);
    }

    // -------------------------------------------------------------------------
    // Conversiones de enum
    // -------------------------------------------------------------------------

    @Test
    void toEntity_convertsBusinessTypeToString() {
        Inmueble inmueble = Inmueble.builder().businessType(BusinessType.RENT).build();
        assertThat(mapper.toEntity(inmueble).getBusinessType()).isEqualTo("RENT");
    }

    @Test
    void toDomain_convertsStringToBusinessType() {
        InmuebleEntity entity = InmuebleEntity.builder().businessType("SALE").build();
        assertThat(mapper.toDomain(entity).getBusinessType()).isEqualTo(BusinessType.SALE);
    }

    @Test
    void toEntity_convertsPropertyTypeToString() {
        Inmueble inmueble = Inmueble.builder().propertyType(PropertyType.APARTMENT).build();
        assertThat(mapper.toEntity(inmueble).getPropertyType()).isEqualTo("APARTMENT");
    }

    @Test
    void toDomain_convertsStringToPropertyType() {
        InmuebleEntity entity = InmuebleEntity.builder().propertyType("FARM").build();
        assertThat(mapper.toDomain(entity).getPropertyType()).isEqualTo(PropertyType.FARM);
    }

    @Test
    void toEntity_convertsStatusToString() {
        Inmueble inmueble = Inmueble.builder().status(InmuebleStatus.PAUSED).build();
        assertThat(mapper.toEntity(inmueble).getStatus()).isEqualTo("PAUSED");
    }

    @Test
    void toDomain_convertsStringToStatus() {
        InmuebleEntity entity = InmuebleEntity.builder().status("DELETED").build();
        assertThat(mapper.toDomain(entity).getStatus()).isEqualTo(InmuebleStatus.DELETED);
    }

    // -------------------------------------------------------------------------
    // null safety en métodos default
    // -------------------------------------------------------------------------

    @Test
    void businessTypeToString_whenNull_returnsNull() {
        assertThat(mapper.businessTypeToString(null)).isNull();
    }

    @Test
    void stringToBusinessType_whenNull_returnsNull() {
        assertThat(mapper.stringToBusinessType(null)).isNull();
    }

    @Test
    void propertyTypeToString_whenNull_returnsNull() {
        assertThat(mapper.propertyTypeToString(null)).isNull();
    }

    @Test
    void stringToPropertyType_whenNull_returnsNull() {
        assertThat(mapper.stringToPropertyType(null)).isNull();
    }

    @Test
    void inmuebleStatusToString_whenNull_returnsNull() {
        assertThat(mapper.inmuebleStatusToString(null)).isNull();
    }

    @Test
    void stringToInmuebleStatus_whenNull_returnsNull() {
        assertThat(mapper.stringToInmuebleStatus(null)).isNull();
    }
}
