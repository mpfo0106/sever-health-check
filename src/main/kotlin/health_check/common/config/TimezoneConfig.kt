package health_check.common.config

import jakarta.annotation.PostConstruct
import org.springframework.context.annotation.Configuration
import java.util.*

@Configuration
class TimezoneConfig {
    @PostConstruct
    fun init() {
        // KST 로 설정
        TimeZone.setDefault(TimeZone.getTimeZone("Asia/Seoul"))
    }
}