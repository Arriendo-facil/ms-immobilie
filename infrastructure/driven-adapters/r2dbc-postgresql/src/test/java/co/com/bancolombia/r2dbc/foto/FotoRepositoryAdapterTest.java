package co.com.bancolombia.r2dbc.foto;

import co.com.bancolombia.model.foto.Foto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.TransientDataAccessException;
import reactor.core.publisher.Flux;
import reactor.test.StepVerifier;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FotoRepositoryAdapterTest {

    @Mock
    private FotoR2dbcRepository r2dbcRepository;

    @Mock
    private FotoMapper mapper;

    private FotoRepositoryAdapter adapter;

    private Foto foto1Domain;
    private Foto foto2Domain;
    private FotoEntity foto1Entity;
    private FotoEntity foto2Entity;

    @BeforeEach
    void setUp() {
        adapter = new FotoRepositoryAdapter(r2dbcRepository, mapper);

        LocalDateTime now = LocalDateTime.of(2026, 3, 25, 10, 0);

        foto1Domain = Foto.builder()
                .id("foto-001").propertyId("prop-123")
                .url("https://cdn.example.com/foto1.jpg").order(1).createdAt(now).build();

        foto2Domain = Foto.builder()
                .id("foto-002").propertyId("prop-123")
                .url("https://cdn.example.com/foto2.jpg").order(2).createdAt(now).build();

        foto1Entity = FotoEntity.builder()
                .id("foto-001").version(0L).propertyId("prop-123")
                .url("https://cdn.example.com/foto1.jpg").order(1).createdAt(now).build();

        foto2Entity = FotoEntity.builder()
                .id("foto-002").version(0L).propertyId("prop-123")
                .url("https://cdn.example.com/foto2.jpg").order(2).createdAt(now).build();

        when(mapper.toEntity(foto1Domain)).thenReturn(foto1Entity);
        when(mapper.toEntity(foto2Domain)).thenReturn(foto2Entity);
        when(mapper.toDomain(foto1Entity)).thenReturn(foto1Domain);
        when(mapper.toDomain(foto2Entity)).thenReturn(foto2Domain);
        when(r2dbcRepository.saveAll(any(Iterable.class))).thenReturn(Flux.just(foto1Entity, foto2Entity));
        when(r2dbcRepository.findByPropertyId("prop-123")).thenReturn(Flux.just(foto1Entity, foto2Entity));
    }

    // -------------------------------------------------------------------------
    // saveAll()
    // -------------------------------------------------------------------------

    @Test
    void saveAll_mapsEachFotoAndDelegates() {
        StepVerifier.create(adapter.saveAll(List.of(foto1Domain, foto2Domain)))
                .assertNext(f -> assertThat(f.getId()).isEqualTo("foto-001"))
                .assertNext(f -> assertThat(f.getId()).isEqualTo("foto-002"))
                .verifyComplete();

        verify(mapper).toEntity(foto1Domain);
        verify(mapper).toEntity(foto2Domain);
        verify(r2dbcRepository).saveAll(any(Iterable.class));
        verify(mapper).toDomain(foto1Entity);
        verify(mapper).toDomain(foto2Entity);
    }

    @Test
    void saveAll_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(r2dbcRepository.saveAll(any(Iterable.class))).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) {
                return Flux.error(transientEx);
            }
            return Flux.just(foto1Entity, foto2Entity);
        });

        StepVerifier.withVirtualTime(() -> adapter.saveAll(List.of(foto1Domain, foto2Domain)))
                .thenAwait(Duration.ofSeconds(2))
                .assertNext(f -> assertThat(f.getId()).isEqualTo("foto-001"))
                .assertNext(f -> assertThat(f.getId()).isEqualTo("foto-002"))
                .verifyComplete();

        verify(r2dbcRepository, times(3)).saveAll(any(Iterable.class));
    }

    @Test
    void saveAll_doesNotRetryOnNonTransientException() {
        when(r2dbcRepository.saveAll(any(Iterable.class)))
                .thenReturn(Flux.error(new RuntimeException("Constraint violation")));

        StepVerifier.create(adapter.saveAll(List.of(foto1Domain, foto2Domain)))
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository, times(1)).saveAll(any(Iterable.class));
    }

    // -------------------------------------------------------------------------
    // findByPropertyId()
    // -------------------------------------------------------------------------

    @Test
    void findByPropertyId_returnsMatchingFotos() {
        StepVerifier.create(adapter.findByPropertyId("prop-123"))
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo("foto-001");
                    assertThat(f.getPropertyId()).isEqualTo("prop-123");
                    assertThat(f.getOrder()).isEqualTo(1);
                })
                .assertNext(f -> {
                    assertThat(f.getId()).isEqualTo("foto-002");
                    assertThat(f.getOrder()).isEqualTo(2);
                })
                .verifyComplete();

        verify(r2dbcRepository).findByPropertyId("prop-123");
    }

    @Test
    void findByPropertyId_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(r2dbcRepository.findByPropertyId(anyString())).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) {
                return Flux.error(transientEx);
            }
            return Flux.just(foto1Entity, foto2Entity);
        });

        StepVerifier.withVirtualTime(() -> adapter.findByPropertyId("prop-123"))
                .thenAwait(Duration.ofSeconds(2))
                .assertNext(f -> assertThat(f.getId()).isEqualTo("foto-001"))
                .assertNext(f -> assertThat(f.getId()).isEqualTo("foto-002"))
                .verifyComplete();

        verify(r2dbcRepository, times(3)).findByPropertyId("prop-123");
    }

    @Test
    void findByPropertyId_doesNotRetryOnNonTransientException() {
        when(r2dbcRepository.findByPropertyId(anyString()))
                .thenReturn(Flux.error(new RuntimeException("Query error")));

        StepVerifier.create(adapter.findByPropertyId("prop-123"))
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository, times(1)).findByPropertyId("prop-123");
    }
}
