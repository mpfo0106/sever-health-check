package health_check.common.error

import org.slf4j.LoggerFactory
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.http.ResponseEntity
import org.springframework.web.HttpRequestMethodNotSupportedException
import org.springframework.web.bind.MethodArgumentNotValidException
import org.springframework.web.bind.annotation.ExceptionHandler
import org.springframework.web.bind.annotation.RestControllerAdvice
import org.springframework.web.servlet.NoHandlerFoundException
import health_check.common.error.exception.BusinessException
import health_check.common.error.exception.ErrorCode

@RestControllerAdvice
class GlobalExceptionHandler {
    private val log = LoggerFactory.getLogger(this::class.java)

    @ExceptionHandler(BusinessException::class)
    fun handleBusinessException(e: BusinessException): ResponseEntity<ErrorResponse> {
        log.error("BusinessException: {}", e.message)
        return createErrorResponseEntity(e.errorCode)
    }

    @ExceptionHandler(MethodArgumentNotValidException::class)
    fun handleMethodArgumentNotValidException(e: MethodArgumentNotValidException): ResponseEntity<ErrorResponse> {
        log.error("MethodArgumentNotValidException: {}", e.message)
        return createErrorResponseEntity(ErrorCode.INVALID_INPUT_VALUE)
    }

    @ExceptionHandler(NoHandlerFoundException::class)
    fun handleNoHandlerFoundException(e: NoHandlerFoundException): ResponseEntity<ErrorResponse> {
        log.error("NoHandlerFoundException: {}", e.message)
        return createErrorResponseEntity(ErrorCode.NOT_FOUND)
    }

    @ExceptionHandler(HttpRequestMethodNotSupportedException::class)
    fun handleHttpRequestMethodNotSupportedException(e: HttpRequestMethodNotSupportedException): ResponseEntity<ErrorResponse> {
        log.error("HttpRequestMethodNotSupportedException: {}", e.message)
        return createErrorResponseEntity(ErrorCode.METHOD_NOT_ALLOWED)
    }

    @ExceptionHandler(DataIntegrityViolationException::class)
    fun handleDataIntegrityViolationException(e: DataIntegrityViolationException): ResponseEntity<ErrorResponse> {
        log.error("DataIntegrityViolationException: {}", e.message)
        return createErrorResponseEntity(ErrorCode.DATA_INTEGRITY_VIOLATION)
    }

    @ExceptionHandler(Exception::class)
    fun handleGlobalException(e: Exception): ResponseEntity<ErrorResponse> {
        log.error("Unexpected Exception: {}", e.message)
        return createErrorResponseEntity(ErrorCode.FAILED_INTERNAL_SYSTEM_PROCESSING)
    }

    private fun createErrorResponseEntity(error: ErrorCode): ResponseEntity<ErrorResponse> {
        return ResponseEntity.status(error.status).body(ErrorResponse(error))
    }

}