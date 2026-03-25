package co.com.bancolombia.model.user.gateways;

import reactor.core.publisher.Mono;

import java.util.Map;

public interface UserClient {
    Mono<Map<String, Object>> findById(String userId);
}
