package health_check.common.error

import jakarta.websocket.DeploymentException
import health_check.common.error.exception.ErrorCode
import java.net.ConnectException
import java.net.SocketTimeoutException
import java.net.http.WebSocketHandshakeException
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLException

object WebSocketExceptionHandler {
    fun handleWebSocketException(e: Exception): ErrorCode {
        val cause = e.cause ?: e
        return when (cause) {
            is ConnectException -> ErrorCode.WEBSOCKET_CONNECTION_FAILED
            is SocketTimeoutException -> ErrorCode.WEBSOCKET_TIMEOUT
            is SSLException -> ErrorCode.WEBSOCKET_SSL_ERROR
            is DeploymentException -> handleDeploymentException(cause)
            is TimeoutException -> ErrorCode.WEBSOCKET_TIMEOUT
            is WebSocketHandshakeException -> ErrorCode.WEBSOCKET_HANDSHAKE_FAILED
            is IllegalArgumentException -> ErrorCode.WEBSOCKET_INVALID_URL
            is SecurityException -> ErrorCode.WEBSOCKET_SECURITY_ERROR
            else -> ErrorCode.WEBSOCKET_UNEXPECTED_ERROR
        }
    }

    private fun handleDeploymentException(e: DeploymentException): ErrorCode {
        return when {
            e.message?.contains("upgrade") == true -> ErrorCode.WEBSOCKET_UPGRADE_FAILED
            e.message?.contains("401") == true -> ErrorCode.WEBSOCKET_UNAUTHORIZED
            e.message?.contains("403") == true -> ErrorCode.WEBSOCKET_FORBIDDEN
            e.message?.contains("404") == true -> ErrorCode.WEBSOCKET_ENDPOINT_NOT_FOUND
            else -> ErrorCode.WEBSOCKET_DEPLOYMENT_ERROR
        }
    }
}