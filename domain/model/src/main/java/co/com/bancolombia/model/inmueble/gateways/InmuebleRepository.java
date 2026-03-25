package co.com.bancolombia.model.inmueble.gateways;

import co.com.bancolombia.model.inmueble.Inmueble;
import reactor.core.publisher.Mono;

public interface InmuebleRepository {
    Mono<Inmueble> save(Inmueble inmueble);
    Mono<Long> countActiveByUserId(String userId);
}
