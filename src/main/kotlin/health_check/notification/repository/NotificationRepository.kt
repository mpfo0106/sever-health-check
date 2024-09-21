package health_check.notification.repository

import health_check.notification.model.Notification
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface NotificationRepository : JpaRepository<Notification, String>
