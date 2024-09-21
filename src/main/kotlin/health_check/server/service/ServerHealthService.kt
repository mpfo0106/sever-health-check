package health_check.server.service


import health_check.common.error.exception.BusinessException
import health_check.common.error.exception.ErrorCode
import health_check.server.dto.ServerDto
import health_check.server.dto.ServerFilterDto
import health_check.server.model.ServerHealth
import health_check.server.repository.ServerHealthRepository
import jakarta.validation.Valid
import org.slf4j.LoggerFactory
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.time.LocalDateTime

@Service
class ServerHealthService(private val serverHealthRepository: ServerHealthRepository) {
    private val log = LoggerFactory.getLogger(this::class.java)

    // 모니터링 서버 모두 fetch
    fun getAllServers(filterDto: ServerFilterDto = ServerFilterDto()): List<ServerHealth> {
        log.debug("Fetching all servers")
        return serverHealthRepository.findAllWithEnvironment(filterDto.environment)
            .also { log.debug("Found {} servers", it.size) }
    }

    fun getServerById(id: Long): ServerHealth {
        log.debug("Fetching server with id: {}", id)
        return serverHealthRepository.findById(id).orElseThrow {
            log.error("ServerHealth not found with id: {}", id)
            BusinessException(ErrorCode.SERVER_NOT_FOUND)
        }.also {
            log.debug("Found server: {}", it.hostName)
        }
    }

    // 서버 등록
    @Transactional
    fun registerServer(@Valid serverDto: ServerDto): ServerHealth {
        log.debug("Attempting to register server: {}", serverDto.trimmedHostName)
        checkAndThrowIfServerNameDuplicated(serverDto)
        return try {
            val server = ServerHealth(
                hostName = serverDto.trimmedHostName,
                host = serverDto.trimmedHost,
                port = serverDto.port,
                type = serverDto.type,
                environment = serverDto.environment
            )
            serverHealthRepository.save(server).also {
                log.info("ServerHealth registered successfully: {}", it.hostName)
            }
        } catch (e: Exception) {
            log.error("Unexpected error occurred while registering server: {}", e.message)
            throw BusinessException(ErrorCode.SERVER_REGISTER_FAILED)
        }
    }


    // 서버 모두 등록
    @Transactional
    fun registerAllServer(@Valid serverDtos: List<ServerDto>): List<ServerHealth> {
        return serverDtos.mapNotNull { serverDto ->
            try {
                registerServer(serverDto)
            } catch (e: BusinessException) {
                if (e.errorCode == ErrorCode.SERVER_ALREADY_EXISTS) {
                    log.info("Skipping already existing server: {}", serverDto.trimmedHostName)
                    null
                } else {
                    throw BusinessException(ErrorCode.SERVER_REGISTER_FAILED)
                }
            }

        }
    }

    // 서버 수정
    @Transactional
    fun updateServer(id: Long, @Valid serverDto: ServerDto): ServerHealth {
        log.debug("Attempting to update server with id: {}", id)
        val existingServer = serverHealthRepository.findById(id).orElseThrow {
            log.error("ServerHealth not found with id: {}", id)
            BusinessException(ErrorCode.SERVER_NOT_FOUND)
        }
        if (existingServer.hostName != serverDto.hostName) {
            checkAndThrowIfServerNameDuplicated(serverDto)
        }

        existingServer.apply {
            hostName = serverDto.trimmedHostName
            host = serverDto.trimmedHost
            port = serverDto.port
            type = serverDto.type
            updatedAt = LocalDateTime.now()
        }
        return serverHealthRepository.save(existingServer).also {
            log.info("ServerHealth updated successfully: {}", it.hostName)
        }
    }


    // 서버 알림 ON/OFF 토글
    @Transactional
    fun toggleNotifications(id: Long): ServerHealth {
        log.debug("Attempting to toggle notifications for server with id: {}", id)
        val serverHealth = serverHealthRepository.findById(id).orElseThrow {
            log.error("ServerHealth not found with id: {}", id)
            BusinessException(ErrorCode.SERVER_NOT_FOUND)
        }
        log.info("Notifications toggled for server: {}", serverHealth.hostName)
        serverHealth.notification = !serverHealth.notification
        return serverHealthRepository.save(serverHealth)
    }

    // 서버 삭제
    @Transactional
    fun deleteServer(id: Long) {
        log.debug("Attempting to delete server with id: {}", id)
        if (!serverHealthRepository.existsById(id)) {
            log.error("ServerHealth not found with id: {}", id)
            throw BusinessException(ErrorCode.SERVER_NOT_FOUND)
        }
        serverHealthRepository.deleteById(id)
        log.info("ServerHealth deleted successfully with id: {}", id)
    }

    // 서버 모두 삭제
    @Transactional
    fun deleteAllServers() {
        log.debug("Attempting to delete all servers")
        serverHealthRepository.deleteAll()
        log.info("All ServerHealth deleted successfully")
    }

    private fun checkAndThrowIfServerNameDuplicated(serverDto: ServerDto) {
        val duplicateHostName = serverHealthRepository.findByHostName(serverDto.trimmedHostName)
        if (duplicateHostName != null) {
            log.info("Server already exists: {}", serverDto.trimmedHostName)
            throw BusinessException(ErrorCode.SERVER_ALREADY_EXISTS)
        }
    }
}