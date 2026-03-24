package co.com.bancolombia.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
    info = @Info(
        title = "ms-immobilie — Microservicio de Inmuebles",
        version = "1.0.0",
        description = """
            API REST reactiva (Spring WebFlux) para la gestión de inmuebles
            de la plataforma **Arriendo Fácil**.
            """,
        contact = @Contact(
            name = "Equipo Arriendo Fácil",
            email = "dev@arriendofacil.co"
        ),
        license = @License(
            name = "Privado — Uso interno",
            url = "https://arriendofacil.co"
        )
    ),
    servers = {
        @Server(url = "http://localhost:8080", description = "Servidor local de desarrollo"),
        @Server(url = "https://api.arriendofacil.co", description = "Servidor de producción")
    }
)
public class OpenApiConfig {
}
