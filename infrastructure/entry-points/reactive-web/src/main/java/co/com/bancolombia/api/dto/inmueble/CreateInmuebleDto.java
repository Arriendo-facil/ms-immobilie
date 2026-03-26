package co.com.bancolombia.api.dto.inmueble;

import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.PropertyType;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotEmpty;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

import java.math.BigDecimal;
import java.util.List;

@Schema(description = "Datos para publicar un nuevo inmueble. El propietario se identifica mediante el header `X-User-Id` propagado por el API Gateway — no se incluye en el body.")
public record CreateInmuebleDto(
        @Schema(description = "Título del aviso (entre 5 y 150 caracteres)", example = "Apartamento moderno en El Poblado", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El título es obligatorio")
        @Size(min = 5, max = 150, message = "El título debe tener entre 5 y 150 caracteres")
        String title,

        @Schema(description = "Descripción detallada del inmueble (máx. 2000 caracteres)", example = "Amplio apartamento de 2 habitaciones con vista a la ciudad.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La descripción es obligatoria")
        @Size(max = 2000, message = "La descripción no puede superar los 2000 caracteres")
        String description,

        @Schema(description = "Área del inmueble en metros cuadrados", example = "75.5", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "Los metros cuadrados son obligatorios")
        @DecimalMin(value = "1.0", message = "Los metros cuadrados deben ser mayores a 0")
        BigDecimal squareMeters,

        @Schema(description = "Precio de arriendo mensual o venta en pesos colombianos", example = "1500000", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El precio es obligatorio")
        @DecimalMin(value = "1.0", message = "El precio debe ser mayor a 0")
        BigDecimal price,

        @Schema(description = "Tipo de negocio", example = "RENT", allowableValues = {"SALE", "RENT"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El tipo de negocio es obligatorio")
        BusinessType businessType,

        @Schema(description = "Tipo de propiedad", example = "APARTMENT", allowableValues = {"HOUSE", "APARTMENT", "FARM", "LOT", "GARAGE"}, requiredMode = Schema.RequiredMode.REQUIRED)
        @NotNull(message = "El tipo de propiedad es obligatorio")
        PropertyType propertyType,

        @Schema(description = "Departamento donde se ubica el inmueble", example = "Antioquia", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El departamento es obligatorio")
        String department,

        @Schema(description = "País", example = "Colombia", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "El país es obligatorio")
        String country,

        @Schema(description = "Ciudad", example = "Medellín", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La ciudad es obligatoria")
        String city,

        @Schema(description = "Dirección completa del inmueble", example = "Calle 10 # 43E-50, El Poblado", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotBlank(message = "La dirección completa es obligatoria")
        String fullAddress,

        @Schema(description = "Fotos del inmueble (mínimo 1, máximo 20). La foto con order=1 es la portada.", requiredMode = Schema.RequiredMode.REQUIRED)
        @NotEmpty(message = "Debe incluir al menos una foto")
        @Size(max = 20, message = "No se pueden incluir más de 20 fotos")
        List<@Valid FotoDto> fotos
) {}
