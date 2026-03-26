package co.com.bancolombia.r2dbc.inmueble;

import co.com.bancolombia.model.inmueble.BusinessType;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.PropertyType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.dao.TransientDataAccessException;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class InmuebleRepositoryAdapterTest {

    @Mock
    private InmuebleR2dbcRepository r2dbcRepository;

    @Mock
    private InmuebleMapper mapper;

    private InmuebleRepositoryAdapter adapter;

    private Inmueble inmuebleDomain;
    private InmuebleEntity inmuebleEntity;

    @BeforeEach
    void setUp() {
        adapter = new InmuebleRepositoryAdapter(r2dbcRepository, mapper);

        LocalDateTime now = LocalDateTime.of(2026, 3, 25, 10, 0);

        inmuebleDomain = Inmueble.builder()
                .id("prop-123")
                .userId("user-abc")
                .title("Apartamento en el centro")
                .description("Bonito apartamento")
                .squareMeters(new BigDecimal("75.00"))
                .price(new BigDecimal("1500000"))
                .businessType(BusinessType.RENT)
                .propertyType(PropertyType.APARTMENT)
                .status(InmuebleStatus.ACTIVE)
                .department("Antioquia")
                .country("Colombia")
                .city("Medellín")
                .fullAddress("Calle 50 # 40-10")
                .publishedAt(now)
                .expiresAt(now.plusDays(30))
                .createdAt(now)
                .updatedAt(now)
                .build();

        inmuebleEntity = InmuebleEntity.builder()
                .id("prop-123")
                .version(0L)
                .userId("user-abc")
                .title("Apartamento en el centro")
                .description("Bonito apartamento")
                .squareMeters(new BigDecimal("75.00"))
                .price(new BigDecimal("1500000"))
                .businessType("RENT")
                .propertyType("APARTMENT")
                .status("ACTIVE")
                .department("Antioquia")
                .country("Colombia")
                .city("Medellín")
                .fullAddress("Calle 50 # 40-10")
                .publishedAt(now)
                .expiresAt(now.plusDays(30))
                .createdAt(now)
                .updatedAt(now)
                .build();

        when(mapper.toEntity(inmuebleDomain)).thenReturn(inmuebleEntity);
        when(mapper.toDomain(inmuebleEntity)).thenReturn(inmuebleDomain);
        when(r2dbcRepository.save(inmuebleEntity)).thenReturn(Mono.just(inmuebleEntity));
        when(r2dbcRepository.countByUserIdAndStatusIn(anyString(), anyList())).thenReturn(Mono.just(3L));
    }

    // -------------------------------------------------------------------------
    // save()
    // -------------------------------------------------------------------------

    @Test
    void save_happyPath_returnsInmueble() {
        StepVerifier.create(adapter.save(inmuebleDomain))
                .assertNext(result -> {
                    assertThat(result.getId()).isEqualTo("prop-123");
                    assertThat(result.getUserId()).isEqualTo("user-abc");
                    assertThat(result.getBusinessType()).isEqualTo(BusinessType.RENT);
                })
                .verifyComplete();

        verify(mapper).toEntity(inmuebleDomain);
        verify(r2dbcRepository).save(inmuebleEntity);
        verify(mapper).toDomain(inmuebleEntity);
    }

    @Test
    void save_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(r2dbcRepository.save(inmuebleEntity)).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) {
                return Mono.error(transientEx);
            }
            return Mono.just(inmuebleEntity);
        });

        StepVerifier.withVirtualTime(() -> adapter.save(inmuebleDomain))
                .thenAwait(Duration.ofSeconds(2))
                .assertNext(result -> assertThat(result.getId()).isEqualTo("prop-123"))
                .verifyComplete();

        verify(r2dbcRepository, times(3)).save(inmuebleEntity);
    }

    @Test
    void save_doesNotRetryOnNonTransientException() {
        when(r2dbcRepository.save(inmuebleEntity))
                .thenReturn(Mono.error(new RuntimeException("Constraint violation")));

        StepVerifier.create(adapter.save(inmuebleDomain))
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository, times(1)).save(inmuebleEntity);
    }

    @Test
    void save_propagatesErrorAfterRetryExhausted() {
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};
        when(r2dbcRepository.save(inmuebleEntity)).thenReturn(Mono.error(transientEx));

        StepVerifier.withVirtualTime(() -> adapter.save(inmuebleDomain))
                .thenAwait(Duration.ofSeconds(2))
                .expectError()
                .verify();

        // 1 intento inicial + 2 reintentos = 3 llamadas totales
        verify(r2dbcRepository, times(3)).save(inmuebleEntity);
    }

    // -------------------------------------------------------------------------
    // countActiveByUserId()
    // -------------------------------------------------------------------------

    @Test
    void countActiveByUserId_passesCorrectStatuses() {
        @SuppressWarnings("unchecked")
        ArgumentCaptor<List<String>> statusCaptor = ArgumentCaptor.forClass(List.class);

        StepVerifier.create(adapter.countActiveByUserId("user-abc"))
                .expectNext(3L)
                .verifyComplete();

        verify(r2dbcRepository).countByUserIdAndStatusIn(eq("user-abc"), statusCaptor.capture());
        assertThat(statusCaptor.getValue())
                .containsExactlyInAnyOrder("ACTIVE", "INACTIVE", "PAUSED");
    }

    @Test
    void countActiveByUserId_returnsCount() {
        when(r2dbcRepository.countByUserIdAndStatusIn(eq("user-abc"), anyList()))
                .thenReturn(Mono.just(7L));

        StepVerifier.create(adapter.countActiveByUserId("user-abc"))
                .expectNext(7L)
                .verifyComplete();
    }

    @Test
    void countActiveByUserId_retriesOnTransientException_succeedsOnThirdAttempt() {
        AtomicInteger attempts = new AtomicInteger(0);
        TransientDataAccessException transientEx = new TransientDataAccessException("DB timeout") {};

        when(r2dbcRepository.countByUserIdAndStatusIn(anyString(), anyList())).thenAnswer(inv -> {
            if (attempts.incrementAndGet() <= 2) {
                return Mono.error(transientEx);
            }
            return Mono.just(5L);
        });

        StepVerifier.withVirtualTime(() -> adapter.countActiveByUserId("user-abc"))
                .thenAwait(Duration.ofSeconds(2))
                .expectNext(5L)
                .verifyComplete();

        verify(r2dbcRepository, times(3)).countByUserIdAndStatusIn(anyString(), anyList());
    }

    @Test
    void countActiveByUserId_doesNotRetryOnNonTransientException() {
        when(r2dbcRepository.countByUserIdAndStatusIn(anyString(), anyList()))
                .thenReturn(Mono.error(new RuntimeException("Query error")));

        StepVerifier.create(adapter.countActiveByUserId("user-abc"))
                .expectError(RuntimeException.class)
                .verify();

        verify(r2dbcRepository, times(1)).countByUserIdAndStatusIn(anyString(), anyList());
    }
}
