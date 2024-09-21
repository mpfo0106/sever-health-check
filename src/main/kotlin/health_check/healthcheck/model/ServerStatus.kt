package health_check.healthcheck.model

import java.time.LocalDateTime

data class ServerStatus(
    var isHealthy: Boolean = true,
    var lastErrorMessage: String? = null,
    var lastErrorTime: LocalDateTime? = null,
    // 연속 성공 or 실패
    var consecutiveSuccessCount: Int = 0,
    var consecutiveFailureCount: Int = 0
)