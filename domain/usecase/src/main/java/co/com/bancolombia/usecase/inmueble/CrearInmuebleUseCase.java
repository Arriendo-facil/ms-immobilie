package co.com.bancolombia.usecase.inmueble;

import co.com.bancolombia.model.events.InmuebleCreatedEvent;
import co.com.bancolombia.model.events.InmueblePublicData;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import co.com.bancolombia.model.user.gateways.UserClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.util.List;
import java.util.Map;

@RequiredArgsConstructor
public class CrearInmuebleUseCase {

    private static final long FREE_PLAN_MAX_PROPERTIES = 2L;

    private final InmuebleRepository inmuebleRepository;
    private final FotoRepository fotoRepository;
    private final UserClient userClient;
    private final EventsGateway eventsGateway;

    public Mono<InmuebleConFotos> execute(Inmueble inmueble, List<Foto> photos) {
        return userClient.findById(inmueble.getUserId())
                .switchIfEmpty(Mono.error(new NotFoundException("NOT_FOUND", "No se pudo obtener la informacion del usuario")))
                .flatMap(userData -> checkPlanLimit(inmueble.getUserId())
                        .then(Mono.defer(() -> buildAndSaveInmueble(inmueble)))
                        .flatMap(saved -> savePhotos(photos, saved)
                                .flatMap(savedPhotos -> emitEventAndReturn(saved, savedPhotos, userData))));
    }

    private Mono<Void> checkPlanLimit(String userId) {
        return inmuebleRepository.countCurrentByUserId(userId)
                .flatMap(count -> count >= FREE_PLAN_MAX_PROPERTIES
                        ? Mono.error(new ForbiddenException(
                                "PLAN_LIMIT_EXCEEDED",
                                "Plan gratuito, maximo 2 propiedades vigentes permitidas"))
                        : Mono.empty());
    }

    private Mono<Inmueble> buildAndSaveInmueble(Inmueble inmueble) {
        return inmuebleRepository.save(inmueble.publish());
    }

    private Mono<List<Foto>> savePhotos(List<Foto> photos, Inmueble inmueble) {
        return fotoRepository.saveAll(Foto.prepareForSave(photos, inmueble.getId())).collectList();
    }

    private Mono<InmuebleConFotos> emitEventAndReturn(Inmueble inmueble, List<Foto> photos, Map<String, Object> userData) {
        InmuebleCreatedEvent event = InmuebleCreatedEvent.builder()
                .user(userData)
                .inmueble(InmueblePublicData.from(inmueble, photos))
                .build();
        return eventsGateway.emit(event)
                .thenReturn(new InmuebleConFotos(inmueble, photos));
    }
}
