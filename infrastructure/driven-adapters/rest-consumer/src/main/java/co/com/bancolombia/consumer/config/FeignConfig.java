package co.com.bancolombia.consumer.config;

import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.context.annotation.Configuration;

@Configuration
@EnableFeignClients(basePackages = "co.com.bancolombia.consumer")
public class FeignConfig {
}
