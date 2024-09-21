package health_check

import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.runApplication
import org.springframework.scheduling.annotation.EnableScheduling

@SpringBootApplication
@EnableScheduling
class HealthCheckApplication

fun main(args: Array<String>) {
    runApplication<HealthCheckApplication>(*args)
}

