package health_check.server.model

import jakarta.persistence.*
import java.time.LocalDateTime

@Entity
@Table(name = "server_health")
class ServerHealth(
    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @Column(name = "host_name", unique = true)
    var hostName: String,

    var host: String,

    var port: Int,

    var type: Int,

    // 알림 ON/OFF
    @Column(nullable = false, columnDefinition = "TINYINT(1) DEFAULT 1")
    var notification: Boolean = true,

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    val environment: Environment,

    @Column(name = "created_at")
    val createdAt: LocalDateTime = LocalDateTime.now(),

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime? = null,


    )