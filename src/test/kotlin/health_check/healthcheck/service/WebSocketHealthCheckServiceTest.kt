package health_check.healthcheck.service

import health_check.common.error.exception.ErrorCode
import health_check.healthcheck.strategy.WebSocketHealthCheckStrategy
import health_check.healthcheck.websocket.WebSocketHealthCheckHandler
import health_check.server.model.Environment
import health_check.server.model.ServerHealth
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.verify
import org.mockito.kotlin.whenever
import org.springframework.web.socket.WebSocketSession
import org.springframework.web.socket.client.standard.StandardWebSocketClient
import java.net.ConnectException
import java.time.LocalDateTime
import java.util.concurrent.CompletableFuture
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class WebSocketHealthCheckServiceTest {
    @Mock
    private lateinit var webSocketClient: StandardWebSocketClient

    @Mock
    private lateinit var webSocketHandlerFactory: () -> WebSocketHealthCheckHandler

    @Mock
    private lateinit var webSocketHandler: WebSocketHealthCheckHandler

    @Mock
    private lateinit var webSocketSession: WebSocketSession

    private lateinit var webSocketHealthCheckStrategy: WebSocketHealthCheckStrategy

    private lateinit var testSocketServers: ServerHealth

    @BeforeEach
    fun setUp() {
        whenever(webSocketHandlerFactory.invoke()).thenReturn(webSocketHandler)
        webSocketHealthCheckStrategy = WebSocketHealthCheckStrategy(webSocketClient, webSocketHandlerFactory)
        testSocketServers = ServerHealth(
            id = 1,
            hostName = "webchatserver-test",
            host = "wss://webchatserver-test/chat",
            port = 443,
            type = 2,
            createdAt = LocalDateTime.parse("2024-07-19T11:32:05.403864"),
            notification = true,
            environment = Environment.DEV
        )
    }

    @Test
    @DisplayName("정상적으로 웹소켓 연결이 성공한 경우")
    fun successfulHealthCheck() {
        runBlocking {
            // given
            val server = testSocketServers
            // webSession 을 결과로 갖는 future
            val future = CompletableFuture.completedFuture(webSocketSession)
            whenever(webSocketClient.execute(any(), any())).thenReturn(future)
            whenever(webSocketHandler.isConnected()).thenReturn(true)

            // when
            val result = webSocketHealthCheckStrategy.checkHealth(server)

            // then
            assertTrue(result.isHealthy)
            assertNull(result.errorMessage)
            // 호출시 새로운 WebSocketHealthCheckHandler가 생성되었는지 확인
            verify(webSocketHandlerFactory).invoke()
        }
    }

    @Test
    @DisplayName("웹소켓 연결은 성공했지만, isConnected가 fail인 경우")
    fun failedConnection() {
        runBlocking {
            // given
            val server = testSocketServers
            val future = CompletableFuture.completedFuture(webSocketSession)
            whenever(webSocketClient.execute(any(), any())).thenReturn(future)
            whenever(webSocketHandler.isConnected()).thenReturn(false)

            // when
            val result = webSocketHealthCheckStrategy.checkHealth(server)

            // then
            assertFalse(result.isHealthy)
            assertEquals(ErrorCode.WEBSOCKET_CONNECTION_FAILED.message, result.errorMessage)
            verify(webSocketHandlerFactory).invoke()
        }
    }

    @Test
    @DisplayName("웹소켓 연결시 ConnectException이 발생한 경우")
    fun connectionException() {
        runBlocking {
            // given
            val server = testSocketServers
            // ConnectException 에러를 mockito 가 처리하지 못하기 때문에 future 를 사용
            val exceptionFuture = CompletableFuture<WebSocketSession>()
            exceptionFuture.completeExceptionally(ConnectException(ErrorCode.WEBSOCKET_UNEXPECTED_ERROR.message))
            whenever(webSocketClient.execute(any(), any())).thenReturn(exceptionFuture)

            // when
            val result = webSocketHealthCheckStrategy.checkHealth(server)

            // then
            assertFalse(result.isHealthy)
            assertEquals(ErrorCode.WEBSOCKET_CONNECTION_FAILED.message, result.errorMessage)
            verify(webSocketHandlerFactory).invoke()
        }
    }
}