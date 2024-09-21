package health_check.server.repository

import health_check.server.model.Environment
import health_check.server.model.ServerHealth

interface ServerHealthRepositoryCustom {
    fun findAllWithEnvironment(environment: Environment?): List<ServerHealth>
}