package co.com.bancolombia.r2dbc.inmueble;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Table("inmuebles")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InmuebleEntity {

    @Id
    private String id;

    @Version
    private Long version;

    @Column("user_id")
    private String userId;

    private String title;

    private String description;

    @Column("square_meters")
    private BigDecimal squareMeters;

    private BigDecimal price;

    @Column("business_type")
    private String businessType;

    @Column("property_type")
    private String propertyType;

    private String status;

    private String department;

    private String country;

    private String city;

    @Column("full_address")
    private String fullAddress;

    @Column("published_at")
    private LocalDateTime publishedAt;

    @Column("expires_at")
    private LocalDateTime expiresAt;

    @Column("paused_at")
    private LocalDateTime pausedAt;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;

    @LastModifiedDate
    @Column("updated_at")
    private LocalDateTime updatedAt;
}
