package health_check.slack.dto

import jakarta.validation.constraints.Pattern
import jakarta.validation.constraints.Size

class RenameSlackChannelDto(
    @field:Size(max = 80, message = "채널 이름은 80자를 초과할 수 없습니다.")
    @field:Pattern(regexp = "^[a-z0-9-_]+$", message = "채널 이름은 소문자, 숫자, 하이픈, 언더스코어만 포함할 수 있습니다.")
    val name: String

)