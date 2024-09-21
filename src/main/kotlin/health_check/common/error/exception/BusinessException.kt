package health_check.common.error.exception

open class BusinessException(val errorCode: ErrorCode) : RuntimeException(errorCode.message)

