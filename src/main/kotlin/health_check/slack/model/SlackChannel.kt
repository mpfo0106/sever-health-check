package health_check.slack.model

import com.fasterxml.jackson.annotation.JsonProperty

data class SlackChannel(
    val id: String,
    val name: String,
    // 채널 보관 여부
    @JsonProperty("is_archived")
    val isArchived: Boolean,
    // 공개 비공개 채널 여부
    @JsonProperty("is_channel")
    val isChannel: Boolean
)

