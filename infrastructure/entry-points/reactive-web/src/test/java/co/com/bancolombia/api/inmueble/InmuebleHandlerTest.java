package co.com.bancolombia.api.inmueble;

import co.com.bancolombia.api.config.GlobalErrorHandler;
import co.com.bancolombia.api.config.UserIdExtractorFilter;
import co.com.bancolombia.api.mapper.InmuebleApiMapperImpl;
import co.com.bancolombia.model.exception.ExternalServiceException;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import co.com.bancolombia.usecase.crearInmueble.CrearInmuebleUseCase;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Mono;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@WebFluxTest
@Import({InmuebleRouter.class, InmuebleHandler.class, InmuebleApiMapperImpl.class,
        GlobalErrorHandler.class, UserIdExtractorFilter.class})
class InmuebleHandlerTest {

    private static final String URL = "/api/v1/inmuebles";
    private static final ObjectMapper TEST_MAPPER = new ObjectMapper();

    @Autowired
    private WebTestClient webTestClient;

    @MockitoBean
    private CrearInmuebleUseCase crearInmuebleUseCase;

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
