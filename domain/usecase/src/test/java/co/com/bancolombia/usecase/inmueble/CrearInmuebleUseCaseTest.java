package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.events.InmuebleCreatedEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import co.com.bancolombia.model.user.gateways.UserClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CrearInmuebleUseCaseTest {

    private static final String USER_ID = "user-123";
    private static final String SAVED_INMUEBLE_ID_PLACEHOLDER = "any-uuid";

    @Mock
    private InmuebleRepository inmuebleRepository;

    @Mock
    private FotoRepository fotoRepository;

    @Mock
    private UserClient userClient;

    @Mock
    private EventsGateway eventsGateway;

    private CrearInmuebleUseCase useCase;

    private Inmueble inmuebleBase;
    private List<Foto> fotosBase;
    private Map<String, Object> userDataBase;

    @BeforeEach
    void setUp() {
        useCase = new CrearInmuebleUseCase(inmuebleRepository, fotoRepository, userClient, eventsGateway);

        inmuebleBase = Inmueble.builder()
                .userId(USER_ID)
                .title("Apartamento en Laureles")
                .description("Amplio apartamento con vista")
                .squareMeters(new BigDecimal("85.00"))
                .price(new BigDecimal("1500000"))
                .businessType(BusinessType.RENT)
                .propertyType(PropertyType.APARTMENT)
                .department("Antioquia")
                .country("Colombia")
                .city("Medellín")
                .fullAddress("Calle 33 # 75-20")
                .build();

        fotosBase = List.of(
                Foto.builder().url("https://cdn.example.com/foto1.jpg").order(1).build(),
                Foto.builder().url("https://cdn.example.com/foto2.jpg").order(2).build()
        );

        userDataBase = Map.of("id", USER_ID, "name", "Juan Pérez", "email", "juan@example.com");

        // Stubs del happy path por defecto
        when(inmuebleRepository.countCurrentByUserId(USER_ID)).thenReturn(Mono.just(0L));
        when(inmuebleRepository.save(any(Inmueble.class))).thenAnswer(invocation -> {
            Inmueble arg = invocation.getArgument(0);
            return Mono.just(arg);
        });
        when(fotoRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<Foto> arg = invocation.getArgument(0);
            return Flux.fromIterable(arg);
        });
        when(userClient.findById(USER_ID)).thenReturn(Mono.just(userDataBase));
        when(eventsGateway.emit(any())).thenReturn(Mono.empty());
    }

    // -------------------------------------------------------------------------
    // Flujo feliz
    // -------------------------------------------------------------------------

    @Test
    void execute_whenCountBelowLimit_returnsInmuebleConFotos() {
        when(inmuebleRepository.countCurrentByUserId(USER_ID)).thenReturn(Mono.just(0L));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    assertThat(result).isNotNull();
                    assertThat(result.inmueble()).isNotNull();
                    assertThat(result.photos()).hasSize(2);
                    assertThat(result.inmueble().getUserId()).isEqualTo(USER_ID);
                })
                .verifyComplete();
    }

    @Test
    void execute_whenCountAtLimit_minusOne_returnsInmuebleConFotos() {
        when(inmuebleRepository.countCurrentByUserId(USER_ID)).thenReturn(Mono.just(1L));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    assertThat(result).isInstanceOf(InmuebleConFotos.class);
                    assertThat(result.inmueble()).isNotNull();
                    assertThat(result.photos()).hasSize(2);
                })
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Enriquecimiento del inmueble al guardarlo
    // -------------------------------------------------------------------------

    @Test
    void execute_assignsUuidToSavedInmueble() {
        ArgumentCaptor<Inmueble> captor = ArgumentCaptor.forClass(Inmueble.class);
        when(inmuebleRepository.save(captor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    Inmueble captured = captor.getValue();
                    assertThat(captured.getId()).isNotNull().isNotBlank();
                    // Valida que sea un UUID v4 con formato estándar
                    assertThat(captured.getId())
                            .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
                })
                .verifyComplete();
    }

    @Test
    void execute_setsStatusActiveOnSavedInmueble() {
        ArgumentCaptor<Inmueble> captor = ArgumentCaptor.forClass(Inmueble.class);
        when(inmuebleRepository.save(captor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result ->
                        assertThat(captor.getValue().getStatus()).isEqualTo(InmuebleStatus.ACTIVE))
                .verifyComplete();
    }

    @Test
    void execute_setsPublishedAtAndExpiresAtOnSavedInmueble() {
        ArgumentCaptor<Inmueble> captor = ArgumentCaptor.forClass(Inmueble.class);
        when(inmuebleRepository.save(captor.capture())).thenAnswer(inv -> Mono.just(inv.getArgument(0)));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    Inmueble captured = captor.getValue();
                    assertThat(captured.getPublishedAt()).isNotNull();
                    assertThat(captured.getExpiresAt()).isNotNull();
                    assertThat(captured.getExpiresAt())
                            .isEqualTo(captured.getPublishedAt().plusDays(30));
                })
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Enriquecimiento de fotos al guardarlas
    // -------------------------------------------------------------------------

    @Test
    void execute_assignsUuidAndPropertyIdToEachPhoto() {
        ArgumentCaptor<List<Foto>> captor = ArgumentCaptor.forClass(List.class);
        when(fotoRepository.saveAll(captor.capture())).thenAnswer(inv -> {
            List<Foto> arg = inv.getArgument(0);
            return Flux.fromIterable(arg);
        });

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    List<Foto> capturedPhotos = captor.getValue();
                    assertThat(capturedPhotos).hasSize(2);

                    String propertyId = result.inmueble().getId();
                    capturedPhotos.forEach(foto -> {
                        assertThat(foto.getId())
                                .isNotNull()
                                .isNotBlank()
                                .matches("[0-9a-f]{8}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{4}-[0-9a-f]{12}");
                        assertThat(foto.getPropertyId()).isEqualTo(propertyId);
                    });
                })
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Evento de dominio
    // -------------------------------------------------------------------------

    @Test
    void execute_emitsInmuebleCreatedEventWithCorrectData() {
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        when(eventsGateway.emit(eventCaptor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    Object emitted = eventCaptor.getValue();
                    assertThat(emitted).isInstanceOf(InmuebleCreatedEvent.class);

                    InmuebleCreatedEvent event = (InmuebleCreatedEvent) emitted;
                    assertThat(event.getUser()).isEqualTo(userDataBase);
                    assertThat(event.getInmueble().getId()).isEqualTo(result.inmueble().getId());
                    assertThat(event.getInmueble().getUserId()).isEqualTo(USER_ID);
                })
                .verifyComplete();
    }

    @Test
    void execute_emitsEventWithAllPhotosInPublicData() {
        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        when(eventsGateway.emit(eventCaptor.capture())).thenReturn(Mono.empty());

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .assertNext(result -> {
                    InmuebleCreatedEvent event = (InmuebleCreatedEvent) eventCaptor.getValue();
                    assertThat(event.getInmueble().getPhotos()).hasSize(2);

                    // Verifica que las URLs y el orden se preservan correctamente
                    assertThat(event.getInmueble().getPhotos())
                            .extracting("url")
                            .containsExactlyInAnyOrder(
                                    "https://cdn.example.com/foto1.jpg",
                                    "https://cdn.example.com/foto2.jpg"
                            );
                    assertThat(event.getInmueble().getPhotos())
                            .extracting("order")
                            .containsExactlyInAnyOrder(1, 2);

                    // Verifica que cada foto del evento tiene id no-null (fue enriquecida)
                    event.getInmueble().getPhotos().forEach(fotoPublicData ->
                            assertThat(fotoPublicData.getId()).isNotNull().isNotBlank()
                    );
                })
                .verifyComplete();
    }

    // -------------------------------------------------------------------------
    // Límite de plan gratuito
    // -------------------------------------------------------------------------

    @Test
    void execute_whenCountEqualsLimit_throwsForbiddenException() {
        when(inmuebleRepository.countCurrentByUserId(USER_ID)).thenReturn(Mono.just(2L));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ForbiddenException.class);
                    assertThat(((ForbiddenException) error).getErrorCode())
                            .isEqualTo("PLAN_LIMIT_EXCEEDED");
                })
                .verify();
    }

    @Test
    void execute_whenCountExceedsLimit_throwsForbiddenException() {
        when(inmuebleRepository.countCurrentByUserId(USER_ID)).thenReturn(Mono.just(5L));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ForbiddenException.class);
                    assertThat(((ForbiddenException) error).getErrorCode())
                            .isEqualTo("PLAN_LIMIT_EXCEEDED");
                    assertThat(error.getMessage())
                            .isEqualTo("Plan gratuito, maximo 2 propiedades vigentes permitidas");
                })
                .verify();
    }

    // -------------------------------------------------------------------------
    // Propagación de errores de infraestructura
    // -------------------------------------------------------------------------

    @Test
    void execute_whenRepositorySaveFails_propagatesError() {
        RuntimeException dbError = new RuntimeException("DB error");
        when(inmuebleRepository.save(any(Inmueble.class))).thenReturn(Mono.error(dbError));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).isEqualTo("DB error");
                })
                .verify();
    }

    @Test
    void execute_whenUserClientFails_propagatesError() {
        RuntimeException clientError = new RuntimeException("User service unavailable");
        when(userClient.findById(anyString())).thenReturn(Mono.error(clientError));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).isEqualTo("User service unavailable");
                })
                .verify();
    }

    @Test
    void execute_whenPlanLimitExceeded_doesNotCallSave() {
        when(inmuebleRepository.countCurrentByUserId(USER_ID)).thenReturn(Mono.just(2L));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .expectError(ForbiddenException.class)
                .verify();

        verify(inmuebleRepository, never()).save(any(Inmueble.class));
    }

    @Test
    void execute_whenUserClientFails_doesNotPersistAnything() {
        when(userClient.findById(USER_ID)).thenReturn(Mono.error(new RuntimeException("ms-user unavailable")));

        StepVerifier.create(useCase.execute(inmuebleBase, fotosBase))
                .expectError(RuntimeException.class)
                .verify();

        verify(inmuebleRepository, never()).save(any(Inmueble.class));
        verify(fotoRepository, never()).saveAll(anyList());
    }
}
