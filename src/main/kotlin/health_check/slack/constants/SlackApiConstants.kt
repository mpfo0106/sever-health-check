package health_check.slack.constants

object SlackApiConstants {
    const val BASE_URL = "https://slack.com/api"
    const val LIST_CHANNELS = "$BASE_URL/conversations.list"
    const val CREATE_CHANNEL = "$BASE_URL/conversations.create"
    const val RENAME_CHANNEL = "$BASE_URL/conversations.rename"
    const val ARCHIVE_CHANNEL = "$BASE_URL/conversations.archive"
    const val POST_MESSAGE = "$BASE_URL/chat.postMessage"
}