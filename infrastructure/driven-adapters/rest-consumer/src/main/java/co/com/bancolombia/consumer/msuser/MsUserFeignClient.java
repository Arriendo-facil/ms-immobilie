package co.com.bancolombia.consumer.msuser;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// NOTA: Cuando se active Eureka, eliminar el atributo `url` y el Feign Client
// resolverá "ms-user" automáticamente vía Spring Cloud LoadBalancer.
@FeignClient(name = "ms-user", url = "${clients.ms-user.url}", fallbackFactory = MsUserFeignClientFallbackFactory.class)
public interface MsUserFeignClient {

    @GetMapping("/api/v1/user/{id}")
    Map<String, Object> findById(@PathVariable("id") String id);
}
