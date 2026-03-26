package co.com.bancolombia.model.events;

import co.com.bancolombia.model.inmueble.BusinessType;
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
}
