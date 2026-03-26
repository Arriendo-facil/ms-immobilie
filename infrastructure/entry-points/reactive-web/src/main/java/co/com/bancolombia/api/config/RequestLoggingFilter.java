package co.com.bancolombia.api.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

import java.util.UUID;

@Component
@Order(-2)
@Slf4j
public class RequestLoggingFilter implements WebFilter {

    public static final String CORRELATION_ID_HEADER = "X-Correlation-Id";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long startTime = System.currentTimeMillis();
        String method = exchange.getRequest().getMethod().name();
        String path = exchange.getRequest().getPath().value();
        String userId = exchange.getRequest().getHeaders().getFirst(GlobalErrorHandler.USER_ID_HEADER);
        String user = userId != null ? userId : "anonymous";

        String incomingCorrelationId = exchange.getRequest().getHeaders().getFirst(CORRELATION_ID_HEADER);
        String correlationId = (incomingCorrelationId != null && !incomingCorrelationId.isBlank())
                ? incomingCorrelationId
                : UUID.randomUUID().toString();

        exchange.getResponse().getHeaders().set(CORRELATION_ID_HEADER, correlationId);

        log.info("--> {} {} | userId={} | correlationId={}", method, path, user, correlationId);

        return chain.filter(exchange)
                .contextWrite(Context.of(CORRELATION_ID_HEADER, correlationId))
                .doFinally(signal -> {
                    long duration = System.currentTimeMillis() - startTime;
                    int statusCode = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;
                    log.info("<-- {} {} | {} | {}ms | userId={} | correlationId={}",
                            method, path, statusCode, duration, user, correlationId);
                });
    }
}
