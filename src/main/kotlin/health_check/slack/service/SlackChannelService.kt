package health_check.slack.service

import com.fasterxml.jackson.module.kotlin.jacksonObjectMapper
import com.fasterxml.jackson.module.kotlin.readValue
import org.slf4j.LoggerFactory
import org.springframework.beans.factory.annotation.Value
import org.springframework.http.HttpEntity
import org.springframework.http.HttpHeaders
import org.springframework.http.HttpMethod
import org.springframework.http.MediaType
import org.springframework.stereotype.Service
import org.springframework.util.LinkedMultiValueMap
import org.springframework.web.client.RestTemplate
import org.springframework.web.util.UriComponentsBuilder
import health_check.common.error.exception.BusinessException
import health_check.common.error.exception.ErrorCode
import health_check.slack.constants.SlackApiConstants
import health_check.slack.dto.CreateSlackChannelDto
import health_check.slack.dto.PostMessageRequest
import health_check.slack.dto.PostMessageResponse
import health_check.slack.dto.RenameSlackChannelDto
import health_check.slack.model.SlackChannel

@Service
class SlackChannelService(private val restTemplate: RestTemplate) {
    private val log = LoggerFactory.getLogger(this::class.java)

    @Value("\${slack.bot-token}")
    private lateinit var slackBotToken: String

    private val objectMapper = jacksonObjectMapper()

    private fun getHeaders(): HttpHeaders {
        return HttpHeaders().apply {
            contentType = MediaType.APPLICATION_JSON
            set("Authorization", "Bearer $slackBotToken")
        }
    }

    // 입력된 channelId 가 실제 존재하는지 검증
    fun isValidChannelId(channelId: String): Boolean {
        val channels = getAllChannels()
        return channels.any { it.id == channelId }
    }

    // 모든 slack 채널 조회
    fun getAllChannels(exclude_archived: Boolean = true, limit: Int = 1000): List<SlackChannel> {
        val entity = HttpEntity<String>(getHeaders())
        // 조회 1000건, archived exclude true 옵션 적용
        val uriBuilder = UriComponentsBuilder.fromUriString(SlackApiConstants.LIST_CHANNELS)
            .queryParam("exclude_archived", exclude_archived).queryParam("limit", limit)
        // restTemplate 에 인코딩 안된 String 형식으로 제공
        val uri = uriBuilder.build().toUriString()
        return executeSlackApiCall(
            uri,
            HttpMethod.GET,
            entity
        ) { response ->
            (response["channels"] as? List<Map<String, Any>>)?.mapNotNull { channel ->
                SlackChannel(
                    id = channel["id"] as String,
                    name = channel["name"] as String,
                    isChannel = channel["is_channel"] as Boolean,
                    isArchived = channel["is_archived"] as Boolean
                )
            }
        } ?: emptyList()
    }


    // 채널에 메세지 전송(postMessage api)
    fun postMessage(postMessageRequest: PostMessageRequest): PostMessageResponse {
        val body = mapOf(
            "channel" to postMessageRequest.channelId,
            "blocks" to postMessageRequest.blocks
        )
        val request = HttpEntity(body, getHeaders())

        return executeSlackApiCall(SlackApiConstants.POST_MESSAGE, HttpMethod.POST, request) { response ->
            PostMessageResponse(
                ok = response["ok"] as Boolean,
                channel = response["channel"] as String,
                ts = response["ts"] as String,
            )
        }
    }


    // 채널 생성
    fun createChannel(createSlackChannelDto: CreateSlackChannelDto): SlackChannel {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("name", createSlackChannelDto.name)
            add("is_private", createSlackChannelDto.isPrivate.toString())
        }
        val request = HttpEntity(body, getHeaders())

        return executeSlackApiCall(SlackApiConstants.CREATE_CHANNEL, HttpMethod.POST, request) { response ->
            createSlackChannelFromResponse(response["channel"] as? Map<String, Any>)
        }
    }

    // 채널 수정(이름 변경)
    fun renameChannel(channelId: String, renameSlackChannelDto: RenameSlackChannelDto): SlackChannel {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("channel", channelId)
            add("name", renameSlackChannelDto.name)
        }

        val request = HttpEntity(body, getHeaders())

        return executeSlackApiCall(SlackApiConstants.RENAME_CHANNEL, HttpMethod.POST, request) { response ->
            createSlackChannelFromResponse(response["channel"] as? Map<String, Any>)
        }

    }


    // 채널 보관(삭제)
    fun archiveChannel(channelId: String) {
        val body = LinkedMultiValueMap<String, String>().apply {
            add("channel", channelId)
        }
        val request = HttpEntity(body, getHeaders())
        executeSlackApiCall(SlackApiConstants.ARCHIVE_CHANNEL, HttpMethod.POST, request) { }
    }

    private fun <T> executeSlackApiCall(
        url: String,
        method: HttpMethod,
        enity: HttpEntity<*>,
        responseHandler: (Map<String, Any>) -> T
    ): T {
        val response = restTemplate.exchange(url, method, enity, String::class.java)

        if (!response.statusCode.is2xxSuccessful) {
            log.error("Slack API HTTP error: ${response.statusCode}")
            throw BusinessException(ErrorCode.SLACK_API_ERROR)
        }

        val jsonResponse: Map<String, Any> = objectMapper.readValue(response.body!!)
        if (jsonResponse["ok"] as? Boolean != true) {
            val error = jsonResponse["error"] as String
            log.error("Slack API Error: $error")

            // 각 대표 에러들
            throw when (error) {
                "invalid_auth" -> BusinessException(ErrorCode.SLACK_INVALID_AUTH)
                "name_taken" -> BusinessException(ErrorCode.SLACK_NAME_TAKEN)
                "not_in_channel" -> BusinessException(ErrorCode.SLACK_NOT_IN_CHANNEL)
                "channel_not_found" -> BusinessException(ErrorCode.SLACK_CHANNEL_NOT_FOUND)
                "too_many_attachments" -> BusinessException(ErrorCode.SLACK_TOO_MANY_ATTACHMENTS)
                else -> BusinessException(ErrorCode.SLACK_API_ERROR)
            }
        }
        log.debug("Slack API call successful: $url")
        return responseHandler(jsonResponse)
    }

    private fun createSlackChannelFromResponse(channelData: Map<String, Any>?): SlackChannel {
        return SlackChannel(
            id = channelData!!["id"] as String,
            name = channelData["name"] as String,
            isChannel = channelData["is_channel"] as Boolean,
            isArchived = channelData["is_archived"] as Boolean,
        )
    }
}

