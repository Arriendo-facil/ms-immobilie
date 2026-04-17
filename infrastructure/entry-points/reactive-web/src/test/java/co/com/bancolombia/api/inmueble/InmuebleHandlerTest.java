package co.com.bancolombia.api.inmueble;

import co.com.bancolombia.api.config.GlobalErrorHandler;
import co.com.bancolombia.api.config.UserIdExtractorFilter;
import co.com.bancolombia.api.mapper.InmuebleApiMapperImpl;
import co.com.bancolombia.model.exception.ExternalServiceException;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.usecase.inmueble.CrearInmuebleUseCase;
import co.com.bancolombia.usecase.inmueble.FindAllImobilieByUserUseCase;
import co.com.bancolombia.usecase.inmueble.PauseInmueblePublicationUseCase;
import co.com.bancolombia.usecase.inmueble.UpdateInmuebleUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import jakarta.validation.Validation;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.MediaType;
import org.springframework.test.web.reactive.server.MockServerConfigurer;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class InmuebleHandlerTest {

    private static final String URL = "/api/v1/inmuebles";
    private static final ObjectMapper TEST_MAPPER = new ObjectMapper();

    @Mock
    private CrearInmuebleUseCase crearInmuebleUseCase;

    @Mock
    private FindAllImobilieByUserUseCase findAllImobilieByUserUseCase;

    @Mock
    private UpdateInmuebleUseCase updateInmuebleUseCase;

    @Mock
    private PauseInmueblePublicationUseCase pauseInmueblePublicationUseCase;

    private WebTestClient webTestClient;

    @BeforeEach
    void setUp() {
        var mapper = new InmuebleApiMapperImpl();
        var validator = Validation.buildDefaultValidatorFactory().getValidator();
        var handler = new InmuebleHandler(crearInmuebleUseCase, findAllImobilieByUserUseCase, updateInmuebleUseCase, pauseInmueblePublicationUseCase, mapper, validator);
        var routerFunction = new InmuebleRouter().inmuebleRoutes(handler);

        var errorHandler = new GlobalErrorHandler();

        webTestClient = WebTestClient
                .bindToRouterFunction(routerFunction)
                .webFilter(new UserIdExtractorFilter())
                .apply(new MockServerConfigurer() {
                    @Override
                    public void beforeServerCreated(
                            org.springframework.web.server.adapter.WebHttpHandlerBuilder builder) {
                        builder.exceptionHandler(errorHandler);
                    }
                })
                .build();
    }

    @Test
    void crearInmueble_cuandoBodyValido_retorna201ConInmuebleCreado() {
        when(crearInmuebleUseCase.execute(any(), any()))
                .thenReturn(Mono.just(buildInmuebleConFotos()));

        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildValidRequest())
                .exchange()
                .expectStatus().isCreated()
                .expectBody()
                .jsonPath("$.id").isEqualTo("test-id")
                .jsonPath("$.userId").isEqualTo("usr_01HX9Z")
                .jsonPath("$.title").isEqualTo("Apartamento moderno en El Poblado")
                .jsonPath("$.status").isEqualTo("ACTIVE")
                .jsonPath("$.fotos[0].url").isEqualTo("https://cdn.arriendofacil.co/fotos/sala.jpg")
                .jsonPath("$.fotos[0].order").isEqualTo(1);
    }

    @Test
    void crearInmueble_cuandoFaltaHeaderUserId_retorna401() {
        webTestClient.post().uri(URL)
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildValidRequest())
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MISSING_USER_IDENTITY");
    }

    @Test
    void crearInmueble_cuandoFaltanCamposObligatorios_retorna400() {
        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue("""
                        {
                            "title": "Ap"
                        }
                        """)
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void crearInmueble_cuandoTituloDemasiadoCorto_retorna400() {
        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestWithField("title", "\"Ap\""))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void crearInmueble_cuandoUrlFotoInvalida_retorna400() {
        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestWithField("fotos", "[{\"url\": \"no-es-una-url\", \"order\": 1}]"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void crearInmueble_cuandoListaFotosVacia_retorna400() {
        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestWithField("fotos", "[]"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void crearInmueble_cuandoOrdenFotosDuplicado_retorna400() {
        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildRequestWithField("fotos",
                        "[{\"url\": \"https://cdn.arriendofacil.co/foto1.jpg\", \"order\": 1},"
                        + "{\"url\": \"https://cdn.arriendofacil.co/foto2.jpg\", \"order\": 1}]"))
                .exchange()
                .expectStatus().isBadRequest()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("VALIDATION_ERROR");
    }

    @Test
    void crearInmueble_cuandoPlanLimiteSuperado_retorna403() {
        when(crearInmuebleUseCase.execute(any(), any()))
                .thenReturn(Mono.error(new ForbiddenException("LIMIT_EXCEEDED",
                        "El plan gratuito permite máximo 2 propiedades activas")));

        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildValidRequest())
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("LIMIT_EXCEEDED")
                .jsonPath("$.message").isEqualTo("El plan gratuito permite máximo 2 propiedades activas");
    }

    @Test
    void crearInmueble_cuandoMsUserNoDisponible_retorna503() {
        when(crearInmuebleUseCase.execute(any(), any()))
                .thenReturn(Mono.error(new ExternalServiceException("MS_USER_UNAVAILABLE",
                        "ms-user no disponible")));

        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildValidRequest())
                .exchange()
                .expectStatus().isEqualTo(503)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MS_USER_UNAVAILABLE")
                .jsonPath("$.message").isEqualTo("ms-user no disponible");
    }

    @Test
    void crearInmueble_cuandoErrorInesperado_retorna500() {
        when(crearInmuebleUseCase.execute(any(), any()))
                .thenReturn(Mono.error(new RuntimeException("Error inesperado")));

        webTestClient.post().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .contentType(MediaType.APPLICATION_JSON)
                .bodyValue(buildValidRequest())
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INTERNAL_ERROR");
    }

    // -------------------------------------------------------------------------
    // GET /api/v1/inmuebles — getinmueblesByUser
    // -------------------------------------------------------------------------

    @Test
    void getInmueblesByUser_cuandoHayInmuebles_retorna200ConLista() {
        when(findAllImobilieByUserUseCase.execute("usr_01HX9Z"))
                .thenReturn(Flux.just(buildInmuebleConFotos()));

        webTestClient.get().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$[0].id").isEqualTo("test-id")
                .jsonPath("$[0].userId").isEqualTo("usr_01HX9Z")
                .jsonPath("$[0].title").isEqualTo("Apartamento moderno en El Poblado")
                .jsonPath("$[0].status").isEqualTo("ACTIVE")
                .jsonPath("$[0].fotos[0].url").isEqualTo("https://cdn.arriendofacil.co/fotos/sala.jpg")
                .jsonPath("$[0].fotos[0].order").isEqualTo(1);
    }

    @Test
    void getInmueblesByUser_cuandoNoHayInmuebles_retorna200ConListaVacia() {
        when(findAllImobilieByUserUseCase.execute("usr_01HX9Z"))
                .thenReturn(Flux.empty());

        webTestClient.get().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$").isArray()
                .jsonPath("$.length()").isEqualTo(0);
    }

    @Test
    void getInmueblesByUser_cuandoFaltaHeaderUserId_retorna401() {
        webTestClient.get().uri(URL)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MISSING_USER_IDENTITY");
    }

    @Test
    void getInmueblesByUser_cuandoUseCaseFalla_retorna500() {
        when(findAllImobilieByUserUseCase.execute("usr_01HX9Z"))
                .thenReturn(Flux.error(new RuntimeException("Error inesperado")));

        webTestClient.get().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INTERNAL_ERROR");
    }

    @Test
    void getInmueblesByUser_cuandoHayMultiplesInmuebles_retornaTodosEnLista() {
        var inmueble2 = new InmuebleConFotos(
                Inmueble.builder()
                        .id("test-id-2").userId("usr_01HX9Z")
                        .title("Casa en Laureles").description("Casa amplia")
                        .squareMeters(new BigDecimal("120.0")).price(new BigDecimal("2500000"))
                        .businessType(BusinessType.RENT).propertyType(PropertyType.HOUSE)
                        .status(InmuebleStatus.ACTIVE).department("Antioquia")
                        .country("Colombia").city("Medellín").fullAddress("Calle 33 # 75-20")
                        .publishedAt(LocalDateTime.now()).expiresAt(LocalDateTime.now().plusDays(30))
                        .createdAt(LocalDateTime.now()).build(),
                List.of()
        );

        when(findAllImobilieByUserUseCase.execute("usr_01HX9Z"))
                .thenReturn(Flux.just(buildInmuebleConFotos(), inmueble2));

        webTestClient.get().uri(URL)
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.length()").isEqualTo(2)
                .jsonPath("$[0].id").isEqualTo("test-id")
                .jsonPath("$[1].id").isEqualTo("test-id-2");
    }

    // -------------------------------------------------------------------------
    // PATCH /api/v1/inmuebles/{id}/pause — pauseInmueble
    // -------------------------------------------------------------------------

    @Test
    void pauseInmueble_cuandoInmuebleActivo_retorna204() {
        when(pauseInmueblePublicationUseCase.execute("inmueble-id", "usr_01HX9Z"))
                .thenReturn(Mono.empty());

        webTestClient.patch().uri(URL + "/inmueble-id/pause")
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNoContent();
    }

    @Test
    void pauseInmueble_cuandoFaltaHeaderUserId_retorna401() {
        webTestClient.patch().uri(URL + "/inmueble-id/pause")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isUnauthorized()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("MISSING_USER_IDENTITY");
    }

    @Test
    void pauseInmueble_cuandoInmuebleNoExiste_retorna404() {
        when(pauseInmueblePublicationUseCase.execute("inmueble-id", "usr_01HX9Z"))
                .thenReturn(Mono.error(new NotFoundException("NOT_FOUND", "No se encontro el inmueble")));

        webTestClient.patch().uri(URL + "/inmueble-id/pause")
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isNotFound()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("NOT_FOUND");
    }

    @Test
    void pauseInmueble_cuandoNoEsPropietario_retorna403() {
        when(pauseInmueblePublicationUseCase.execute("inmueble-id", "usr_01HX9Z"))
                .thenReturn(Mono.error(new ForbiddenException("FORBIDDEN", "No tienes permiso para modificar este inmueble")));

        webTestClient.patch().uri(URL + "/inmueble-id/pause")
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isForbidden()
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("FORBIDDEN");
    }

    @Test
    void pauseInmueble_cuandoEstadoInvalido_retorna409() {
        when(pauseInmueblePublicationUseCase.execute("inmueble-id", "usr_01HX9Z"))
                .thenReturn(Mono.error(new ConflictException("INVALID_STATE",
                        "El inmueble debe estar ACTIVO para pausarse, estado actual: PAUSED")));

        webTestClient.patch().uri(URL + "/inmueble-id/pause")
                .header(GlobalErrorHandler.USER_ID_HEADER, "usr_01HX9Z")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isEqualTo(409)
                .expectBody()
                .jsonPath("$.errorCode").isEqualTo("INVALID_STATE");
    }

    // --- helpers ---

    private static String buildValidRequest() {
        return """
                {
                    "title": "Apartamento moderno en El Poblado",
                    "description": "Amplio apartamento de 2 habitaciones con vista a la ciudad.",
                    "squareMeters": 75.5,
                    "price": 1500000,
                    "businessType": "RENT",
                    "propertyType": "APARTMENT",
                    "department": "Antioquia",
                    "country": "Colombia",
                    "city": "Medellín",
                    "fullAddress": "Calle 10 # 43E-50, El Poblado",
                    "fotos": [
                        {"url": "https://cdn.arriendofacil.co/fotos/sala.jpg", "order": 1}
                    ]
                }
                """;
    }

    private static String buildRequestWithField(String field, String jsonValue) {
        try {
            ObjectNode node = TEST_MAPPER.createObjectNode();
            node.put("title", "Apartamento moderno en El Poblado");
            node.put("description", "Amplio apartamento de 2 habitaciones con vista a la ciudad.");
            node.put("squareMeters", 75.5);
            node.put("price", 1500000);
            node.put("businessType", "RENT");
            node.put("propertyType", "APARTMENT");
            node.put("department", "Antioquia");
            node.put("country", "Colombia");
            node.put("city", "Medellín");
            node.put("fullAddress", "Calle 10 # 43E-50, El Poblado");
            node.set("fotos", TEST_MAPPER.readTree(
                    "[{\"url\": \"https://cdn.arriendofacil.co/fotos/sala.jpg\", \"order\": 1}]"));
            node.set(field, TEST_MAPPER.readTree(jsonValue));
            return TEST_MAPPER.writeValueAsString(node);
        } catch (JsonProcessingException e) {
            throw new RuntimeException(e);
        }
    }

    private static InmuebleConFotos buildInmuebleConFotos() {
        var now = LocalDateTime.of(2026, 3, 25, 10, 0);
        var inmueble = Inmueble.builder()
                .id("test-id")
                .userId("usr_01HX9Z")
                .title("Apartamento moderno en El Poblado")
                .description("Amplio apartamento de 2 habitaciones con vista a la ciudad.")
                .squareMeters(new BigDecimal("75.5"))
                .price(new BigDecimal("1500000"))
                .businessType(BusinessType.RENT)
                .propertyType(PropertyType.APARTMENT)
                .status(InmuebleStatus.ACTIVE)
                .department("Antioquia")
                .country("Colombia")
                .city("Medellín")
                .fullAddress("Calle 10 # 43E-50, El Poblado")
                .publishedAt(now)
                .expiresAt(now.plusDays(30))
                .createdAt(now)
                .build();

        var foto = Foto.builder()
                .id("foto-id")
                .propertyId("test-id")
                .url("https://cdn.arriendofacil.co/fotos/sala.jpg")
                .order(1)
                .createdAt(now)
                .build();

        return new InmuebleConFotos(inmueble, List.of(foto));
    }
}
