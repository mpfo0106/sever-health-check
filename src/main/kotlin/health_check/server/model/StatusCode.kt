package health_check.server.model

enum class StatusCode(val status: String, code: Int) {
    INACTIVE("비활성", 0),
    READY("준비중", 1),
    ACTIVE("활성", 2)
}