package co.com.bancolombia.api.dto.inmueble;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Foto guardada del inmueble")
public record FotoResponse(

        @Schema(description = "Identificador único de la foto", example = "f7b1c2d3-4e5f-6789-abcd-ef0123456789")
        String id,

        @Schema(description = "URL pública de la imagen", example = "https://cdn.arriendofacil.co/fotos/sala.jpg")
        String url,

        @Schema(description = "Posición en la galería (1 = portada)", example = "1")
        Integer order
) {}
