package co.com.bancolombia.r2dbc.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgreSQLConnectionPoolTest {

    @Test
    void constants_haveExpectedPoolValues() {
        assertThat(PostgreSQLConnectionPool.INITIAL_SIZE).isEqualTo(12);
        assertThat(PostgreSQLConnectionPool.MAX_SIZE).isEqualTo(15);
        assertThat(PostgreSQLConnectionPool.MAX_IDLE_TIME).isEqualTo(30);
        assertThat(PostgreSQLConnectionPool.DEFAULT_PORT).isEqualTo(5432);
    }

    @Test
    void constants_initialSizeIsLessThanOrEqualToMaxSize() {
        assertThat(PostgreSQLConnectionPool.INITIAL_SIZE)
                .isLessThanOrEqualTo(PostgreSQLConnectionPool.MAX_SIZE);
    }
}
