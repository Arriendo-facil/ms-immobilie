package co.com.bancolombia.r2dbc.config;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class PostgresqlConnectionPropertiesTest {

    @Test
    void constructor_storesAllPropertiesCorrectly() {
        PostgresqlConnectionProperties props = new PostgresqlConnectionProperties(
                "localhost", 5432, "ms_user", "public", "postgres", "secret"
        );

        assertThat(props.host()).isEqualTo("localhost");
        assertThat(props.port()).isEqualTo(5432);
        assertThat(props.database()).isEqualTo("ms_user");
        assertThat(props.schema()).isEqualTo("public");
        assertThat(props.username()).isEqualTo("postgres");
        assertThat(props.password()).isEqualTo("secret");
    }
}
