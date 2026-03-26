package co.com.bancolombia.api.dto.inmueble;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.hibernate.validator.constraints.URL;

@Schema(description = "Foto del inmueble")
public record FotoDto(

        @Schema(description = "URL pública de la imagen", example = "https://cdn.arriendofacil.co/fotos/sala.jpg")
        @NotBlank(message = "La URL de la foto es obligatoria")
        @URL(message = "La URL de la foto no es válida")
        String url,

        @Schema(description = "Posición de la foto en la galería (1 = portada)", example = "1", minimum = "1", maximum = "20")
        @NotNull(message = "El orden de la foto es obligatorio")
        @Min(value = 1, message = "El orden mínimo es 1")
        @Max(value = 20, message = "El orden máximo es 20")
        Integer order
) {}
