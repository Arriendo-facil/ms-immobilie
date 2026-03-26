package co.com.bancolombia.api.inmueble;

import co.com.bancolombia.api.config.UserIdExtractorFilter;
import co.com.bancolombia.api.dto.inmueble.CreateInmuebleDto;
import co.com.bancolombia.api.mapper.InmuebleApiMapper;
import co.com.bancolombia.model.exception.UnauthorizedException;
import co.com.bancolombia.model.exception.ValidationException;
import co.com.bancolombia.usecase.crearInmueble.CrearInmuebleUseCase;
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
    private final InmuebleApiMapper mapper;
    private final Validator validator;

    public Mono<ServerResponse> crearInmueble(ServerRequest request) {
        return Mono.deferContextual(ctx -> {
            String userId = ctx.<String>getOrEmpty(UserIdExtractorFilter.CTX_USER_ID).orElse(null);
            if (userId == null) {
                return Mono.error(new UnauthorizedException("MISSING_USER_IDENTITY",
                        "El gateway no proporcionó identidad de usuario"));
            }
            log.info("[CREATE_INMUEBLE] userId={} - iniciando creación de inmueble", userId);
            return request.bodyToMono(CreateInmuebleDto.class)
                    .doOnNext(this::validate)
                    .doOnNext(this::validateFotoOrders)
                    .flatMap(dto -> crearInmuebleUseCase.execute(mapper.toInmueble(dto, userId), mapper.toFotos(dto.fotos())))
                    .doOnSuccess(result -> log.info("[CREATE_INMUEBLE] userId={} - inmueble creado id={}", userId, result.inmueble().getId()))
                    .flatMap(result -> ServerResponse
                            .status(HttpStatus.CREATED)
                            .contentType(MediaType.APPLICATION_JSON)
                            .bodyValue(mapper.toResponse(result)));
        });
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
