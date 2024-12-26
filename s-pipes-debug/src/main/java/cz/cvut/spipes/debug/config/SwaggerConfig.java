package cz.cvut.spipes.debug.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "SPipes Debug Open API",
                description = "SPipes Debug Open API documentation",
                version = "1.0"
        )
)
public class SwaggerConfig {

}