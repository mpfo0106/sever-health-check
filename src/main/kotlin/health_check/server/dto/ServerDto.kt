package health_check.server.dto

import com.fasterxml.jackson.annotation.JsonProperty
import jakarta.validation.constraints.Min
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Positive

data class ServerDto(
    @field:NotBlank(message = "서버 이름은 필수입니다.")
    val hostName: String,

    @field:NotBlank(message = "도메인 값은 필수입니다.")
    val host: String,

    @field:Positive(message = "포트 번호는 양수여야 합니다.")
    @field:Min(value = 1, message = "포트 번호는 0보다 커야 합니다.")
    val port: Int,

    @field:NotNull(message = "타입은 필수입니다.")
    val type: Int,

    @field:NotNull(message = "환경은 필수입니다.")
    @JsonProperty("environment")
    val environment: health_check.server.model.Environment
) {
    val trimmedHostName: String = hostName.trim()
    val trimmedHost: String = host.trim()
}