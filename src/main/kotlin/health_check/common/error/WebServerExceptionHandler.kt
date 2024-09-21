package health_check.common.error

import org.springframework.http.HttpStatus
import org.springframework.http.converter.HttpMessageNotReadableException
import org.springframework.web.client.HttpClientErrorException
import org.springframework.web.client.HttpServerErrorException
import org.springframework.web.client.ResourceAccessException
import org.springframework.web.client.RestClientException
import health_check.common.error.exception.ErrorCode
import java.io.IOException
import java.net.ConnectException
import java.net.HttpRetryException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import javax.net.ssl.SSLException

object WebServerExceptionHandler {
    fun handleWebServerException(e: Exception): ErrorCode = when (e) {
        is HttpServerErrorException -> ErrorCode.WEBSERVER_ERROR
        is HttpClientErrorException -> handleHttpClientError(e)
        is ResourceAccessException -> handleResourceAccessException(e)
        is RestClientException -> handleRestClientException(e)
        is IllegalArgumentException -> handleIllegalArgumentException(e)
        is IOException -> ErrorCode.WEBSERVER_IO_ERROR
        else -> ErrorCode.WEBSERVER_UNEXPECTED_ERROR
    }

    private fun handleIllegalArgumentException(e: Exception) =
        if (e.message!!.contains("URI is not absolute") == true) {
            ErrorCode.WEBSERVER_INVALID_URI
        } else {
            ErrorCode.WEBSERVER_INVALID_ARGUMENT
        }

    private fun handleRestClientException(e: Exception) = when (e) {
        is HttpMessageNotReadableException -> ErrorCode.WEBSERVER_INVALID_RESPONSE
        is HttpRetryException -> ErrorCode.WEBSERVER_RETRY_FAILED
        else -> ErrorCode.WEBSERVER_CLIENT_ERROR
    }

    private fun handleResourceAccessException(e: Exception) = when (e.cause) {
        is UnknownHostException -> ErrorCode.WEBSERVER_UNKNOWN_HOST
        is ConnectException -> ErrorCode.WEBSERVER_CONNECTION_REFUSED
        is SocketTimeoutException -> ErrorCode.WEBSERVER_TIMEOUT
        is SSLException -> ErrorCode.WEBSERVER_SSL_ERROR
        else -> ErrorCode.WEBSERVER_NETWORK_ERROR
    }

    private fun handleHttpClientError(e: HttpClientErrorException) = when (e.statusCode) {
        HttpStatus.UNAUTHORIZED -> ErrorCode.WEBSERVER_UNAUTHORIZED
        HttpStatus.FORBIDDEN -> ErrorCode.WEBSERVER_FORBIDDEN
        HttpStatus.NOT_FOUND -> ErrorCode.WEBSERVER_NOT_FOUND
        else -> ErrorCode.WEBSERVER_CLIENT_ERROR
    }
}