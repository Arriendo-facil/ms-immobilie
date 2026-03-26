package co.com.bancolombia.api.inmueble;

import co.com.bancolombia.api.dto.common.ErrorResponse;
import co.com.bancolombia.api.dto.inmueble.CreateInmuebleDto;
import co.com.bancolombia.api.dto.inmueble.InmuebleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springdoc.core.annotations.RouterOperation;
import org.springdoc.core.annotations.RouterOperations;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.reactive.function.server.RouterFunction;
import org.springframework.web.reactive.function.server.RouterFunctions;
import org.springframework.web.reactive.function.server.ServerResponse;

import static org.springframework.web.reactive.function.server.RequestPredicates.POST;
import static org.springframework.web.reactive.function.server.RequestPredicates.accept;

@Tag(name = "Inmuebles", description = "Operaciones para gestión de inmuebles en la plataforma Arriendo Fácil")
@Configuration
public class InmuebleRouter {

    private static final String BASE_PATH = "/api/v1/inmuebles";

    @RouterOperations({
        @RouterOperation(
            path = BASE_PATH,
            method = RequestMethod.POST,
            beanClass = InmuebleHandler.class,
            beanMethod = "crearInmueble",
            operation = @Operation(
                operationId = "crearInmueble",
                summary = "Publicar un nuevo inmueble",
                description = """
                    Crea y publica un nuevo aviso de inmueble en la plataforma.

                    **Reglas de negocio:**
                    - El campo `userId` identifica al propietario del aviso. Será reemplazado por extracción desde JWT cuando se implemente autenticación.
                    - Los usuarios con **plan gratuito** tienen un límite de **2 propiedades activas** simultáneas. Superar ese límite retorna `403 Forbidden`.
                    - Las fotos se incluyen en el cuerpo del request (campo `fotos`). Se admiten entre 1 y 20 fotos.
                    - La foto con `order = 1` se considera la **portada** del aviso y es la que se muestra en los listados.
                    - El inmueble queda publicado con estado `ACTIVE` y una **vigencia de 30 días** desde la fecha de creación (`expiresAt`).
                    - El servicio depende de **ms-user** para validar la existencia y el plan del propietario. Si ms-user no está disponible, se retorna `503 Service Unavailable`.
                    """,
                tags = {"Inmuebles"},
                requestBody = @RequestBody(
                    description = "Datos del inmueble y sus fotos",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CreateInmuebleDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "201",
                        description = "Inmueble creado y publicado exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InmuebleResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Datos de entrada inválidos — validación de campos fallida",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "Límite de propiedades activas excedido — el plan gratuito permite máximo 2 propiedades activas",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "503",
                        description = "Servicio ms-user no disponible — no fue posible validar el propietario",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "500",
                        description = "Error interno del servidor",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    )
                }
            )
        )
    })
    @Bean
    public RouterFunction<ServerResponse> inmuebleRoutes(InmuebleHandler handler) {
        return RouterFunctions.route(
            POST(BASE_PATH).and(accept(MediaType.APPLICATION_JSON)),
            handler::crearInmueble
        );
    }
}
