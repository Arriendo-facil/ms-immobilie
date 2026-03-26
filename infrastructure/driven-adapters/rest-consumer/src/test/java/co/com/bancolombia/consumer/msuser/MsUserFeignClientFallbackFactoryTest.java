package co.com.bancolombia.consumer.msuser;

import co.com.bancolombia.model.exception.ExternalServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@ExtendWith(MockitoExtension.class)
class MsUserFeignClientFallbackFactoryTest {

    private static final String USER_ID = "u1";
    private static final String EXPECTED_ERROR_CODE = "MS_USER_UNAVAILABLE";
    private static final String EXPECTED_MESSAGE =
            "El servicio de usuarios no está disponible temporalmente. Intente de nuevo más tarde.";

    private MsUserFeignClientFallbackFactory fallbackFactory;

    @BeforeEach
    void setUp() {
        fallbackFactory = new MsUserFeignClientFallbackFactory();
    }

    @Test
    void create_returnsNonNullFeignClient() {
        MsUserFeignClient client = fallbackFactory.create(new RuntimeException("error"));

        assertThat(client).isNotNull();
    }

    @Test
    void create_fallbackClient_throwsExternalServiceException() {
        MsUserFeignClient client = fallbackFactory.create(new RuntimeException("circuit open"));

        assertThatThrownBy(() -> client.findById(USER_ID))
                .isInstanceOf(ExternalServiceException.class);
    }

    @Test
    void create_fallbackClient_exceptionHasCorrectErrorCode() {
        MsUserFeignClient client = fallbackFactory.create(new RuntimeException("circuit open"));

        assertThatThrownBy(() -> client.findById(USER_ID))
                .isInstanceOf(ExternalServiceException.class)
                .satisfies(ex -> assertThat(((ExternalServiceException) ex).getErrorCode())
                        .isEqualTo(EXPECTED_ERROR_CODE));
    }

    @Test
    void create_fallbackClient_exceptionHasCorrectMessage() {
        MsUserFeignClient client = fallbackFactory.create(new RuntimeException("circuit open"));

        assertThatThrownBy(() -> client.findById(USER_ID))
                .isInstanceOf(ExternalServiceException.class)
                .hasMessage(EXPECTED_MESSAGE);
    }

    @ParameterizedTest
    @MethodSource("causas")
    void create_withDifferentCauses_alwaysThrowsExternalServiceException(Throwable cause) {
        MsUserFeignClient client = fallbackFactory.create(cause);

        assertThatThrownBy(() -> client.findById(USER_ID))
                .isInstanceOf(ExternalServiceException.class)
                .satisfies(ex -> assertThat(((ExternalServiceException) ex).getErrorCode())
                        .isEqualTo(EXPECTED_ERROR_CODE));
    }

    static Stream<Throwable> causas() {
        return Stream.of(
                new RuntimeException("timeout"),
                new IllegalStateException("connection refused"),
                new NullPointerException("null response")
        );
    }
}
