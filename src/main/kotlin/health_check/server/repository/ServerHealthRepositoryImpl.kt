package health_check.server.repository

import com.querydsl.core.types.dsl.BooleanExpression
import com.querydsl.jpa.impl.JPAQueryFactory
import health_check.server.model.Environment
import health_check.server.model.QServerHealth
import health_check.server.model.ServerHealth

class ServerHealthRepositoryImpl(private val queryFactory: JPAQueryFactory) : ServerHealthRepositoryCustom {
    override fun findAllWithEnvironment(environment: Environment?): List<ServerHealth> {
        val serverHealth = QServerHealth.serverHealth

        return queryFactory
            .selectFrom(serverHealth)
            .where(environmentEq(environment))
            .orderBy(serverHealth.hostName.asc())
            .fetch()
    }

    private fun environmentEq(environment: Environment?): BooleanExpression? {
        return environment?.let { QServerHealth.serverHealth.environment.eq(it) }
    }
}