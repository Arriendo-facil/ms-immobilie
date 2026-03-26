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
}
