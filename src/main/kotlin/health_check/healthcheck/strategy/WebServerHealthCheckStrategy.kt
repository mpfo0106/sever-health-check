package health_check.healthcheck.strategy

import health_check.common.error.WebServerExceptionHandler
import health_check.healthcheck.dto.HealthCheckResultDto
import health_check.server.model.ServerHealth
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.client.RestTemplate

@Component
class WebServerHealthCheckStrategy(private val restTemplate: RestTemplate) :
    health_check.healthcheck.strategy.HealthCheckStrategy {
    private val log = LoggerFactory.getLogger(this::class.java)
    override suspend fun checkHealth(serverHealth: ServerHealth): HealthCheckResultDto {
        log.debug("Checking health for web serverHealth: {} at URL: {}", serverHealth.hostName, serverHealth.host)
        try {
            restTemplate.getForEntity(serverHealth.host, String::class.java)
            log.debug(
                "Successfully connected to web serverHealth: {}. Response time: {} ms",
                serverHealth.hostName,
            )
            return HealthCheckResultDto(true, null)
        } catch (e: Exception) {
            log.error("Error checking web serverHealth health for {}: {}", serverHealth.hostName, e.message)
            val errorCode = WebServerExceptionHandler.handleWebServerException(e)
            return HealthCheckResultDto(
                isHealthy = false,
                errorMessage = errorCode.message,
            )
        }
    }
}