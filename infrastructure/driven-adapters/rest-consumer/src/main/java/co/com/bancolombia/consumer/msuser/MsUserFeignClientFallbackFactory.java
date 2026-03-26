package co.com.bancolombia.consumer.msuser;

import co.com.bancolombia.model.exception.ExternalServiceException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.openfeign.FallbackFactory;
import org.springframework.stereotype.Component;

import java.util.Map;

@Slf4j
@Component
public class MsUserFeignClientFallbackFactory implements FallbackFactory<MsUserFeignClient> {

    @Override
    public MsUserFeignClient create(Throwable cause) {
        log.warn("[ms-user] Circuito abierto o fallo detectado: {}", cause.getMessage());
        return userId -> {
            log.error("[ms-user] Fallback activado para userId={}", userId, cause);
            throw new ExternalServiceException(
                    "MS_USER_UNAVAILABLE",
                    "El servicio de usuarios no está disponible temporalmente. Intente de nuevo más tarde."
            );
        };
    }
}
