package co.com.bancolombia.api.inmueble;

import co.com.bancolombia.api.config.UserIdExtractorFilter;
import co.com.bancolombia.api.dto.inmueble.CreateInmuebleDto;
import co.com.bancolombia.api.mapper.InmuebleApiMapper;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.usecase.inmueble.*;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.server.ServerRequest;
import org.springframework.web.reactive.function.server.ServerResponse;
import reactor.core.publisher.Mono;

import java.util.Set;

@Slf4j
@Component
@RequiredArgsConstructor
public class InmuebleHandler {

    private final CrearInmuebleUseCase crearInmuebleUseCase;
    private final FindAllImobilieByUserUseCase findAllImobilieByUserUseCase;
    private final UpdateInmuebleUseCase updateInmuebleUseCase;
    private final PauseInmueblePublicationUseCase pauseInmueblePublicationUseCase;
    private final ResumeInmuebleUseCase resumeInmuebleUseCase;
    private final RenewInmuebleUseCase renewInmuebleUseCase;
    private final DeleteInmuebleUseCase deleteInmuebleUseCase;

    private final InmuebleApiMapper mapper;
    private final Validator validator;

    public Mono<ServerResponse> crearInmueble(ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    log.info("[CREATE_INMUEBLE] userId={} - iniciando creación de inmueble", userId);
                    return request.bodyToMono(CreateInmuebleDto.class)
                            .doOnNext(this::validate)
                            .doOnNext(this::validateFotoOrders)
                            .flatMap(dto -> crearInmuebleUseCase
                                    .execute(mapper.toInmueble(dto, userId), mapper.toFotos(dto.fotos())))
                            .doOnSuccess(result -> log.info(
                                    "[CREATE_INMUEBLE] userId={} - inmueble creado id={}",
                                    userId,
                                    result.inmueble().getId()))
                            .flatMap(result -> ServerResponse
                                    .status(HttpStatus.CREATED)
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(mapper.toResponse(result)));
                });
    }

    public Mono<ServerResponse> getinmueblesByUser(ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    log.info("[GET_INMUEBLE_BY_USER] userId={} - iniciando consulta de propiedades", userId);
                    return findAllImobilieByUserUseCase.execute(userId)
                            .map(mapper::toResponse)
                            .collectList()
                            .flatMap(list -> ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(list));
                });
    }

    public Mono<ServerResponse> updateInmueble(ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    String inmuebleId = request.pathVariable("id");
                    log.info("[UPDATE_INMUEBLE] userId={} inmuebleId={} - iniciando actualización", userId, inmuebleId);
                    return request.bodyToMono(CreateInmuebleDto.class)
                            .doOnNext(this::validate)
                            .doOnNext(this::validateFotoOrders)
                            .flatMap(dto -> updateInmuebleUseCase.execute(
                                    mapper.toInmueble(dto, userId).toBuilder().id(inmuebleId).build(),
                                    mapper.toFotos(dto.fotos())
                            ))
                            .doOnSuccess(result -> log.info(
                                    "[UPDATE_INMUEBLE] userId={} inmuebleId={} - actualización exitosa",
                                    userId,
                                    inmuebleId))
                            .flatMap(result -> ServerResponse
                                    .ok()
                                    .contentType(MediaType.APPLICATION_JSON)
                                    .bodyValue(mapper.toResponse(result)));
                });
    }

    public Mono<ServerResponse> pauseInmueble(ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    String inmuebleId = request.pathVariable("id");
                    log.info("[PAUSE_INMUEBLE] userId={} inmuebleId={} - iniciando pausa", userId, inmuebleId);
                    return pauseInmueblePublicationUseCase.execute(inmuebleId, userId)
                            .then(ServerResponse.noContent().build());
                });
    }

    public Mono<ServerResponse> resumeInmueble(ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    String inmuebleId = request.pathVariable("id");
                    log.info("[RESUME_INMUEBLE] userId={} inmuebleId={} - iniciando reactivacion", userId, inmuebleId);
                    return resumeInmuebleUseCase.execute(inmuebleId, userId)
                            .then(ServerResponse.noContent().build());
                });
    }

    public Mono<ServerResponse> renewInmueble(ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    String inmuebleId = request.pathVariable("id");
                    log.info("[RENEW_INMUEBLE] userId={} inmuebleId={} - iniciando renovacion", userId, inmuebleId);
                    return renewInmuebleUseCase.execute(inmuebleId, userId)
                            .then(ServerResponse.noContent().build());
                });
    }

    public Mono<ServerResponse> deleteInmueble (ServerRequest request) {
        return extractUserId(request)
                .flatMap(userId -> {
                    String inmuebleId = request.pathVariable("id");
                    log.info("[DELETE_INMUEBLE] userId={} inmuebleId={} - iniciando eliminacion", userId, inmuebleId);
                    return deleteInmuebleUseCase.execute(inmuebleId, userId)
                            .then(ServerResponse.noContent().build());
                });
    }

    private Mono<String> extractUserId(ServerRequest request) {
        return Mono.deferContextual(ctx ->
                ctx.<String>getOrEmpty(UserIdExtractorFilter.CTX_USER_ID)
                        .map(Mono::just)
                        .orElseGet(() -> Mono.error(new UnauthorizedException(
                                "MISSING_USER_IDENTITY",
                                "El gateway no proporcionó identidad de usuario"
                        )))
        );
    }

    private void validate(Object body) {
        Set<ConstraintViolation<Object>> violations = validator.validate(body);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
    }

    private void validateFotoOrders(CreateInmuebleDto dto) {
        if (dto.fotos() == null) return;
        long distinctOrders = dto.fotos().stream().map(foto -> foto.order()).distinct().count();
        if (distinctOrders < dto.fotos().size()) {
            throw new ValidationException("VALIDATION_ERROR",
                    "Los valores de 'order' en las fotos deben ser únicos");
        }
    }
}
