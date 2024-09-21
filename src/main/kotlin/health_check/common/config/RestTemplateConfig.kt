package health_check.common.config

import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.web.client.RestTemplate

@Configuration
class RestTemplateConfig {
    // 추후 WebClient 로 변경해보기
    @Bean
    fun restTemplate(): RestTemplate {
        return RestTemplate()
    }
}