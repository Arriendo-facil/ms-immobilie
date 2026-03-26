package co.com.bancolombia.r2dbc.inmueble;

import org.springframework.data.repository.reactive.ReactiveCrudRepository;
import reactor.core.publisher.Mono;

import java.util.List;

public interface InmuebleR2dbcRepository extends ReactiveCrudRepository<InmuebleEntity, String> {

    Mono<Long> countByUserIdAndStatusIn(String userId, List<String> statuses);
}
