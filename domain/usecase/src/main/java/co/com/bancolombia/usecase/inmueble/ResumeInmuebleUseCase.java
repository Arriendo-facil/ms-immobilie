package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.events.InmuebleCreatedEvent;
import co.com.bancolombia.model.events.InmueblePublicData;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import co.com.bancolombia.model.user.gateways.UserClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@RequiredArgsConstructor
public class ResumeInmuebleUseCase {
    private final InmuebleRepository inmuebleRepository;
    private final UserClient userClient;
    private final EventsGateway eventsGateway;
    private final FotoRepository fotoRepository;

    public Mono<Void> execute(String inmuebleId, String userId) {
        return userClient.findById(userId)
                .switchIfEmpty(
                        Mono.error(new NotFoundException("NOT_FOUND", "No se pudo obtener la informacion del usuario"))
                )
                .flatMap(user -> inmuebleRepository.findById(inmuebleId)
                        .switchIfEmpty(Mono.error(new NotFoundException("NOT_FOUND", "No hay informacion del inmueble")))
                        .map(inmueble -> inmueble.requireOwner(userId))
                        .map(inmueble -> inmueble.requireStatus(InmuebleStatus.PAUSED))
                        .map(Inmueble::resume)
                        .flatMap(inmuebleRepository::save)
                        .flatMap(saved -> fotoRepository.findByPropertyId(saved.getId()).collectList()
                                .flatMap(fotos -> eventsGateway.emit(
                                                InmuebleCreatedEvent.builder()
                                                        .user(user)
                                                        .inmueble(InmueblePublicData.from(saved, fotos))
                                                        .build()
                                        )
                                )
                        )
                )
                .then();

    }
}
