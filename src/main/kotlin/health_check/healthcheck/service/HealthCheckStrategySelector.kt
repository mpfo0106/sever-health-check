package health_check.healthcheck.service

import health_check.common.util.isWebSocketServer
import health_check.healthcheck.strategy.WebServerHealthCheckStrategy
import health_check.healthcheck.strategy.WebSocketHealthCheckStrategy
import health_check.server.model.ServerHealth
import org.springframework.stereotype.Service

@Service
class HealthCheckStrategySelector(
    private val webServerHealthCheckStrategy: WebServerHealthCheckStrategy,
    private val webSocketHealthCheckStrategy: WebSocketHealthCheckStrategy
) {
    fun selectStrategy(serverHealth: ServerHealth): health_check.healthcheck.strategy.HealthCheckStrategy {
        return when {
            serverHealth.isWebSocketServer() -> webSocketHealthCheckStrategy
            else -> webServerHealthCheckStrategy
        }
    }
}