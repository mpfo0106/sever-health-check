package health_check.server.repository

import health_check.server.model.ServerHealth
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.stereotype.Repository

@Repository
interface ServerHealthRepository : JpaRepository<ServerHealth, Long>, ServerHealthRepositoryCustom {
    fun findByHostName(hostName: String): ServerHealth?
}