package co.com.bancolombia.r2dbc.foto;

import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Flux;

import java.util.List;

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
                .map(mapper::toDomain);
    }
}
