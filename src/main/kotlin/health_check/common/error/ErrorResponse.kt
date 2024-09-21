package health_check.common.error

import health_check.common.error.exception.ErrorCode

data class ErrorResponse(
    val code: String,
    val message: String,
) {
    constructor(errorCode: ErrorCode) : this(errorCode.code, errorCode.message)
}