package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.events.InmueblePublicData;
import co.com.bancolombia.model.events.UpdateInmuebleEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.*;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;

@RequiredArgsConstructor
public class UpdateInmuebleUseCase {

    private final InmuebleRepository inmuebleRepository;
    private final FotoRepository fotoRepository;
    private  final EventsGateway eventsGateway;


    public Mono<InmuebleConFotos> execute (Inmueble inmueble, List<Foto> photos) {
        return inmuebleRepository.findById(inmueble.getId())
                .switchIfEmpty(
                        Mono.error(
                                new NotFoundException(
                                        "NOT_FOUND", "El inmueble no esta registrado: " + inmueble.getId()
                                )
                        )
                ).map(inmuebleDb -> inmuebleDb.update(inmueble))
                .flatMap(updated -> fotoRepository.deleteAllByInmuebleId(updated.getId())
                        .then(inmuebleRepository.save(updated))
                        .flatMap(saved -> fotoRepository.saveAll(photos)
                                .collectList()
                                .map(fotos -> new InmuebleConFotos(saved, fotos))
                        )
                )
                .flatMap(this::emitEventAndReturn);

    }

    private Mono<InmuebleConFotos> emitEventAndReturn(InmuebleConFotos result) {
        UpdateInmuebleEvent event = UpdateInmuebleEvent
                .builder()
                .inmueble(InmueblePublicData.from(result.inmueble(), result.photos()))
                .build();

        return eventsGateway.emit(event)
                .thenReturn(result);
    }


}
