package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.events.DeleteInmuebleEvent;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class DeleteInmuebleUseCase {
    private final InmuebleRepository inmuebleRepository;
    private final FotoRepository fotoRepository;
    private final EventsGateway eventsGateway;

    public Mono<Void> execute(String inmuebleId, String userId) {
        return inmuebleRepository.findById(inmuebleId)
                .map(inmueble -> inmueble.requireOwner(userId))
                .flatMap(inmueble -> Mono.when(
                        inmuebleRepository.deleteInmuebleById(inmueble.getId()),
                        fotoRepository.deleteAllByInmuebleId(inmueble.getId())
                ))
                .then(eventsGateway.emit(
                        DeleteInmuebleEvent.builder()
                                .inmuebleId(inmuebleId)
                                .build()
                        )
                );
    }
}
