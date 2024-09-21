package health_check.common.util

import health_check.server.model.ServerHealth

fun ServerHealth.isWebSocketServer() = type in listOf(2, 3, 4)

fun ServerHealth.getWebSocketUrl(): String {
    val protocol = if (host.startsWith("https://")) "wss://" else "ws://"
    val hostWithoutProtocol = host.replace("https://", "").replace("http://", "")
    val baseUrl = "$protocol$hostWithoutProtocol"
    return when (type) {
        2 -> "$baseUrl/hub/chat"
        3 -> "$baseUrl/hub/edit"
        4 -> "$baseUrl/hub/game"
        else -> baseUrl
    }

}

