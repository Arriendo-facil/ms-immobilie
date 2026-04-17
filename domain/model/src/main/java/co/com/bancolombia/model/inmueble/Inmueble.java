package co.com.bancolombia.model.inmueble;

import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.ForbiddenException;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;

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

    public Inmueble requireOwner(String requestingUserId) {
        if (!this.userId.equals(requestingUserId)) {
            throw new ForbiddenException("FORBIDDEN", "No tienes permiso para modificar este inmueble");
        }
        return this;
    }

    public Inmueble requireStatus(InmuebleStatus status) {
        if (this.status != status) {
            throw new ConflictException(
                    "INVALID_STATE",
                    String.format("El inmueble debe estar %s, estado actual: %s", status, this.status)
            );
        }
        return this;
    }

    public Inmueble publish() {
        LocalDateTime publishedAt = LocalDateTime.now();
        return this.toBuilder()
                .id(UUID.randomUUID().toString())
                .status(InmuebleStatus.ACTIVE)
                .publishedAt(publishedAt)
                .expiresAt(publishedAt.plusDays(30))
                .build();
    }

    public Inmueble pause() {
        return this.toBuilder()
                .status(InmuebleStatus.PAUSED)
                .pausedAt(LocalDateTime.now())
                .build();
    }

    public Inmueble resume() {
        Duration pauseDuration = Duration.between(this.pausedAt, LocalDateTime.now());
        return this.toBuilder()
                .status(InmuebleStatus.ACTIVE)
                .expiresAt(this.expiresAt.plus(pauseDuration))
                .pausedAt(null)
                .build();
    }

    public Inmueble renew() {
        return this.toBuilder()
                .status(InmuebleStatus.ACTIVE)
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }



}
