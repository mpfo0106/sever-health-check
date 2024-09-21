package health_check.server.controller

import health_check.server.dto.BatchServerDto
import health_check.server.dto.ServerDto
import health_check.server.dto.ServerFilterDto
import health_check.server.model.ServerHealth
import health_check.server.service.ServerHealthService
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
@RequestMapping("/api/servers")
@Tag(name = "Server Health", description = "모니터링하는 서버 관리 API")

class ServerHealthController(private val serverHealthService: ServerHealthService) {
    // 모니터링 하는 모든 서버 찾기
    @Operation(
        summary = "모든 서버 조회",
        description = "모니터링 중인 모든 서버의 정보를 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 서버 목록을 조회함",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerHealth::class))]
    )
    @GetMapping
    fun getAllServers(@ModelAttribute filterDto: ServerFilterDto = ServerFilterDto()): ResponseEntity<List<ServerHealth>> =
        ResponseEntity.ok(serverHealthService.getAllServers(filterDto))

    @Operation(
        summary = "특정 서버 조회",
        description = "지정된 ID의 서버 정보를 조회합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "성공적으로 서버 정보를 조회함",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerHealth::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 서버를 찾을 수 없음"
    )
    @GetMapping("/{id}")
    fun getServerById(@PathVariable id: Long): ResponseEntity<ServerHealth> =
        ResponseEntity.ok(serverHealthService.getServerById(id))

    // 서버 단일 등록
    @Operation(
        summary = "서버 등록",
        description = "새로운 서버를 등록합니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "서버가 성공적으로 등록됨",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerHealth::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터"
    )
    @PostMapping
    fun registerServer(@RequestBody @Valid serverDto: ServerDto): ResponseEntity<ServerHealth> =
        ResponseEntity.status(HttpStatus.CREATED).body(serverHealthService.registerServer(serverDto))

    // 서버 모두 등록
    @Operation(
        summary = "다수 서버 일괄 등록",
        description = "여러 서버를 한 번에 등록합니다."
    )
    @ApiResponse(
        responseCode = "201",
        description = "서버들이 성공적으로 등록됨",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerHealth::class))]
    )
    @ApiResponse(
        responseCode = "400",
        description = "잘못된 요청 데이터"
    )
    @PostMapping("/all")
    fun registerAllServer(@RequestBody @Valid batchServerDto: BatchServerDto): ResponseEntity<List<ServerHealth>> =
        ResponseEntity.status(HttpStatus.CREATED).body(serverHealthService.registerAllServer(batchServerDto.servers))

    // 서버 수정
    @Operation(
        summary = "서버 정보 수정",
        description = "지정된 ID의 서버 정보를 수정합니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "서버 정보가 성공적으로 수정됨",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerHealth::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 서버를 찾을 수 없음"
    )
    @PutMapping("/{id}")
    fun updateServer(
        @PathVariable id: Long,
        @RequestBody @Valid serverDto: ServerDto
    ): ResponseEntity<ServerHealth> {
        val updatedServer = serverHealthService.updateServer(id, serverDto)
        return ResponseEntity.ok(updatedServer)
    }

    // 서버 단일 알림설정 ON/OFF
    @Operation(
        summary = "서버 알림 설정 토글",
        description = "지정된 ID의 서버에 대한 알림 설정을 켜거나 끕니다."
    )
    @ApiResponse(
        responseCode = "200",
        description = "알림 설정이 성공적으로 변경됨",
        content = [Content(mediaType = "application/json", schema = Schema(implementation = ServerHealth::class))]
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 서버를 찾을 수 없음"
    )
    @PatchMapping("/{id}/notification")
    fun toggleNotifications(@PathVariable id: Long): ResponseEntity<ServerHealth> {
        val updatedServer = serverHealthService.toggleNotifications(id)
        return ResponseEntity.ok(updatedServer)
    }

    // 서버 단일 삭제
    @Operation(
        summary = "서버 삭제",
        description = "지정된 ID의 서버를 삭제합니다."
    )
    @ApiResponse(
        responseCode = "204",
        description = "서버가 성공적으로 삭제됨"
    )
    @ApiResponse(
        responseCode = "404",
        description = "지정된 ID의 서버를 찾을 수 없음"
    )
    @DeleteMapping("/{id}")
    fun deleteServer(@PathVariable id: Long): ResponseEntity<Unit> {
        serverHealthService.deleteServer(id)
        return ResponseEntity.noContent().build()
    }

    // 서버 모두 삭제
    @Operation(
        summary = "모든 서버 삭제",
        description = "모든 서버를 삭제합니다."
    )
    @ApiResponse(
        responseCode = "204",
        description = "모든 서버가 성공적으로 삭제됨"
    )
    @DeleteMapping("/all")
    fun deleteAllServers(): ResponseEntity<Unit> {
        serverHealthService.deleteAllServers()
        return ResponseEntity.noContent().build()
    }
}
