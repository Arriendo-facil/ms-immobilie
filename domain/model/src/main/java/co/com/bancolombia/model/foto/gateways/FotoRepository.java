package co.com.bancolombia.model.foto.gateways;

import co.com.bancolombia.model.foto.Foto;
import reactor.core.publisher.Flux;

import java.util.List;

public interface FotoRepository {
    Flux<Foto> saveAll(List<Foto> fotos);
    Flux<Foto> findByPropertyId(String propertyId);
}
