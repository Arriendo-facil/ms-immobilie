package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Flux;

@RequiredArgsConstructor
public class FindAllImobilieByUserUseCase {
    private final InmuebleRepository repository;
    private final FotoRepository fotoRepository;

    public Flux<InmuebleConFotos> execute(String userId) {
        return repository.findAllByUserId(userId)
                .flatMap(inmueble -> fotoRepository.findByPropertyId(inmueble.getId())
                        .collectList()
                        .map(fotos -> new InmuebleConFotos(inmueble, fotos)
                        )
                );
    }

}
