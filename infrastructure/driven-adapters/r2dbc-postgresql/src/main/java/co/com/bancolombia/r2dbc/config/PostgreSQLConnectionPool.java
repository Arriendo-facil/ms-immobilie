package co.com.bancolombia.r2dbc.config;

import io.r2dbc.pool.ConnectionPool;
import io.r2dbc.pool.ConnectionPoolConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionConfiguration;
import io.r2dbc.postgresql.PostgresqlConnectionFactory;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.r2dbc.config.EnableR2dbcAuditing;

import java.time.Duration;

@Configuration
@EnableR2dbcAuditing
@EnableConfigurationProperties(PostgresqlConnectionProperties.class)
public class PostgreSQLConnectionPool {
    /* Change these values for your project */
    public static final int INITIAL_SIZE = 12;
    public static final int MAX_SIZE = 15;
    public static final int MAX_IDLE_TIME = 30;
    public static final int DEFAULT_PORT = 5432;

	@Bean
	public ConnectionPool getConnectionConfig(PostgresqlConnectionProperties properties) {
		PostgresqlConnectionConfiguration dbConfiguration = PostgresqlConnectionConfiguration.builder()
                .host(properties.host())
                .port(properties.port())
                .database(properties.database())
                .schema(properties.schema())
                .username(properties.username())
                .password(properties.password())
                .build();

        ConnectionPoolConfiguration poolConfiguration = ConnectionPoolConfiguration.builder()
                .connectionFactory(new PostgresqlConnectionFactory(dbConfiguration))
                .name("ms-immobilie-postgres-pool")
                .initialSize(INITIAL_SIZE)
                .maxSize(MAX_SIZE)
                // Libera conexiones inactivas antes de que el servidor las cierre
                .maxIdleTime(Duration.ofMinutes(MAX_IDLE_TIME))
                // Rota conexiones longevas para evitar conexiones "zombie"
                .maxLifeTime(Duration.ofMinutes(60))
                // Falla rápido si no hay conexión libre en el pool (evita cola infinita)
                .maxAcquireTime(Duration.ofSeconds(5))
                // Reintentos para adquirir conexión ante fallo transitorio del pool
                .acquireRetry(2)
                // Valida la conexión contra la BD real antes de entregarla al caller
                .validationQuery("SELECT 1")
                .build();

		return new ConnectionPool(poolConfiguration);
	}
}