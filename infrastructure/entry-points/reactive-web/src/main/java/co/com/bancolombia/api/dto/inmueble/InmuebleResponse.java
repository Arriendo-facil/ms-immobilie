package co.com.bancolombia.api.dto.inmueble;

import io.swagger.v3.oas.annotations.media.Schema;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Schema(description = "Inmueble creado con sus fotos")
public record InmuebleResponse(

        @Schema(description = "Identificador único del inmueble", example = "a1b2c3d4-e5f6-7890-abcd-ef1234567890")
        String id,

        @Schema(description = "ID del propietario", example = "usr_01HX9Z")
        String userId,

        @Schema(description = "Título del aviso", example = "Apartamento moderno en El Poblado")
        String title,

        @Schema(description = "Descripción detallada", example = "Amplio apartamento de 2 habitaciones con vista a la ciudad.")
        String description,

        @Schema(description = "Área en metros cuadrados", example = "75.5")
        BigDecimal squareMeters,

        @Schema(description = "Precio (arriendo mensual o venta)", example = "1500000")
        BigDecimal price,

        @Schema(description = "Tipo de negocio", example = "RENT", allowableValues = {"SALE", "RENT"})
        String businessType,

        @Schema(description = "Tipo de propiedad", example = "APARTMENT",
                allowableValues = {"HOUSE", "APARTMENT", "FARM", "LOT", "GARAGE"})
        String propertyType,

        @Schema(description = "Estado del inmueble", example = "ACTIVE")
        String status,

        @Schema(description = "Departamento", example = "Antioquia")
        String department,

        @Schema(description = "País", example = "Colombia")
        String country,

        @Schema(description = "Ciudad", example = "Medellín")
        String city,

        @Schema(description = "Dirección completa", example = "Calle 10 # 43E-50, El Poblado")
        String fullAddress,

        @Schema(description = "Fecha de publicación", example = "2026-03-25T10:00:00")
        LocalDateTime publishedAt,

        @Schema(description = "Fecha de expiración (30 días desde publicación)", example = "2026-04-24T10:00:00")
        LocalDateTime expiresAt,

        @Schema(description = "Fecha de creación", example = "2026-03-25T10:00:00")
        LocalDateTime createdAt,

        @Schema(description = "Fotos del inmueble ordenadas por posición")
        List<FotoResponse> fotos
) {}
