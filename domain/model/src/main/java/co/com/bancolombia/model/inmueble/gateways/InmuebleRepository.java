package co.com.bancolombia.model.inmueble.gateways;

import co.com.bancolombia.model.inmueble.Inmueble;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface InmuebleRepository {
    Mono<Inmueble> save(Inmueble inmueble);
    Mono<Long> countCurrentByUserId(String userId);
    Flux<Inmueble> findAllByUserId(String userId);
    Mono<Inmueble> findById(String id);
}
