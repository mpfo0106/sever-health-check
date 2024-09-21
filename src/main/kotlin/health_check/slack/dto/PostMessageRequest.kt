package health_check.slack.dto

data class PostMessageRequest(
    val channelId: String,
    val blocks: List<Map<String, Any>>? = null
)

