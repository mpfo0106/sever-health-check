package health_check.common.config

import io.swagger.v3.oas.models.OpenAPI
import io.swagger.v3.oas.models.info.Info
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class SwaggerConfig {
    @Bean
    fun openAPI(): OpenAPI {
        return OpenAPI().info(
            Info().title("HealthCheck Management API").description("HealthCheck Management API")
                .version("1.0.0")
        )

    }
}