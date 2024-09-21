package health_check.healthcheck.service

import health_check.common.error.exception.ErrorCode
import health_check.healthcheck.dto.HealthCheckResultDto
import health_check.server.model.Environment
import health_check.server.model.ServerHealth
import health_check.server.service.ServerHealthService
import health_check.slack.service.SlackHookService
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue

@ExtendWith(MockitoExtension::class)
class HealthCheckServiceTest {
    @Mock
    private lateinit var healthCheckStrategySelector: HealthCheckStrategySelector

    @Mock
    private lateinit var healthCheckStrategy: health_check.healthcheck.strategy.HealthCheckStrategy

    @Mock
    private lateinit var serverHealthService: ServerHealthService

    @Mock
    private lateinit var slackHookService: SlackHookService

    private lateinit var healthCheckService: HealthCheckService

    private val healthCheckTimeout = 3000L

    private val healthyThreshold = 3

    private val unhealthyThreshold = 3

    private lateinit var testServer: ServerHealth

    @BeforeEach
    fun setUp() {
        healthCheckService = HealthCheckService(
            healthCheckStrategySelector,
            serverHealthService,
            slackHookService, healthCheckTimeout, healthyThreshold, unhealthyThreshold
        )
        testServer = ServerHealth(
            id = 1,
            hostName = "web-server-test",
            host = "https://web-server-test",
            port = 443,
            type = 1,
            createdAt = LocalDateTime.now(),
            notification = true,
            environment = Environment.DEV
        )
    }

    @Test
    @DisplayName("checkServerHealthWithTimeout 함수 성공")
    fun checkServerHealthTestSuccess() {
        runBlocking {
            // given
            val serverDto = HealthCheckResultDto(
                true,
                null
            )
            whenever(healthCheckStrategySelector.selectStrategy(testServer)).thenReturn(healthCheckStrategy)
            whenever(healthCheckStrategy.checkHealth(testServer)).thenReturn(serverDto)

            // when
            val (resultServer, resultDto) = healthCheckService.checkServerHealthWithTimeout(testServer)

            // then
            assertEquals(testServer, resultServer)
            assertEquals(resultDto, serverDto)
        }
    }

    @Test
    @DisplayName("checkServerHealthWithTimeout 함수 타임아웃")
    fun checkServerHealthTestTimeOut() {
        runBlocking {
            // given
            whenever(healthCheckStrategySelector.selectStrategy(testServer)).thenReturn(healthCheckStrategy)
            whenever(healthCheckStrategy.checkHealth(testServer)).thenAnswer {
                Thread.sleep(1000 + healthCheckTimeout)
                HealthCheckResultDto(
                    false,
                    ErrorCode.HEALTH_CHECK_TIMEOUT.message,
                )
            }

            // when
            val (resultServer, resultDto) = healthCheckService.checkServerHealthWithTimeout(testServer)

            // then
            assertEquals(testServer, resultServer)
            assertEquals(resultDto.isHealthy, false)
            assertEquals(resultDto.errorMessage, ErrorCode.HEALTH_CHECK_TIMEOUT.message)
        }
    }

    @Test
    @DisplayName("Healthy 에서 UnHealthy 로 updateServerStatus")
    fun updateServerStatusHealthyToUnHealthy() {
        runBlocking {
            // given
            val healthCheckResult = HealthCheckResultDto(false, "Error")

            // when
            repeat(unhealthyThreshold) {
                healthCheckService.updateServerStatus(testServer, healthCheckResult)
            }

            // then
            val status = healthCheckService.getServerStatus(testServer.id)
            assertFalse(status!!.isHealthy)
            assertEquals(status.consecutiveFailureCount, unhealthyThreshold)
            assertEquals(status.consecutiveSuccessCount, 0)
        }
    }

    @Test
    @DisplayName("첫 Healthy 로 updateServerStatus")
    fun updateServerStatusHealthyToHealthy() {
        runBlocking {
            // given
            val healthCheckResult = HealthCheckResultDto(true, null)

            // when
            healthCheckService.updateServerStatus(testServer, healthCheckResult)

            // then
            val status = healthCheckService.getServerStatus(testServer.id)
            assertTrue(status!!.isHealthy)
            assertEquals(status.consecutiveFailureCount, 0)
            assertEquals(status.consecutiveSuccessCount, 1)
        }
    }

    @Test
    @DisplayName("정상 -> 비정상 -> 정상 복구 updateServer")
    fun updateServerStatusHealthyToUnHealthyToRecovery() {
        runBlocking {
            // given
            val healthyResult = HealthCheckResultDto(true, null)
            val unhealthyResult = HealthCheckResultDto(false, ErrorCode.HEALTH_CHECK_TIMEOUT.message)

            // when: 정상상태 시작
            healthCheckService.updateServerStatus(testServer, healthyResult)

            // then
            var status = healthCheckService.getServerStatus(testServer.id)
            assertTrue(status!!.isHealthy)
            assertEquals(status.consecutiveSuccessCount, 1)
            assertEquals(status.consecutiveFailureCount, 0)

            // when : 비정상으로 전환
            repeat(unhealthyThreshold) {
                healthCheckService.updateServerStatus(testServer, unhealthyResult)
            }

            // then : 비정상 검증
            status = healthCheckService.getServerStatus(testServer.id)
            assertFalse(status!!.isHealthy)
            assertEquals(status.consecutiveSuccessCount, 0)
            assertEquals(status.consecutiveFailureCount, unhealthyThreshold)
            assertEquals(status.lastErrorMessage, ErrorCode.HEALTH_CHECK_TIMEOUT.message)

            // 슬랙 오류 메세지 전송 검증
            verify(slackHookService).sendChannelIdMessage(
                any(),
                any(),
                eq(true)
            )

            // when : 다시 정상 복구
            repeat(healthyThreshold) {
                healthCheckService.updateServerStatus(testServer, healthyResult)
            }
            // then
            status = healthCheckService.getServerStatus(testServer.id)

            assertTrue(status!!.isHealthy)
            assertEquals(status.consecutiveSuccessCount, healthyThreshold)
            assertEquals(status.consecutiveFailureCount, 0)
            // 슬랙 복구 메세지 전송 검증
            verify(slackHookService).sendChannelIdMessage(
                any(),
                any(),
                eq(false)
            )
            assertNull(status.lastErrorMessage)
        }
    }

    @Test
    @DisplayName("서버 알림설정이 꺼져있을때는 슬랙 메세지가 전송되지 않게한다")
    fun noSlackMessageWithoutNotification() {
        runBlocking {
            // given
            val serverWithoutNotifications = ServerHealth(
                id = 1,
                hostName = "web-server-test",
                host = "https://web-server-test",
                port = 443,
                type = 1,
                createdAt = LocalDateTime.now(),
                notification = false,
                environment = Environment.DEV
            )
            val unhealthyResult = HealthCheckResultDto(false, "Error occurred")
            val healthyResult = HealthCheckResultDto(true, null)

            // when : unhealthy 로 변환
            repeat(unhealthyThreshold) {
                healthCheckService.updateServerStatus(serverWithoutNotifications, unhealthyResult)
            }

            // then
            var status = healthCheckService.getServerStatus(serverWithoutNotifications.id)
            assertFalse(status!!.isHealthy)
            verify(slackHookService, never()).sendChannelIdMessage(any(), any(), eq(true))

            // when: healthy 로 정상 복구
            repeat(healthyThreshold) {
                healthCheckService.updateServerStatus(serverWithoutNotifications, healthyResult)
            }

            // then
            status = healthCheckService.getServerStatus(serverWithoutNotifications.id)
            assertTrue(status!!.isHealthy)
            verify(slackHookService, never()).sendChannelIdMessage(any(), any(), eq(false))
        }
    }
}