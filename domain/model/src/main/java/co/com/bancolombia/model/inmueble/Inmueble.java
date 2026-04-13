package co.com.bancolombia.model.inmueble;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder(toBuilder = true)
public class Inmueble {
    private String id;
    private String userId;
    private String title;
    private String description;
    private BigDecimal squareMeters;
    private BigDecimal price;
    private BusinessType businessType;
    private PropertyType propertyType;
    private InmuebleStatus status;
    private String department;
    private String country;
    private String city;
    private String fullAddress;
    private LocalDateTime publishedAt;
    private LocalDateTime expiresAt;
    private LocalDateTime pausedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public Inmueble update(Inmueble newInmueble) {
        return Inmueble.builder()
                .id(this.getId())
                .userId(this.userId)
                .title(newInmueble.getTitle())
                .description(newInmueble.getDescription())
                .squareMeters(newInmueble.getSquareMeters())
                .price(newInmueble.getPrice())
                .businessType(newInmueble.getBusinessType())
                .propertyType(newInmueble.getPropertyType())
                .status(this.status)
                .department(newInmueble.getDepartment())
                .country(newInmueble.getCountry())
                .city(newInmueble.getCity())
                .fullAddress(newInmueble.getFullAddress())
                .publishedAt(this.publishedAt)
                .expiresAt(this.expiresAt)
                .pausedAt(this.pausedAt)
                .createdAt(this.createdAt)
                .updatedAt(this.updatedAt)
                .build();
    }
}
