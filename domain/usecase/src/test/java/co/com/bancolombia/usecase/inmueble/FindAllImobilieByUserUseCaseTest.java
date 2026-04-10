package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class FindAllImobilieByUserUseCaseTest {

    private static final String USER_ID = "user-123";

    @Mock
    private InmuebleRepository inmuebleRepository;

    @Mock
    private FotoRepository fotoRepository;

    private FindAllImobilieByUserUseCase useCase;

    private Inmueble inmueble1;
    private Inmueble inmueble2;

    @BeforeEach
    void setUp() {
        useCase = new FindAllImobilieByUserUseCase(inmuebleRepository, fotoRepository);

        inmueble1 = Inmueble.builder()
                .id("inmueble-1")
                .userId(USER_ID)
                .title("Apartamento en Laureles")
                .description("Amplio apartamento con vista")
                .squareMeters(new BigDecimal("85.00"))
                .price(new BigDecimal("1500000"))
                .businessType(BusinessType.RENT)
                .propertyType(PropertyType.APARTMENT)
                .status(InmuebleStatus.ACTIVE)
                .department("Antioquia")
                .country("Colombia")
                .city("Medellín")
                .fullAddress("Calle 33 # 75-20")
                .publishedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();

        inmueble2 = Inmueble.builder()
                .id("inmueble-2")
                .userId(USER_ID)
                .title("Casa en El Poblado")
                .description("Casa con jardín")
                .squareMeters(new BigDecimal("200.00"))
                .price(new BigDecimal("3000000"))
                .businessType(BusinessType.RENT)
                .propertyType(PropertyType.HOUSE)
                .status(InmuebleStatus.ACTIVE)
                .department("Antioquia")
                .country("Colombia")
                .city("Medellín")
                .fullAddress("Carrera 43A # 1-50")
                .publishedAt(LocalDateTime.now())
                .expiresAt(LocalDateTime.now().plusDays(30))
                .build();
    }

    // -------------------------------------------------------------------------
    // Flujo feliz
    // -------------------------------------------------------------------------

    @Test
    void execute_whenUserHasInmuebles_returnsEachWithPhotos() {
        Foto foto1 = Foto.builder().id("foto-1").propertyId("inmueble-1").url("https://cdn.example.com/foto1.jpg").order(1).build();
        Foto foto2 = Foto.builder().id("foto-2").propertyId("inmueble-2").url("https://cdn.example.com/foto2.jpg").order(1).build();

        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(inmueble1, inmueble2));
        when(fotoRepository.findByPropertyId("inmueble-1")).thenReturn(Flux.just(foto1));
        when(fotoRepository.findByPropertyId("inmueble-2")).thenReturn(Flux.just(foto2));

        StepVerifier.create(useCase.execute(USER_ID))
                .assertNext(result -> {
                    assertThat(result.inmueble().getId()).isEqualTo("inmueble-1");
                    assertThat(result.photos()).hasSize(1);
                    assertThat(result.photos().get(0).getId()).isEqualTo("foto-1");
                })
                .assertNext(result -> {
                    assertThat(result.inmueble().getId()).isEqualTo("inmueble-2");
                    assertThat(result.photos()).hasSize(1);
                    assertThat(result.photos().get(0).getId()).isEqualTo("foto-2");
                })
                .verifyComplete();
    }

    @Test
    void execute_whenUserHasNoInmuebles_returnsEmptyFlux() {
        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(USER_ID))
                .verifyComplete();
    }

    @Test
    void execute_whenInmuebleHasNoPhotos_returnsInmuebleWithEmptyPhotoList() {
        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(inmueble1));
        when(fotoRepository.findByPropertyId("inmueble-1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(USER_ID))
                .assertNext(result -> {
                    assertThat(result.inmueble().getId()).isEqualTo("inmueble-1");
                    assertThat(result.photos()).isEmpty();
                })
                .verifyComplete();
    }

    @Test
    void execute_whenInmuebleHasMultiplePhotos_returnsAllPhotos() {
        List<Foto> fotos = List.of(
                Foto.builder().id("foto-1").propertyId("inmueble-1").url("https://cdn.example.com/foto1.jpg").order(1).build(),
                Foto.builder().id("foto-2").propertyId("inmueble-1").url("https://cdn.example.com/foto2.jpg").order(2).build(),
                Foto.builder().id("foto-3").propertyId("inmueble-1").url("https://cdn.example.com/foto3.jpg").order(3).build()
        );

        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(inmueble1));
        when(fotoRepository.findByPropertyId("inmueble-1")).thenReturn(Flux.fromIterable(fotos));

        StepVerifier.create(useCase.execute(USER_ID))
                .assertNext(result -> {
                    assertThat(result.inmueble().getId()).isEqualTo("inmueble-1");
                    assertThat(result.photos()).hasSize(3);
                    assertThat(result.photos()).extracting("order").containsExactlyInAnyOrder(1, 2, 3);
                })
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Integridad del resultado
    // -------------------------------------------------------------------------

    @Test
    void execute_returnsCorrectInmuebleData() {
        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(inmueble1));
        when(fotoRepository.findByPropertyId("inmueble-1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(USER_ID))
                .assertNext(result -> {
                    Inmueble inmueble = result.inmueble();
                    assertThat(inmueble.getUserId()).isEqualTo(USER_ID);
                    assertThat(inmueble.getTitle()).isEqualTo("Apartamento en Laureles");
                    assertThat(inmueble.getStatus()).isEqualTo(InmuebleStatus.ACTIVE);
                    assertThat(inmueble.getBusinessType()).isEqualTo(BusinessType.RENT);
                    assertThat(inmueble.getPropertyType()).isEqualTo(PropertyType.APARTMENT);
                })
                .verifyComplete();
    }

    @Test
    void execute_returnsInstanceOfInmuebleConFotos() {
        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(inmueble1));
        when(fotoRepository.findByPropertyId("inmueble-1")).thenReturn(Flux.empty());

        StepVerifier.create(useCase.execute(USER_ID))
                .assertNext(result -> assertThat(result).isInstanceOf(InmuebleConFotos.class))
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Propagación de errores de infraestructura
    // -------------------------------------------------------------------------

    @Test
    void execute_whenRepositoryFails_propagatesError() {
        RuntimeException dbError = new RuntimeException("DB connection lost");
        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.error(dbError));

        StepVerifier.create(useCase.execute(USER_ID))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).isEqualTo("DB connection lost");
                })
                .verify();
    }

    @Test
    void execute_whenFotoRepositoryFails_propagatesError() {
        RuntimeException dbError = new RuntimeException("Foto DB error");
        when(inmuebleRepository.findAllByUserId(USER_ID)).thenReturn(Flux.just(inmueble1));
        when(fotoRepository.findByPropertyId("inmueble-1")).thenReturn(Flux.error(dbError));

        StepVerifier.create(useCase.execute(USER_ID))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).isEqualTo("Foto DB error");
                })
                .verify();
    }
}
