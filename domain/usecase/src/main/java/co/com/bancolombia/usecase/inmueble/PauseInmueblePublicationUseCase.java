package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.events.DeleteInmueble;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class PauseInmueblePublicationUseCase {
    private final InmuebleRepository inmuebleRepository;
    private  final EventsGateway eventsGateway;

    public Mono<Void> execute(String inmuebleId, String userId) {
        return inmuebleRepository.findById(inmuebleId)
                .switchIfEmpty(Mono.error(new NotFoundException("NOT_FOUND", "No se encontro el inmueble")))
                .map(inmueble -> inmueble.requireOwner(userId))
                .map(inmueble -> inmueble.requireStatus(InmuebleStatus.ACTIVE))
                .map(Inmueble::pause)
                .flatMap(inmuebleRepository::save)
                .flatMap(saved -> eventsGateway.emit(
                        DeleteInmueble
                                .builder()
                                .inmuebleId(saved.getId())
                                .build())
                )
                .then();
    }
}
