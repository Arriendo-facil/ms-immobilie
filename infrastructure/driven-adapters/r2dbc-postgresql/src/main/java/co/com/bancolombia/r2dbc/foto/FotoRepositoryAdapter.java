package co.com.bancolombia.r2dbc.foto;

import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.TransientDataAccessException;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;
import reactor.util.retry.Retry;

import java.time.Duration;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class FotoRepositoryAdapter implements FotoRepository {

    private final FotoR2dbcRepository r2dbcRepository;
    private final FotoMapper mapper;

    @Override
    public Flux<Foto> saveAll(List<Foto> fotos) {
        return r2dbcRepository.saveAll(fotos.stream().map(mapper::toEntity).toList())
                .map(mapper::toDomain);
    }

    @Override
    public Flux<Foto> findByPropertyId(String propertyId) {
        return r2dbcRepository.findByPropertyId(propertyId)
                .map(mapper::toDomain)
                .retryWhen(Retry.fixedDelay(2, Duration.ofMillis(150))
                        .filter(ex -> ex instanceof TransientDataAccessException)
                        .doBeforeRetry(signal -> log.warn(
                                "[FotoRepository] Reintento #{} en findByPropertyId() — causa: {}",
                                signal.totalRetries() + 1, signal.failure().getMessage())));
    }
}
