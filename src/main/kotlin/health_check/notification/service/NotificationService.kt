package health_check.notification.service

import health_check.common.error.exception.BusinessException
import health_check.common.error.exception.ErrorCode
import health_check.notification.model.Notification
import health_check.notification.repository.NotificationRepository
import health_check.slack.service.SlackChannelService
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional

@Service
class NotificationService(
    private val notificationRepository: NotificationRepository,
    private val slackChannelService: SlackChannelService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Transactional(readOnly = true)
    fun getAllNotificationStatus(): List<Notification> = notificationRepository.findAll()

    @Transactional(readOnly = true)
    fun getNotificationStatus(channelId: String): Notification {
        return notificationRepository.findById(channelId)
            .orElseThrow { BusinessException(ErrorCode.GLOBAL_NOTIFICATION_NOT_FOUND) }
    }

    @Transactional
    fun updateNotifications(channelIds: List<String>): List<Notification> {
        val validChannelIds = channelIds.distinct().filter { slackChannelService.isValidChannelId(it) }
        if (validChannelIds.isNotEmpty()) {
            val newNotifications = validChannelIds.map { Notification(channelId = it) }
            return notificationRepository.saveAll(newNotifications)
        }
        return emptyList()
    }

    @Transactional
    fun deleteNotificationChannel(channelId: String) {
        log.debug("Attempting to delete notifications with id: {}", channelId)
        if (!notificationRepository.existsById(channelId)) {
            throw BusinessException(ErrorCode.SLACK_INVALID_CHANNEL_ID)
        }
        notificationRepository.deleteById(channelId)
        log.info("Notification deleted successfully with id : {}", channelId)
    }

    @Transactional
    fun deleteAllNotificationChannel() {
        log.debug("Attempting to delete All notifications")
        notificationRepository.deleteAll()
        log.info("All Notification deleted successfully")
    }
}