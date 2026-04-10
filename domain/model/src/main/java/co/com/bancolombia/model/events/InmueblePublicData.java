package co.com.bancolombia.model.events;

import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.PropertyType;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class InmueblePublicData {
    private final String id;
    private final String userId;
    private final String title;
    private final String description;
    private final BigDecimal squareMeters;
    private final BigDecimal price;
    private final BusinessType businessType;
    private final PropertyType propertyType;
    private final String department;
    private final String country;
    private final String city;
    private final List<FotoPublicData> photos;

    public static InmueblePublicData from(Inmueble inmueble, List<Foto> photos) {
        return InmueblePublicData.builder()
                .id(inmueble.getId())
                .userId(inmueble.getUserId())
                .title(inmueble.getTitle())
                .description(inmueble.getDescription())
                .squareMeters(inmueble.getSquareMeters())
                .price(inmueble.getPrice())
                .businessType(inmueble.getBusinessType())
                .propertyType(inmueble.getPropertyType())
                .department(inmueble.getDepartment())
                .country(inmueble.getCountry())
                .city(inmueble.getCity())
                .photos(photos.stream().map(FotoPublicData::from).toList())
                .build();
    }
}
