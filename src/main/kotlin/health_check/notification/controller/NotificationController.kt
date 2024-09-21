package health_check.notification.controller

import health_check.notification.dto.ChannelIdRequest
import health_check.notification.dto.NotificationResponse
import health_check.notification.service.NotificationService
import io.swagger.v3.oas.annotations.Operation
import io.swagger.v3.oas.annotations.media.Content
import io.swagger.v3.oas.annotations.media.Schema
import io.swagger.v3.oas.annotations.responses.ApiResponse
import io.swagger.v3.oas.annotations.tags.Tag
import jakarta.validation.Valid
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*

@RestController
@RequestMapping("/api/notification")
@Tag(name = "Notification", description = "슬랙 채널 알림 설정 관리 API")
class NotificationController(private val notificationService: NotificationService) {
    @Operation(
        summary = "전체 알림 채널 상태 조회",
        description = "등록된 모든 슬랙 채널의 알림 상태를 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 모든 채널의 알림 상태를 조회함",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = NotificationResponse::class)
        )]
    )
    @GetMapping("/status")
    fun getAllNotificationStatus(): ResponseEntity<List<NotificationResponse>> {
        val statuses = notificationService.getAllNotificationStatus()
        val responses = statuses.map { status ->
            NotificationResponse(
                status.channelId,
                status.updatedAt
            )
        }
        return ResponseEntity.ok(responses)
    }


    @Operation(
        summary = "새로운 알림 채널 추가",
        description = "새로운 Slack 채널을 알림 대상으로 추가합니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "성공적으로 새 채널을 추가함",
        content = [Content(
            mediaType = "application/json",
            schema = Schema(implementation = NotificationResponse::class)
        )]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 채널 ID 또는 이미 존재하는 채널"
    )
    @PutMapping("/update")
    fun updateNotificationChannels(@RequestBody @Valid request: ChannelIdRequest): ResponseEntity<List<NotificationResponse>> {
        notificationService.deleteAllNotificationChannel()

        return if (request.channelIds.isNotEmpty()) {
            val updateNotifications = notificationService.updateNotifications(request.channelIds)
            val responses = updateNotifications.map {
                NotificationResponse(it.channelId, it.updatedAt)
            }
            ResponseEntity.ok(responses)
        } else {
            ResponseEntity.noContent().build()
        }
    }

    @Operation(
        summary = "알림 채널 삭제",
        description = "지정된 ID의 Slack 채널을 알림 대상에서 제거합니다."
    )
    @ApiResponse(
        responseCode = "204",
        description = "성공적으로 채널을 삭제함"
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 채널을 찾을 수 없음"
    )
    @DeleteMapping("/{id}")
    fun deleteNotificationChannel(@PathVariable id: String): ResponseEntity<Unit> {
        notificationService.deleteNotificationChannel(id)
        return ResponseEntity.noContent().build()
    }
}