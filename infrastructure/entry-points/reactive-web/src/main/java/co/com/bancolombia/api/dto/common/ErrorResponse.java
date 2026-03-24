package co.com.bancolombia.api.dto.common;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "Respuesta de error estándar")
public record ErrorResponse(
        @Schema(description = "Código semántico del error", example = "NOT_FOUND")
        String errorCode,
        @Schema(description = "Mensaje descriptivo del error", example = "Recurso no encontrado")
        String message
) {}
