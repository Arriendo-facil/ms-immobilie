package co.com.bancolombia.consumer.msuser;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

// url: static for now — remove this attribute and Eureka will resolve by name automatically
@FeignClient(name = "ms-user", url = "${clients.ms-user.url}")
public interface MsUserFeignClient {

    @GetMapping("/api/user/{id}")
    Map<String, Object> findById(@PathVariable String id);
}
