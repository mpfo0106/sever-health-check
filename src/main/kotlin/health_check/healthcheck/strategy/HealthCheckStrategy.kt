package health_check.healthcheck.strategy

import health_check.healthcheck.dto.HealthCheckResultDto
import health_check.server.model.ServerHealth

interface HealthCheckStrategy {
    suspend fun checkHealth(serverHealth: ServerHealth): HealthCheckResultDto
}