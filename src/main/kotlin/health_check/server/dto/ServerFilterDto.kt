package health_check.server.dto

import com.fasterxml.jackson.annotation.JsonProperty

data class ServerFilterDto(
    @JsonProperty("environment")
    val environment: health_check.server.model.Environment? = null
)