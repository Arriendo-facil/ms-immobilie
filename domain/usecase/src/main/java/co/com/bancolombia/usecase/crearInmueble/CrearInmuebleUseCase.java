package co.com.bancolombia.usecase.crearInmueble;

import co.com.bancolombia.model.events.FotoPublicData;
import co.com.bancolombia.model.events.InmuebleCreatedEvent;
import co.com.bancolombia.model.events.InmueblePublicData;
import co.com.bancolombia.model.events.gateways.EventsGateway;
import co.com.bancolombia.model.exception.ForbiddenException;
import co.com.bancolombia.model.foto.Foto;
import co.com.bancolombia.model.foto.gateways.FotoRepository;
import co.com.bancolombia.model.inmueble.Inmueble;
import co.com.bancolombia.model.inmueble.InmuebleConFotos;
import co.com.bancolombia.model.inmueble.InmuebleStatus;
import co.com.bancolombia.model.inmueble.gateways.InmuebleRepository;
import co.com.bancolombia.model.user.gateways.UserClient;
import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@RequiredArgsConstructor
public class CrearInmuebleUseCase {

    private static final long FREE_PLAN_MAX_PROPERTIES = 2L;

    private final InmuebleRepository inmuebleRepository;
    private final FotoRepository fotoRepository;
    private final UserClient userClient;
    private final EventsGateway eventsGateway;

    public Mono<InmuebleConFotos> execute(Inmueble inmueble, List<Foto> photos) {
        return checkPlanLimit(inmueble.getUserId())
                .then(buildAndSaveInmueble(inmueble))
                .flatMap(saved -> savePhotos(photos, saved)
                        .flatMap(savedPhotos -> publishEventAndReturn(saved, savedPhotos)));
    }

    private Mono<Void> checkPlanLimit(String userId) {
        return inmuebleRepository.countActiveByUserId(userId)
                .flatMap(count -> count >= FREE_PLAN_MAX_PROPERTIES
                        ? Mono.error(new ForbiddenException(
                                "PLAN_LIMIT_EXCEEDED",
                                "Plan gratuito, maximo 2 propiedades activas permitidas"))
                        : Mono.empty());
    }

    private Mono<Inmueble> buildAndSaveInmueble(Inmueble inmueble) {
        LocalDateTime publishedAt = LocalDateTime.now();
        return inmuebleRepository.save(inmueble.toBuilder()
                .id(UUID.randomUUID().toString())
                .status(InmuebleStatus.ACTIVE)
                .publishedAt(publishedAt)
                .expiresAt(publishedAt.plusDays(30))
                .build());
    }

    private Mono<List<Foto>> savePhotos(List<Foto> photos, Inmueble inmueble) {
        List<Foto> photosWithId = photos.stream()
                .map(photo -> photo.toBuilder()
                        .id(UUID.randomUUID().toString())
                        .propertyId(inmueble.getId())
                        .build())
                .toList();
        return fotoRepository.saveAll(photosWithId).collectList();
    }

    private Mono<InmuebleConFotos> publishEventAndReturn(Inmueble inmueble, List<Foto> photos) {
        return userClient.findById(inmueble.getUserId())
                .flatMap(userData -> {
                    InmuebleCreatedEvent event = InmuebleCreatedEvent.builder()
                            .user(userData)
                            .inmueble(buildPublicData(inmueble, photos))
                            .build();
                    return eventsGateway.emit(event)
                            .thenReturn(new InmuebleConFotos(inmueble, photos));
                });
    }

    private InmueblePublicData buildPublicData(Inmueble inmueble, List<Foto> photos) {
        return InmueblePublicData.builder()
                .id(inmueble.getId())
                .userId(inmueble.getUserId())
                .title(inmueble.getTitle())
                .description(inmueble.getDescription())
                .squareMeters(inmueble.getSquareMeters())
                .price(inmueble.getPrice())
                .businessType(inmueble.getBusinessType())
                .propertyType(inmueble.getPropertyType())
                .department(inmueble.getDepartment())
                .country(inmueble.getCountry())
                .city(inmueble.getCity())
                .photos(photos.stream()
                        .map(photo -> FotoPublicData.builder()
                                .id(photo.getId())
                                .url(photo.getUrl())
                                .order(photo.getOrder())
                                .build())
                        .toList())
                .build();
    }
}
