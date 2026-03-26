package co.com.bancolombia.r2dbc.inmueble;

import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class InmuebleRepositoryAdapter implements InmuebleRepository {

    private final InmuebleR2dbcRepository r2dbcRepository;
    private final InmuebleMapper mapper;

    @Override
    public Mono<Inmueble> save(Inmueble inmueble) {
        return r2dbcRepository.save(mapper.toEntity(inmueble))
                .map(mapper::toDomain)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(200))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[InmuebleRepository] Reintento #{} en save() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }

    @Override
    public Mono<Long> countActiveByUserId(String userId) {
        return r2dbcRepository.countByUserIdAndStatusIn(userId,
                        List.of(InmuebleStatus.ACTIVE.name(), InmuebleStatus.INACTIVE.name(), InmuebleStatus.PAUSED.name()))
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(150))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[InmuebleRepository] Reintento #{} en countActiveByUserId() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }
}
