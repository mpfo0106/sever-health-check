package health_check.slack.controller

import health_check.slack.dto.CreateSlackChannelDto
import health_check.slack.dto.RenameSlackChannelDto
import health_check.slack.model.SlackChannel
import health_check.slack.service.SlackChannelService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/slack-channels")
@Tag(name = "Slack Channel", description = "Slack 채널 관리 API")
class SlackChannelController(private val slackChannelService: SlackChannelService) {

    @Operation(
        summary = "모든 Slack 채널 조회",
        description = "등록된 모든 Slack 채널의 정보를 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 채널 목록을 조회함",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SlackChannel::class))]
    )
    @GetMapping
    fun getAllChannels(): ResponseEntity<List<SlackChannel>> {
        val channels = slackChannelService.getAllChannels()
        return ResponseEntity.ok(channels)
    }

    @Operation(
        summary = "새 Slack 채널 생성",
        description = "새로운 Slack 채널을 생성합니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "채널이 성공적으로 생성됨",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SlackChannel::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터"
    )
    @PostMapping
    fun createChannel(
        @Valid @RequestBody createSlackChannelDto: CreateSlackChannelDto
    ): ResponseEntity<SlackChannel> {
        val channel = slackChannelService.createChannel(createSlackChannelDto)
        return ResponseEntity.status(HttpStatus.CREATED).body(channel)
    }


    @Operation(
        summary = "Slack 채널 이름 변경",
        description = "지정된 ID의 Slack 채널 이름을 변경합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "채널 이름이 성공적으로 변경됨",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = SlackChannel::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 채널을 찾을 수 없음"
    )
    @PatchMapping("/{channelId}/rename")
    fun renameChannel(
        @PathVariable channelId: String,
        @RequestBody renameSlackChannelDto: RenameSlackChannelDto
    ): ResponseEntity<SlackChannel> {
        val channel = slackChannelService.renameChannel(channelId, renameSlackChannelDto)
        return ResponseEntity.ok(channel)
    }

    @Operation(
        summary = "Slack 채널 보관 (삭제)",
        description = "지정된 ID의 Slack 채널을 보관(삭제)합니다."
    )
    @ApiResponse(
        responseCode = "204",
        description = "채널이 성공적으로 보관됨"
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 채널을 찾을 수 없음"
    )
    @DeleteMapping("/{channelId}")
    fun archiveChannel(@PathVariable channelId: String): ResponseEntity<Unit> {
        slackChannelService.archiveChannel(channelId)
        return ResponseEntity.noContent().build()
    }
}