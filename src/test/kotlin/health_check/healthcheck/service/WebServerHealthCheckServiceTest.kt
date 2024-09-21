package health_check.healthcheck.service

import health_check.common.error.exception.ErrorCode
import health_check.healthcheck.strategy.WebServerHealthCheckStrategy
import health_check.server.model.Environment
import health_check.server.model.ServerHealth
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.whenever
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestTemplate
import java.net.UnknownHostException
import java.time.LocalDateTime
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class WebServerHealthCheckServiceTest {
    @Mock
    private lateinit var restTemplate: RestTemplate

    private lateinit var webServerHealthCheckStrategy: WebServerHealthCheckStrategy

    private lateinit var testWebServer: ServerHealth

    @BeforeEach
    fun setUp() {
        webServerHealthCheckStrategy = WebServerHealthCheckStrategy(restTemplate)
        testWebServer = ServerHealth(
            id = 1,
            hostName = "webserver-test",
            host = "https://webserver-test",
            port = 443,
            type = 1,
            createdAt = LocalDateTime.now(),
            notification = true,
            environment = Environment.DEV
        )
    }

    @Test
    @DisplayName("정상적으로  요청이 성공한 경우")
    fun successfulHealthCheck() {
        runBlocking {
            // given
            whenever(restTemplate.getForEntity(testWebServer.host, String::class.java)).thenReturn(
                ResponseEntity(
                    "OK",
                    HttpStatus.OK
                )
            )

            // when
            val result = webServerHealthCheckStrategy.checkHealth(testWebServer)

            // then
            assertTrue(result.isHealthy)
            assertNull(result.errorMessage)
        }
    }

    @Test
    @DisplayName("서버가 2xx 가 아닌 상태로 응답하는 경우")
    fun healthCheckFailsWithNon2xxStatusCode() {
        runBlocking {
            // given
            whenever(
                restTemplate.getForEntity(
                    testWebServer.host,
                    String::class.java
                )
            ).thenThrow(HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR))
            // when
            val result = webServerHealthCheckStrategy.checkHealth(testWebServer)

            // then
            assertFalse(result.isHealthy)
            assertEquals(ErrorCode.WEBSERVER_ERROR.message, result.errorMessage)
        }
    }

    @Test
    @DisplayName("HttpServerError 가 발생하는 경우")
    fun healthCheckFailsWithHttpServerError() {
        runBlocking {
            // given
            whenever(restTemplate.getForEntity(testWebServer.host, String::class.java)).thenThrow(
                HttpServerErrorException(HttpStatus.INTERNAL_SERVER_ERROR)
            )

            // when
            val result = webServerHealthCheckStrategy.checkHealth(testWebServer)

            // then
            assertFalse(result.isHealthy)
            assertEquals(ErrorCode.WEBSERVER_ERROR.message, result.errorMessage)
        }
    }

    @Test
    @DisplayName("UnknownHostException 가 발생하는 경우")
    fun healthCheckFailsWithUnknownHost() {
        runBlocking {
            // given
            whenever(restTemplate.getForEntity(testWebServer.host, String::class.java)).thenThrow(
                ResourceAccessException("Unknown host", UnknownHostException())
            )

            // when
            val result = webServerHealthCheckStrategy.checkHealth(testWebServer)

            // then
            assertFalse(result.isHealthy)
            assertEquals(ErrorCode.WEBSERVER_UNKNOWN_HOST.message, result.errorMessage)
        }
    }
}