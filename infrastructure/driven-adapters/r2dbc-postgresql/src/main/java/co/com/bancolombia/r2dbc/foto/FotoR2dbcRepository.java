package co.com.bancolombia.r2dbc.foto;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface FotoR2dbcRepository extends ReactiveCrudRepository<FotoEntity, String> {

    Flux<FotoEntity> findByPropertyId(String propertyId);
    Mono<Void> deleteAllByPropertyId(String propertyId);
}
