package health_check.slack.dto

data class PostMessageResponse(
    val ok: Boolean,
    val channel: String?,
    val ts: String?,
)