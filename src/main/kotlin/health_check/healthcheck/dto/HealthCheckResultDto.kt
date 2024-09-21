package health_check.healthcheck.dto

data class HealthCheckResultDto(
    val isHealthy: Boolean,
    val errorMessage: String?,
)