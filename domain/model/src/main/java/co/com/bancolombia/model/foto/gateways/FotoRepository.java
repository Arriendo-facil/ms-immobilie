package co.com.bancolombia.model.foto.gateways;

import co.com.bancolombia.model.foto.Foto;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

public interface FotoRepository {
    Flux<Foto> saveAll(List<Foto> fotos);
    Flux<Foto> findByPropertyId(String propertyId);
    Mono<Void> deleteAllByInmuebleId(String inmuebleId);
}
