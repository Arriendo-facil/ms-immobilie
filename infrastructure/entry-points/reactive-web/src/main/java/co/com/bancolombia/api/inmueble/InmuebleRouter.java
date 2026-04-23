package co.com.bancolombia.api.inmueble;

import co.com.bancolombia.api.dto.common.ErrorResponse;
import co.com.bancolombia.api.dto.inmueble.CreateInmuebleDto;
import co.com.bancolombia.api.dto.inmueble.InmuebleResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.media.ArraySchema;
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

import static org.springframework.web.reactive.function.server.RequestPredicates.*;

@Tag(name = "Inmuebles", description = "Operaciones para gestión de inmuebles en la plataforma Arriendo Fácil")
@Configuration
public class InmuebleRouter {

    private static final String BASE_PATH = "/api/v1/inmuebles";

    @RouterOperations({
        @RouterOperation(
            path = BASE_PATH,
            method = RequestMethod.GET,
            beanClass = InmuebleHandler.class,
            beanMethod = "getinmueblesByUser",
            operation = @Operation(
                operationId = "getInmueblesByUser",
                summary = "Listar inmuebles propios",
                description = """
                    Retorna todos los inmuebles publicados por el usuario autenticado, incluyendo sus fotos.

                    **Reglas de negocio:**
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Si el usuario no tiene inmuebles registrados, se retorna `200 OK` con una lista vacía `[]`.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Lista de inmuebles del usuario con sus fotos. Retorna `[]` si no tiene inmuebles.",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            array = @ArraySchema(schema = @Schema(implementation = InmuebleResponse.class))
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
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
        ),
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
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Los usuarios con **plan gratuito** tienen un límite de **2 propiedades vigentes** simultáneas (estados `ACTIVE`, `INACTIVE` y `PAUSED`). Superar ese límite retorna `403 Forbidden`.
                    - Las fotos se incluyen en el cuerpo del request (campo `fotos`). Se admiten entre 1 y 20 fotos.
                    - La foto con `order = 1` se considera la **portada** del aviso y es la que se muestra en los listados.
                    - Los valores de `order` dentro de la lista de fotos deben ser **únicos**. Dos fotos con el mismo `order` retornan `400 Bad Request`.
                    - El inmueble queda publicado con estado `ACTIVE` y una **vigencia de 30 días** desde la fecha de creación (`expiresAt`).
                    - El servicio depende de **ms-user** para validar la existencia y el plan del propietario. Si ms-user no está disponible, se retorna `503 Service Unavailable`.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
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
                        description = "Datos de entrada inválidos — validación de campos fallida o valores de `order` duplicados en la lista de fotos (errorCode: `VALIDATION_ERROR`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "Límite de propiedades vigentes excedido — el plan gratuito permite máximo 2 propiedades vigentes (ACTIVE, INACTIVE o PAUSED) (errorCode: `PLAN_LIMIT_EXCEEDED`)",
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
        ),
        @RouterOperation(
            path = BASE_PATH + "/{id}/pause",
            method = RequestMethod.PATCH,
            beanClass = InmuebleHandler.class,
            beanMethod = "pauseInmueble",
            operation = @Operation(
                operationId = "pauseInmueble",
                summary = "Pausar la publicación de un inmueble",
                description = """
                    Pausa la publicación de un inmueble activo. El inmueble deja de aparecer en los resultados de búsqueda.

                    **Reglas de negocio:**
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Solo el propietario del inmueble puede pausarlo. Intentar pausar un inmueble ajeno retorna `403 Forbidden`.
                    - El inmueble debe estar en estado `ACTIVE` para poder pausarse. Cualquier otro estado retorna `409 Conflict`.
                    - Si el inmueble no existe, se retorna `404 Not Found`.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        description = "Identificador único del inmueble a pausar",
                        required = true,
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        schema = @Schema(type = "string")
                    ),
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "204",
                        description = "Inmueble pausado exitosamente"
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "El usuario no es propietario del inmueble (errorCode: `FORBIDDEN`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Inmueble no encontrado (errorCode: `NOT_FOUND`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "409",
                        description = "El inmueble no está en estado ACTIVE — transición de estado inválida (errorCode: `INVALID_STATE`)",
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
        ),
        @RouterOperation(
            path = BASE_PATH + "/{id}/resume",
            method = RequestMethod.PATCH,
            beanClass = InmuebleHandler.class,
            beanMethod = "resumeInmueble",
            operation = @Operation(
                operationId = "resumeInmueble",
                summary = "Reactivar la publicación de un inmueble",
                description = """
                    Reactiva un inmueble pausado. El inmueble vuelve a aparecer en los resultados de búsqueda.

                    **Reglas de negocio:**
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Solo el propietario del inmueble puede reactivarlo. Intentar reactivar un inmueble ajeno retorna `403 Forbidden`.
                    - El inmueble debe estar en estado `PAUSED` para poder reactivarse. Cualquier otro estado retorna `409 Conflict`.
                    - Si el inmueble no existe, se retorna `404 Not Found`.
                    - El tiempo que el inmueble estuvo pausado **no descuenta** de su vigencia: `expiresAt` se extiende automáticamente por la duración de la pausa.
                    - El servicio depende de **ms-user** para validar la existencia del propietario. Si ms-user no está disponible, se retorna `503 Service Unavailable`.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        description = "Identificador único del inmueble a reactivar",
                        required = true,
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        schema = @Schema(type = "string")
                    ),
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "204",
                        description = "Inmueble reactivado exitosamente"
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "El usuario no es propietario del inmueble (errorCode: `FORBIDDEN`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Inmueble no encontrado (errorCode: `NOT_FOUND`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "409",
                        description = "El inmueble no está en estado PAUSED — transición de estado inválida (errorCode: `INVALID_STATE`)",
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
        ),
        @RouterOperation(
            path = BASE_PATH + "/{id}/renew",
            method = RequestMethod.PATCH,
            beanClass = InmuebleHandler.class,
            beanMethod = "renewInmueble",
            operation = @Operation(
                operationId = "renewInmueble",
                summary = "Renovar la publicación de un inmueble",
                description = """
                    Renueva la vigencia de un inmueble expirado. El inmueble vuelve a aparecer en los resultados de búsqueda con una vigencia de 30 días a partir del momento de la renovación.

                    **Reglas de negocio:**
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Solo el propietario del inmueble puede renovarlo. Intentar renovar un inmueble ajeno retorna `403 Forbidden`.
                    - El inmueble debe estar en estado `INACTIVE` para poder renovarse. Cualquier otro estado retorna `409 Conflict`.
                    - Si el inmueble no existe, se retorna `404 Not Found`.
                    - La fecha original de publicación (`publishedAt`) no se modifica; solo se actualiza `expiresAt` con 30 días desde el momento de la renovación.
                    - El servicio depende de **ms-user** para validar la existencia del propietario. Si ms-user no está disponible, se retorna `503 Service Unavailable`.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        description = "Identificador único del inmueble a renovar",
                        required = true,
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        schema = @Schema(type = "string")
                    ),
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "204",
                        description = "Inmueble renovado exitosamente"
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "El usuario no es propietario del inmueble (errorCode: `FORBIDDEN`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Inmueble no encontrado (errorCode: `NOT_FOUND`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "409",
                        description = "El inmueble no está en estado INACTIVE — transición de estado inválida (errorCode: `INVALID_STATE`)",
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
        ),
        @RouterOperation(
            path = BASE_PATH + "/{id}",
            method = RequestMethod.DELETE,
            beanClass = InmuebleHandler.class,
            beanMethod = "deleteInmueble",
            operation = @Operation(
                operationId = "deleteInmueble",
                summary = "Eliminar un inmueble",
                description = """
                    Elimina permanentemente un inmueble junto con todas sus fotos asociadas.

                    **Reglas de negocio:**
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Solo el propietario del inmueble puede eliminarlo. Intentar eliminar un inmueble ajeno retorna `403 Forbidden`.
                    - Si el inmueble no existe, se retorna `404 Not Found`.
                    - La eliminación es **permanente e irreversible**: se borran el inmueble y todas sus fotos asociadas.
                    - Se emite el evento de dominio `co.arriendo.facil.inmueble.delete` para notificar a otros servicios.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        description = "Identificador único del inmueble a eliminar",
                        required = true,
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        schema = @Schema(type = "string")
                    ),
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
                responses = {
                    @ApiResponse(
                        responseCode = "204",
                        description = "Inmueble y sus fotos eliminados exitosamente"
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "403",
                        description = "El usuario no es propietario del inmueble (errorCode: `FORBIDDEN`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Inmueble no encontrado (errorCode: `NOT_FOUND`)",
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
        ),
        @RouterOperation(
            path = BASE_PATH + "/{id}",
            method = RequestMethod.PUT,
            beanClass = InmuebleHandler.class,
            beanMethod = "updateInmueble",
            operation = @Operation(
                operationId = "updateInmueble",
                summary = "Actualizar un inmueble existente",
                description = """
                    Actualiza los datos y las fotos de un inmueble publicado por el usuario autenticado.

                    **Reglas de negocio:**
                    - La identidad del propietario se extrae del header `X-User-Id`, propagado por el API Gateway. Si el header está ausente, se retorna `401 Unauthorized`.
                    - Si el inmueble no existe, se retorna `404 Not Found`.
                    - Las fotos anteriores son eliminadas y reemplazadas por las nuevas enviadas en el request.
                    - Los valores de `order` dentro de la lista de fotos deben ser **únicos**. Dos fotos con el mismo `order` retornan `400 Bad Request`.
                    """,
                tags = {"Inmuebles"},
                parameters = {
                    @Parameter(
                        name = "id",
                        in = ParameterIn.PATH,
                        description = "Identificador único del inmueble a actualizar",
                        required = true,
                        example = "550e8400-e29b-41d4-a716-446655440000",
                        schema = @Schema(type = "string")
                    ),
                    @Parameter(
                        name = "X-User-Id",
                        in = ParameterIn.HEADER,
                        description = "Identificador del usuario propietario, propagado por el API Gateway. Requerido — su ausencia retorna 401.",
                        required = true,
                        example = "usr_01HX9Z",
                        schema = @Schema(type = "string")
                    )
                },
                requestBody = @RequestBody(
                    description = "Nuevos datos del inmueble y sus fotos",
                    required = true,
                    content = @Content(
                        mediaType = MediaType.APPLICATION_JSON_VALUE,
                        schema = @Schema(implementation = CreateInmuebleDto.class)
                    )
                ),
                responses = {
                    @ApiResponse(
                        responseCode = "200",
                        description = "Inmueble actualizado exitosamente",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = InmuebleResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "400",
                        description = "Datos de entrada inválidos — validación de campos fallida o valores de `order` duplicados en la lista de fotos (errorCode: `VALIDATION_ERROR`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "401",
                        description = "Header `X-User-Id` ausente o vacío — el API Gateway no propagó la identidad del usuario (errorCode: `MISSING_USER_IDENTITY`)",
                        content = @Content(
                            mediaType = MediaType.APPLICATION_JSON_VALUE,
                            schema = @Schema(implementation = ErrorResponse.class)
                        )
                    ),
                    @ApiResponse(
                        responseCode = "404",
                        description = "Inmueble no encontrado — el id proporcionado no corresponde a ningún inmueble registrado (errorCode: `NOT_FOUND`)",
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
            POST(BASE_PATH).and(accept(MediaType.APPLICATION_JSON)),handler::crearInmueble)
                .andRoute(GET(BASE_PATH).and(accept(MediaType.APPLICATION_JSON)), handler::getinmueblesByUser)
                .andRoute(PUT(BASE_PATH + "/{id}").and(accept(MediaType.APPLICATION_JSON)), handler::updateInmueble)
                .andRoute(PATCH(BASE_PATH + "/{id}/pause").and(accept(MediaType.APPLICATION_JSON)), handler::pauseInmueble)
                .andRoute(PATCH(BASE_PATH + "/{id}/resume").and(accept(MediaType.APPLICATION_JSON)), handler::resumeInmueble)
                .andRoute(PATCH(BASE_PATH + "/{id}/renew").and(accept(MediaType.APPLICATION_JSON)), handler::renewInmueble)
                .andRoute(DELETE(BASE_PATH + "/{id}"), handler::deleteInmueble);
    }
}
