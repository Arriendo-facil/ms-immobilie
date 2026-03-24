package co.com.bancolombia.api.config;

import co.com.bancolombia.api.dto.common.ErrorResponse;
import co.com.bancolombia.model.exception.ConflictException;
import co.com.bancolombia.model.exception.DomainException;
import co.com.bancolombia.model.exception.NotFoundException;
import co.com.bancolombia.model.exception.ValidationException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.validation.ConstraintViolationException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.core.io.buffer.DataBuffer;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebExceptionHandler;
import reactor.core.publisher.Mono;

import java.nio.ByteBuffer;
import java.util.stream.Collectors;

@Component
@Order(-2)
@Slf4j
public class GlobalErrorHandler implements WebExceptionHandler {
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public Mono<Void> handle(ServerWebExchange exchange, Throwable ex) {
        HttpStatusCode status;
        String errorCode;
        String message;

        if (ex instanceof ConflictException e) {
            status = HttpStatus.CONFLICT;
            errorCode = e.getErrorCode();
            message = e.getMessage();
        } else if (ex instanceof NotFoundException e) {
            status = HttpStatus.NOT_FOUND;
            errorCode = e.getErrorCode();
            message = e.getMessage();
        } else if (ex instanceof ValidationException e) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = e.getErrorCode();
            message = e.getMessage();
        } else if (ex instanceof ConstraintViolationException e) {
            status = HttpStatus.BAD_REQUEST;
            errorCode = "VALIDATION_ERROR";
            message = e.getConstraintViolations().stream()
                    .map(v -> v.getPropertyPath() + ": " + v.getMessage())
                    .collect(Collectors.joining(", "));
        } else if (ex instanceof DomainException e) {
            status = HttpStatusCode.valueOf(422);
            errorCode = e.getErrorCode();
            message = e.getMessage();
        } else {
            status = HttpStatus.INTERNAL_SERVER_ERROR;
            errorCode = "INTERNAL_ERROR";
            message = "Error interno del servidor";
        }

        logError(ex, errorCode, message, exchange.getRequest().getPath().value());
        return writeResponse(exchange, status, new ErrorResponse(errorCode, message));
    }

    private void logError(Throwable ex, String errorCode, String message, String path) {
        if (ex instanceof DomainException || ex instanceof ConstraintViolationException) {
            log.warn("[{}] {} - path: {}", errorCode, message, path);
        } else {
            log.error("[{}] Error no controlado en path: {}", errorCode, path, ex);
        }
    }

    private Mono<Void> writeResponse(ServerWebExchange exchange, HttpStatusCode status, ErrorResponse body) {
        exchange.getResponse().setStatusCode(status);
        exchange.getResponse().getHeaders().setContentType(MediaType.APPLICATION_JSON);
        try {
            byte[] bytes = objectMapper.writeValueAsBytes(body);
            DataBuffer buffer = exchange.getResponse().bufferFactory().wrap(ByteBuffer.wrap(bytes));
            return exchange.getResponse().writeWith(Mono.just(buffer));
        } catch (Exception e) {
            return Mono.error(e);
        }
    }
}
