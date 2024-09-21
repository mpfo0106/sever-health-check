package health_check.slack.service

import health_check.notification.service.NotificationService
import health_check.slack.dto.PostMessageRequest
import kotlinx.coroutines.async
import kotlinx.coroutines.runBlocking
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service


@Service
class SlackHookService(
    private val notificationService: NotificationService,
    private val slackChannelService: SlackChannelService
) {
    private val log = LoggerFactory.getLogger(this::class.java)

    // 채널 ID 로 메세지 전송
    fun sendChannelIdMessage(serverName: String, details: String, isError: Boolean) {
        val blocks = createMessageBlocks(serverName, details, isError)
        runBlocking {
            val activeChannels = notificationService.getAllNotificationStatus()
            activeChannels.map { channel ->
                async {
                    val request = PostMessageRequest(channel.channelId, blocks)
                    slackChannelService.postMessage(request)
                }
            }.map { it.await() }
        }
    }

    // 슬랙 텍스트 메세지 형식
    private fun createTextMessage(serverName: String, details: String, isError: Boolean): String {
        val emoji = if (isError) ":red_circle:" else ":large_green_circle:"
        val status = if (isError) "Error Detected" else "Recovered"
        val detailsLabel = if (isError) "Error Details" else "Recovery Messages"

        return """
            $emoji ServerHealth $status
            *ServerHealth:* $serverName
            *$detailsLabel:* $details
            *Time:* ${java.time.LocalDateTime.now()}
        """.trimIndent().lines().joinToString("\n") { it.trim() }
    }

    // Slack API postMessage 용 메세지 블럭 형식
    private fun createMessageBlocks(serverName: String, details: String, isError: Boolean): List<Map<String, Any>> {
        val text = createTextMessage(serverName, details, isError)
        // 참고) Slack API 메세지 블록 형식 (https://api.slack.com/reference/block-kit/blocks)
        return listOf(
            mapOf(
                "type" to "section",
                "text" to mapOf(
                    "type" to "mrkdwn",
                    "text" to text
                )
            )
        )
    }

//========== 기존 웹 훅 방식 ============/

    //웹 훅 URL 로 메세지 전송
//    fun sendWebhookMessage(serverName: String, details: String, isError: Boolean) {
//        val message = createTextMessage(serverName, details, isError)
//        sendWebHookMessage(message)
//    }

    // 웹 훅 URL 전송
//    private fun sendWebHookMessage(message: String) {
//        // 전역 알림 설정이 켜져있을 때만
//        if (notificationService.getNotificationStatus().notifications) {
//            val headers = HttpHeaders().apply {
//                contentType = MediaType.APPLICATION_JSON
//            }
//            val payload = mapOf("text" to message)
//            val entity = HttpEntity(objectMapper.writeValueAsString(payload), headers)
//
//            restTemplate.postForEntity(webhookUrl, entity, String::class.java)
//        }
//    }

}
