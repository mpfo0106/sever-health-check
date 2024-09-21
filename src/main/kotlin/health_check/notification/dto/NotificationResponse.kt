package health_check.notification.dto

import java.time.LocalDateTime

data class NotificationResponse(
    var channelId: String,
    val updatedAt: LocalDateTime
)