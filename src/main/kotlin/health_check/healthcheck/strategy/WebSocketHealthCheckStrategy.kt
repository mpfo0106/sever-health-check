package health_check.healthcheck.strategy

import health_check.common.error.WebSocketExceptionHandler
import health_check.common.util.getWebSocketUrl
import health_check.healthcheck.dto.HealthCheckResultDto
import health_check.healthcheck.websocket.WebSocketHealthCheckHandler
import health_check.server.model.ServerHealth
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Component
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.ConnectException

@Component
class WebSocketHealthCheckStrategy(
    private val webSocketClient: StandardWebSocketClient,
    private val webSocketHandlerFactory: () -> WebSocketHealthCheckHandler
) : health_check.healthcheck.strategy.HealthCheckStrategy {
    private val log = LoggerFactory.getLogger(this::class.java)
    override suspend fun checkHealth(serverHealth: ServerHealth): HealthCheckResultDto {
        val handler = webSocketHandlerFactory()
        try {
            log.debug("Checking health for {} at {}", serverHealth.hostName, serverHealth.getWebSocketUrl())
            webSocketClient.execute(handler, serverHealth.getWebSocketUrl()).get()
            // 연결 실패했다면
            if (!handler.isConnected()) {
                log.error("WebSocket connection failed for serverHealth: {}", serverHealth.hostName)
                throw ConnectException()
            }
            return HealthCheckResultDto(true, null)
        } catch (e: Exception) {
            val errorCode = WebSocketExceptionHandler.handleWebSocketException(e)
            log.error("Error checking serverHealth health for {}: {}", serverHealth.hostName, e.message)
            return HealthCheckResultDto(false, errorCode.message)
        } finally {
            handler.closeConnection()
            log.debug("Closed WebSocket connection for serverHealth: {}", serverHealth.hostName)
        }
    }
}