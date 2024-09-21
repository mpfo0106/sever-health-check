package health_check.common.error.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // System Exception
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C001", "올바르지 않은 입력값입니다."),
    FAILED_INTERNAL_SYSTEM_PROCESSING(
        HttpStatus.INTERNAL_SERVER_ERROR,
        "S001",
        "내부 시스템 처리 작업이 실패했습니다."
    ),
    NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "존재하지 않는 정보입니다."),
    METHOD_NOT_ALLOWED(HttpStatus.METHOD_NOT_ALLOWED, "C003", "올바르지 않는 HTTP 메소드입니다."),
    DATA_INTEGRITY_VIOLATION(HttpStatus.BAD_REQUEST, " C004", "데이터 무결성 위반입니다."),


    // ServerHealth
    SERVER_NOT_FOUND(HttpStatus.NOT_FOUND, "S001", "서버가 존재하지 않습니다."),
    SERVER_REGISTER_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "S002", "서버 추가에 실패했습니다."),
    SERVER_ALREADY_EXISTS(HttpStatus.CONFLICT, "S003", "이미 존재하는 서버이름입니다."),


    // Slack
    SLACK_API_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "SL001", "Slack API 오류가 발생했습니다."),
    SLACK_INVALID_CHANNEL_ID(HttpStatus.BAD_REQUEST, "SL002", "올바르지 않은 Slack 채널 Id 값입니다."),
    SLACK_CHANNEL_NOT_CONFIGURED(HttpStatus.BAD_REQUEST, "SL003", "알림을 받을 Slack 채널이 설정되지 않았습니다."),

    //Slack common error
    SLACK_INVALID_AUTH(HttpStatus.UNAUTHORIZED, "SL100", "Slack 토큰 인증이 실패하였습니다."),
    SLACK_NAME_TAKEN(HttpStatus.BAD_REQUEST, "SL200", "주어진 이름으로는 채널을 만들 수 없습니다."),
    SLACK_NOT_IN_CHANNEL(HttpStatus.FORBIDDEN, "SL300", "채널 구성원만 이름을 수정할 수 있습니다."),
    SLACK_CHANNEL_NOT_FOUND(HttpStatus.NOT_FOUND, "SL400", "Slack 채널을 찾을 수 없습니다."),
    SLACK_TOO_MANY_ATTACHMENTS(HttpStatus.BAD_REQUEST, "SL500", "첨부 파일이 너무 많습니다.메세지에는 최대 100개의 첨부파일이 허용됩니다."),

    // WebSocket
    WEBSOCKET_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "WS001", "WebSocket 연결에 실패했습니다."),
    WEBSOCKET_UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WS004", "예상치 못한 WebSocket 오류가 발생했습니다."),
    WEBSOCKET_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "WS002", "웹소켓 연결 시간이 초과되었습니다."),
    WEBSOCKET_SSL_ERROR(HttpStatus.BAD_REQUEST, "WS003", "웹소켓 SSL/TLS 연결 오류가 발생했습니다."),
    WEBSOCKET_HANDSHAKE_FAILED(HttpStatus.BAD_REQUEST, "WS005", "웹소켓 핸드셰이크에 실패했습니다."),
    WEBSOCKET_INVALID_URL(HttpStatus.BAD_REQUEST, "WS006", "잘못된 웹소켓 URL입니다."),
    WEBSOCKET_SECURITY_ERROR(HttpStatus.FORBIDDEN, "WS007", "웹소켓 보안 오류가 발생했습니다."),

    WEBSOCKET_ENDPOINT_NOT_FOUND(HttpStatus.NOT_FOUND, "WS010", "웹소켓 엔드포인트를 찾을 수 없습니다."),
    WEBSOCKET_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "WS011", "웹소켓 연결에 대한 인증이 실패했습니다."),
    WEBSOCKET_FORBIDDEN(HttpStatus.FORBIDDEN, "WS012", "웹소켓 연결에 대한 접근이 거부되었습니다."),
    WEBSOCKET_UPGRADE_FAILED(HttpStatus.BAD_REQUEST, "WS013", "HTTP에서 웹소켓으로의 업그레이드가 실패했습니다."),
    WEBSOCKET_DEPLOYMENT_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WS014", "웹소켓 배포 중 오류가 발생했습니다."),

    // WebServer
    WEBSERVER_UNKNOWN_HOST(HttpStatus.SERVICE_UNAVAILABLE, "WEB001", "웹 서버 도메인을 찾을 수 없습니다."),
    WEBSERVER_CONNECTION_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "WEB002", "웹 서버 연결에 실패했습니다."),
    WEBSERVER_NETWORK_ERROR(HttpStatus.SERVICE_UNAVAILABLE, "WEB003", "웹 서버 네트워크 오류가 발생했습니다."),
    WEBSERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WEB004", "웹 서버 처리 중 오류가 발생했습니다."),
    WEBSERVER_UNEXPECTED_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WEB005", "예상치 못한 WebServer 오류가 발생했습니다."),
    WEBSERVER_CONNECTION_REFUSED(HttpStatus.SERVICE_UNAVAILABLE, "WE006", "웹 서버 연결이 거부되었습니다."),
    WEBSERVER_INVALID_URI(HttpStatus.BAD_REQUEST, "WE007", "잘못된 URI 형식입니다"),
    WEBSERVER_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "WEB008", "웹 서버에 대한 인증이 실패했습니다."),
    WEBSERVER_FORBIDDEN(HttpStatus.FORBIDDEN, "WEB009", "웹 서버에 대한 접근이 거부되었습니다."),
    WEBSERVER_NOT_FOUND(HttpStatus.NOT_FOUND, "WEB010", "요청한 리소스를 찾을 수 없습니다."),
    WEBSERVER_CLIENT_ERROR(HttpStatus.BAD_REQUEST, "WEB011", "클라이언트 요청 오류가 발생했습니다."),
    WEBSERVER_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "WEB012", "웹 서버 요청 시간이 초과되었습니다."),
    WEBSERVER_SSL_ERROR(HttpStatus.BAD_REQUEST, "WEB013", "SSL/TLS 연결 오류가 발생했습니다."),
    WEBSERVER_INVALID_RESPONSE(HttpStatus.INTERNAL_SERVER_ERROR, "WEB014", "웹 서버로부터 잘못된 응답을 받았습니다."),
    WEBSERVER_RETRY_FAILED(HttpStatus.SERVICE_UNAVAILABLE, "WEB015", "웹 서버 요청 재시도가 실패했습니다."),
    WEBSERVER_INVALID_ARGUMENT(HttpStatus.BAD_REQUEST, "WEB016", "잘못된 인자가 제공되었습니다."),
    WEBSERVER_IO_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "WEB017", "I/O 오류가 발생했습니다."),

    // Integrated
    HEALTH_CHECK_TIMEOUT(HttpStatus.REQUEST_TIMEOUT, "HC002", "서버 헬스 체크 시간이 초과되었습니다."),
    HEALTH_UNKNOWN_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "HC003", "서버 헬스 체크 중 알 수 없는 오류가 발생했습니다."),

    // Notification
    GLOBAL_NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "GN001", "전역 알림 설정이 존재하지 않습니다."),
    DUPLICATE_CHANNEL_ID(HttpStatus.CONFLICT, " GN002", "중복된 채널ID 값입니다."),

}