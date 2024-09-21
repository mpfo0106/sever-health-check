package health_check.server.dto

import jakarta.validation.Valid
import jakarta.validation.constraints.NotEmpty

data class BatchServerDto(
    @field:NotEmpty
    @field:Valid
    val servers: List<ServerDto>
)