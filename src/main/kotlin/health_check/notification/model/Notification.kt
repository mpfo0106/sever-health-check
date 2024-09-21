package health_check.notification.model

import jakarta.persistence.Column
import jakarta.persistence.Entity
import jakarta.persistence.Id
import jakarta.persistence.Table
import java.time.LocalDateTime

@Entity
@Table(name = "notifications")
class Notification(
    @Id @Column(name = "channel_id", nullable = false)
    var channelId: String,

    @Column(name = "updated_at")
    var updatedAt: LocalDateTime = LocalDateTime.now(),
)