package co.com.bancolombia.consumer.msuser;

import co.com.bancolombia.model.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import reactor.test.StepVerifier;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class UserClientAdapterTest {

    private static final String USER_ID = "u1";
    private static final Map<String, Object> USER_DATA = Map.of("id", "u1", "name", "Juan");

    @Mock
    private MsUserFeignClient feignClient;

    private UserClientAdapter adapter;

    @BeforeEach
    void setUp() {
        adapter = new UserClientAdapter(feignClient);
    }

    @Test
    void findById_whenFeignSucceeds_returnsMono() {
        when(feignClient.findById(USER_ID)).thenReturn(USER_DATA);

        StepVerifier.create(adapter.findById(USER_ID))
                .assertNext(result -> assertThat(result).isEqualTo(USER_DATA))
                .verifyComplete();
    }

    @Test
    void findById_executesOnBoundedElasticScheduler() {
        when(feignClient.findById(USER_ID)).thenReturn(USER_DATA);

        StepVerifier.create(adapter.findById(USER_ID))
                .assertNext(value -> {
                    assertThat(value).isNotNull();
                    assertThat(value).containsEntry("id", "u1");
                    assertThat(value).containsEntry("name", "Juan");
                })
                .verifyComplete();
    }

    @Test
    void findById_whenFeignThrowsRuntimeException_propagatesError() {
        when(feignClient.findById(USER_ID)).thenThrow(new RuntimeException("not found"));

        StepVerifier.create(adapter.findById(USER_ID))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(RuntimeException.class);
                    assertThat(error.getMessage()).isEqualTo("not found");
                })
                .verify();
    }

    @Test
    void findById_whenFeignThrowsExternalServiceException_propagatesError() {
        ExternalServiceException cause = new ExternalServiceException(
                "MS_USER_UNAVAILABLE",
                "El servicio de usuarios no está disponible temporalmente. Intente de nuevo más tarde."
        );
        when(feignClient.findById(USER_ID)).thenThrow(cause);

        StepVerifier.create(adapter.findById(USER_ID))
                .expectErrorSatisfies(error -> {
                    assertThat(error).isInstanceOf(ExternalServiceException.class);
                    assertThat(((ExternalServiceException) error).getErrorCode())
                            .isEqualTo("MS_USER_UNAVAILABLE");
                })
                .verify();
    }
}
