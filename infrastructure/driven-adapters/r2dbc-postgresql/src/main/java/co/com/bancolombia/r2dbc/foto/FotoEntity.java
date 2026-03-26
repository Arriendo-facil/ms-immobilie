package co.com.bancolombia.r2dbc.foto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.Id;
import org.springframework.data.annotation.Version;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

import java.time.LocalDateTime;

@Table("fotos")
@Getter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class FotoEntity {

    @Id
    private String id;

    @Version
    private Long version;

    @Column("property_id")
    private String propertyId;

    private String url;

    private Integer order;

    @CreatedDate
    @Column("created_at")
    private LocalDateTime createdAt;
}
