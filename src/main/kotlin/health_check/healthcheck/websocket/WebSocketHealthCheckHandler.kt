package health_check.healthcheck.websocket

import org.slf4j.LoggerFactory
import org.springframework.web.socket.CloseStatus
import org.springframework.web.socket.WebSocketHandler
import org.springframework.web.socket.WebSocketMessage
import org.springframework.web.socket.WebSocketSession
import java.util.concurrent.atomic.AtomicBoolean

class WebSocketHealthCheckHandler : WebSocketHandler {
    //멀티스레드 환경 고려
    private val connected = AtomicBoolean(false)
    private var session: WebSocketSession? = null
    private val log = LoggerFactory.getLogger(this::class.java)

    // 웹소켓 연결 성공적일때 호출
    override fun afterConnectionEstablished(session: WebSocketSession) {
        this.session = session
        connected.set(true)
    }

    // 웹소켓 메세지를 수신할때 호출
    override fun handleMessage(session: WebSocketSession, message: WebSocketMessage<*>) {
    }

    //웹소켓 전송중 오류 발생했을때 호출
    override fun handleTransportError(session: WebSocketSession, exception: Throwable) {
        connected.set(false)
        log.error("Transport error: ${exception.message}")
    }

    // 웹소켓 연결이 닫힌후 호출
    override fun afterConnectionClosed(session: WebSocketSession, closeStatus: CloseStatus) {
        connected.set(false)
    }

    // 부분 메세지 지원 여부 반환
    override fun supportsPartialMessages(): Boolean = false

    fun isConnected(): Boolean = connected.get()
    fun closeConnection() {
        session?.close()
    }

}