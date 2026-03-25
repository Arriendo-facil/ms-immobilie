package co.com.bancolombia.r2dbc.inmueble;

import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;

import java.util.List;

@Repository
@RequiredArgsConstructor
public class InmuebleRepositoryAdapter implements InmuebleRepository {

    private final InmuebleR2dbcRepository r2dbcRepository;
    private final InmuebleMapper mapper;

    @Override
    public Mono<Inmueble> save(Inmueble inmueble) {
        return r2dbcRepository.save(mapper.toEntity(inmueble))
                .map(mapper::toDomain);
    }

    @Override
    public Mono<Long> countActiveByUserId(String userId) {
        return r2dbcRepository.countByUserIdAndStatusIn(userId,
                List.of(InmuebleStatus.ACTIVE.name(), InmuebleStatus.INACTIVE.name(), InmuebleStatus.PAUSED.name()));
    }
}
