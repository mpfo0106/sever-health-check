package health_check.healthcheck.service

import kotlinx.coroutines.*
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.scheduling.annotation.Scheduled
import org.springframework.stereotype.Service
import health_check.common.error.exception.ErrorCode
import health_check.healthcheck.dto.HealthCheckResultDto
import health_check.healthcheck.model.ServerStatus
import health_check.server.model.ServerHealth
import health_check.server.service.ServerHealthService
import health_check.slack.service.SlackHookService
import java.time.LocalDateTime

@Service
class HealthCheckService(
    private val healthCheckStrategySelector: HealthCheckStrategySelector,
    private val serverHealthService: ServerHealthService,
    private val slackHookService: SlackHookService,
    @Value("\${healthcheck.timeout:10000}")
    private val healthCheckTimeout: Long,
    @Value("\${healthcheck.healthy-threshold:3}")
    private val healthyThreshold: Int,
    @Value("\${healthcheck.unhealthy-threshold:3}")
    private val unhealthyThreshold: Int
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    private val serverStatusMap = mutableMapOf<Long, ServerStatus>()

    // 네트워크 요청(I/O)에 작업이 최적화 될 수 있게 현재 IO 세팅 + SupervisorJob 으로 코루틴이 독립적으로
    private val coroutineScope = CoroutineScope(Dispatchers.IO + SupervisorJob())


    @Scheduled(fixedRateString = "\${healthcheck.interval:30000}")
    fun startHealthCheck() {
        log.info("===========Starting health check===============")
        val servers = serverHealthService.getAllServers()

        coroutineScope.launch {
            val results = servers.map { server ->
                async { checkServerHealthWithTimeout(server) }
            }.awaitAll()

            results.forEach { (server, healthCheckResult) ->
                updateServerStatus(server, healthCheckResult)
            }
            log.debug("Completed health check for {} active servers", servers.size)
        }
    }

    // 헬스체크 수행 & 타임아웃 체크.
    internal suspend fun checkServerHealthWithTimeout(serverHealth: ServerHealth): Pair<ServerHealth, HealthCheckResultDto> =
        coroutineScope {
            val healthCheckJob = async {
                val strategy = healthCheckStrategySelector.selectStrategy(serverHealth)
                strategy.checkHealth(serverHealth)
            }
            // 타임 아웃 시간 내에 결과가 온다면
            val result = withTimeoutOrNull(healthCheckTimeout) {
                healthCheckJob.await()
            } ?: HealthCheckResultDto(
                isHealthy = false,
                errorMessage = ErrorCode.HEALTH_CHECK_TIMEOUT.message,
            )
            Pair(serverHealth, result)
        }

    // 이전 상태 기록 저장
    internal suspend fun updateServerStatus(
        serverHealth: ServerHealth,
        healthCheckResult: HealthCheckResultDto
    ) {
        val currentStatus = serverStatusMap.getOrPut(serverHealth.id) { ServerStatus() }
        updateStatusCounts(currentStatus, healthCheckResult.isHealthy)
        when {
            shouldRestoreStatus(currentStatus, healthCheckResult.isHealthy) -> handleRestoreStatus(
                serverHealth,
                currentStatus
            )

            shouldBecomeUnhealthy(currentStatus, healthCheckResult.isHealthy) -> handleUnhealthyStatus(
                serverHealth,
                currentStatus, healthCheckResult.errorMessage!!
            )
        }
    }

    // 성공 실패 카운트 업데이트
    private fun updateStatusCounts(status: ServerStatus, isHealthy: Boolean) {
        if (isHealthy) {
            status.consecutiveSuccessCount++
            status.consecutiveFailureCount = 0
        } else {
            status.consecutiveFailureCount++
            status.consecutiveSuccessCount = 0
        }
    }

    // 복구 되는 조건 충족여부
    private fun shouldRestoreStatus(
        status: ServerStatus,
        isHealthy: Boolean
    ): Boolean = isHealthy && status.consecutiveSuccessCount >= healthyThreshold && !status.isHealthy

    // 에러 발생 조건 충족여부
    private fun shouldBecomeUnhealthy(status: ServerStatus, isHealthy: Boolean): Boolean =
        !isHealthy && status.consecutiveFailureCount >= unhealthyThreshold && status.isHealthy


    // ===================== 채널 ID용 메세지 전송 =====================

    // 슬랙 오류 메세지 전송
    private fun handleUnhealthyStatus(
        serverHealth: ServerHealth,
        currentStatus: ServerStatus,
        errorMessage: String,
    ) {
        if (!currentStatus.isHealthy) {
            log.debug("{} is already unhealthy state", serverHealth.hostName)
            return
        }
        currentStatus.apply {
            isHealthy = false
            lastErrorMessage = errorMessage
            lastErrorTime = LocalDateTime.now()
            consecutiveFailureCount = unhealthyThreshold
            consecutiveSuccessCount = 0
        }
        log.error("{} is now unhealthy. Error: {}", serverHealth.hostName, errorMessage)

        if (serverHealth.notification) {
            slackHookService.sendChannelIdMessage(serverHealth.hostName, errorMessage, true)
        }
    }

    private fun handleRestoreStatus(
        serverHealth: ServerHealth,
        currentStatus: ServerStatus
    ) {
        val recoveryMessage = """
            서버가 정상화되었습니다.
            *이전 오류:* ${currentStatus.lastErrorMessage}
            *이전 오류 발생 시간:* ${currentStatus.lastErrorTime}    
        """.trimIndent()

        currentStatus.apply {
            isHealthy = true
            lastErrorTime = null
            lastErrorMessage = null
            consecutiveSuccessCount = healthyThreshold
            consecutiveFailureCount = 0
        }
        log.info("{} 정상 복구: {}", serverHealth.hostName, recoveryMessage)

        if (serverHealth.notification) {
            slackHookService.sendChannelIdMessage(serverHealth.hostName, recoveryMessage, false)
        }
    }

    // 서버 상태 단건 조회
    fun getServerStatus(serverId: Long): ServerStatus? {
        return serverStatusMap[serverId]
    }

    // 서버 상태 모두 조회
    fun getAllServerStatus(): Map<Long, ServerStatus> {
        return serverStatusMap.toMap()
    }

    //================ 기존 웹 훅용 메세지 전송법 ========================

//    // 슬랙 오류 메세지 전송
//    private fun handleUnhealthyStatus(
//        serverHealth: ServerHealth,
//        currentStatus: ServerStatus,
//        errorMessage: String,
//    ) {
//        if (!currentStatus.isHealthy) {
//            log.debug("{} is already unhealthy state", serverHealth.hostName)
//            return
//        }
//        currentStatus.apply {
//            isHealthy = false
//            lastErrorMessage = errorMessage
//            lastErrorTime = LocalDateTime.now()
//            consecutiveFailureCount = unhealthyThreshold
//            consecutiveSuccessCount = 0
//        }
//        log.error("{} is now unhealthy. Error: {}", serverHealth.hostName, errorMessage)
//
//        if (serverHealth.notifications) {
//            slackHookService.sendWebhookMessage(serverHealth.hostName, errorMessage, true)
//        }
//    }
//
//    // 슬랙 복구 메세지 전송
//    private fun handleRestoreStatus(
//        serverHealth: ServerHealth,
//        currentStatus: ServerStatus
//    ) {
//        val recoveryMessage =
//            "서버가 정상화되었습니다.\n이전 오류: ${currentStatus.lastErrorMessage}\n이전 오류 발생 시간: ${currentStatus.lastErrorTime}"
//        currentStatus.apply {
//            isHealthy = true
//            lastErrorTime = null
//            lastErrorMessage = null
//            consecutiveSuccessCount = healthyThreshold
//            consecutiveFailureCount = 0
//        }
//        log.info("{} 정상 복구: {}", serverHealth.hostName, recoveryMessage)
//
//        if (serverHealth.notifications) {
//            slackHookService.sendWebhookMessage(serverHealth.hostName, recoveryMessage, false)
//        }
//    }

}
