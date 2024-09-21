package health_check.server.controller

import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import org.springframework.stereotype.Controller
import org.springframework.ui.Model
import org.springframework.web.bind.annotation.GetMapping
import org.springframework.web.bind.annotation.ResponseBody
import health_check.notification.service.NotificationService
import health_check.server.dto.ServerStatusDto
import health_check.slack.service.SlackChannelService

@Controller
@Tag(name = "Monitor", description = "모니터링 대시보드 관련 API")
class MonitorController(
    private val serverStatusService: health_check.server.service.ServerStatusService,
    private val notificationService: NotificationService,
    private val slackChannelService: SlackChannelService
) {
    @Operation(
        summary = "헬스체크 대시보드 페이지",
        description = "서버 상태를 보여주는 헬스체크 대시보드 페이지를 반환합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 대시보드 페이지를 로드함",
        content = [Content(mediaType = "text/html")]
    )
    @GetMapping
    fun getHealthCheckDashboard(model: Model): String {
        val servers = serverStatusService.getAllServerStatus()
        model.addAttribute("servers", servers)
        return "healthcheck-dashboard"
    }


    @Operation(
        summary = "Slack 채널 대시보드 페이지",
        description = "Slack 채널 정보를 보여주는 대시보드 페이지를 반환합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 Slack 채널 대시보드 페이지를 로드함",
        content = [Content(mediaType = "text/html")]
    )
    @GetMapping("/channel")
    fun getSlackChannelDashboard(model: Model): String {
        val channels = slackChannelService.getAllChannels().sortedBy { it.name }
        val notifications = notificationService.getAllNotificationStatus()
        val notificationChannelIds = notifications.map { it.channelId }


        model.addAttribute("channels", channels)
        model.addAttribute("notifications", notificationChannelIds)
        return "slackchannel-dashboard"
    }

    @Operation(
        summary = "모든 서버 상태 조회",
        description = "모든 서버의 현재 상태 정보를 JSON 형식으로 반환합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 모든 서버의 상태 정보를 조회함",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerStatusDto::class))]
    )
    @GetMapping("/status")
    @ResponseBody
    fun getAllServerStatus(): List<ServerStatusDto> {
        return serverStatusService.getAllServerStatus()
    }


}