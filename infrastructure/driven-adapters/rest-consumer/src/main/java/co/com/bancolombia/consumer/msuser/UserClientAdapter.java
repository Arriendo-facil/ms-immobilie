package co.com.bancolombia.consumer.msuser;

import co.com.bancolombia.model.user.gateways.UserClient;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

import java.util.Map;

@Repository
@RequiredArgsConstructor
public class UserClientAdapter implements UserClient {

    private final MsUserFeignClient feignClient;

    @Override
    public Mono<Map<String, Object>> findById(String userId) {
        return Mono.fromCallable(() -> feignClient.findById(userId))
                .subscribeOn(Schedulers.boundedElastic());
    }
}
