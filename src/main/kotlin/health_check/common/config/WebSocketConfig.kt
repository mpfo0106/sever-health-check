package health_check.common.config

import health_check.healthcheck.websocket.WebSocketHealthCheckHandler
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.socket.client.standard.StandardWebSocketClient

@Configuration
class WebSocketConfig {
    @Bean
    fun webSocketClient(): StandardWebSocketClient {
        return StandardWebSocketClient()
    }
    
    @Bean
    // 매 새로운 WebSocketHealthCheckHandler를 생성
    fun webSocketHandlerFactory(): () -> WebSocketHealthCheckHandler {
        return { WebSocketHealthCheckHandler() }
    }
}
