package health_check.server.service

import health_check.healthcheck.service.HealthCheckService
import health_check.server.dto.ServerStatusDto
import org.springframework.stereotype.Service
import java.time.format.DateTimeFormatter

@Service
class ServerStatusService(
    private val serverHealthService: ServerHealthService,
    private val healthCheckService: HealthCheckService
) {
    fun getAllServerStatus(): List<ServerStatusDto> {
        val servers = serverHealthService.getAllServers()
        val serverStatuses = healthCheckService.getAllServerStatus()
        return servers.map { server ->
            val status = serverStatuses[server.id]
            ServerStatusDto(
                id = server.id,
                name = server.hostName,
                domain = server.host,
                health = when {
                    !server.notification -> "MONITORING OFF"
                    status?.isHealthy == true -> "HEALTHY"
                    status?.isHealthy == false -> "UNHEALTHY"
                    else -> "UNKNOWN"
                },
                onStateFrom = status?.lastErrorTime?.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))
                    ?: "N/A",
                errorMessage = status?.lastErrorMessage,
                notification = server.notification,
                environment = server.environment
            )
        }.sortedBy {
            when (it.health) {
                "UNHEALTHY" -> 0
                "HEALTHY" -> 1
                "UNKNOWN" -> 2
                "MONITORING OFF" -> 3
                else -> 4
            }
        }
    }
}