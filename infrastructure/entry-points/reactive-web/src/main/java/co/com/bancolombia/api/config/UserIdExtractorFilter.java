package co.com.bancolombia.api.config;

import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;
import reactor.util.context.Context;

@Component
@Order(-1)
public class UserIdExtractorFilter implements WebFilter {

    public static final String CTX_USER_ID = "userId";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest().getHeaders().getFirst(GlobalErrorHandler.USER_ID_HEADER);
        if (userId != null && !userId.isBlank()) {
            return chain.filter(exchange).contextWrite(Context.of(CTX_USER_ID, userId));
        }
        return chain.filter(exchange);
    }
}
