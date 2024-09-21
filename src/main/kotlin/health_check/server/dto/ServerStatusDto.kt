package health_check.server.dto

import health_check.server.model.Environment

data class ServerStatusDto(
    val id: Long,
    val name: String,
    val domain: String,
    val health: String,
    val onStateFrom: String,
    val errorMessage: String?,
    val notification: Boolean,
    val environment: Environment
)