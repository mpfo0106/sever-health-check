package health_check.server.service

import health_check.common.error.exception.BusinessException
import health_check.common.error.exception.ErrorCode
import health_check.server.dto.ServerDto
import health_check.server.model.Environment
import health_check.server.model.ServerHealth
import health_check.server.repository.ServerHealthRepository
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.*
import java.time.LocalDateTime
import java.util.*
import kotlin.test.assertEquals
import kotlin.test.assertNotEquals

@ExtendWith(MockitoExtension::class)
class ServerHealthServiceTest {

    @Mock
    private lateinit var serverHealthRepository: ServerHealthRepository

    @InjectMocks
    private lateinit var serverHealthService: ServerHealthService

    private lateinit var testServers: List<ServerHealth>

    @BeforeEach
    fun setup() {
        testServers = listOf(
            ServerHealth(
                id = 1,
                hostName = "web-server-1",
                host = "https://web1.example.com",
                port = 8080,
                type = 1,
                createdAt = LocalDateTime.parse("2024-07-19T11:32:05.403864"),
                notification = true,
                environment = Environment.DEV
            ),
            ServerHealth(
                id = 2,
                hostName = "websocket-server-1",
                host = "wss://ws1.example.com",
                port = 8081,
                type = 2,
                createdAt = LocalDateTime.parse("2024-07-19T11:32:05.436164"),
                notification = true,
                environment = Environment.STAGE
            ),
            ServerHealth(
                id = 3,
                hostName = "chat-server-1",
                host = "https://chat1.example.com",
                port = 8082,
                type = 3,
                createdAt = LocalDateTime.parse("2024-07-19T11:32:05.452529"),
                notification = true,
                environment = Environment.LIVE
            )
        )
    }

    @Test
    @DisplayName("모든 모니터링 서버를 조회")
    fun getAllServers() {
        // given
        whenever(serverHealthRepository.findAllWithEnvironment(environment = null)).thenReturn(testServers)
        // when
        val result = serverHealthService.getAllServers()
        // then
        assertEquals(3, result.size)
        assertEquals("web-server-1", result[0].hostName)
        assertEquals("websocket-server-1", result[1].hostName)
        assertEquals("chat-server-1", result[2].hostName)
    }

    @Test
    @DisplayName("중복된 서버를 등록")
    fun registerDuplicateServer() {
        // given
        val server = testServers[0]
        val duplicateServerDto = ServerDto(server.hostName, server.host, server.port, server.type, server.environment)
        whenever(serverHealthRepository.findByHostName(duplicateServerDto.trimmedHostName)).thenReturn(testServers[0])

        // when
        val exception = assertThrows<BusinessException> {
            serverHealthService.registerServer(duplicateServerDto)
        }
        // then
        assertEquals(ErrorCode.SERVER_ALREADY_EXISTS, exception.errorCode)
    }

    @Test
    @DisplayName("중복되지 않은 서버를 등록")
    fun registerNonDuplicateServer() {
        // given
        val newServer = ServerHealth(
            id = 4,
            hostName = "api-server-1",
            host = "https://api1.example.com",
            port = 8083,
            type = 4,
            createdAt = LocalDateTime.now(),
            notification = true,
            environment = Environment.DEV
        )
        val newServerDto =
            ServerDto(newServer.hostName, newServer.host, newServer.port, newServer.type, newServer.environment)
        whenever(serverHealthRepository.findByHostName(newServerDto.trimmedHostName)).thenReturn(null)
        whenever(serverHealthRepository.save(any())).thenReturn(newServer)

        // when
        val result = serverHealthService.registerServer(newServerDto)

        // then
        assertEquals(newServer.host, result.host)
        assertEquals(newServer.hostName, result.hostName)
        assertEquals(newServer.port, result.port)
    }

    @Test
    @DisplayName("존재하는 서버에 대한 모니터링 서버 수정")
    fun updateExistingServer() {
        // given
        val existingServer = testServers[0]
        val updatedServerDto = ServerDto(
            hostName = "updated-web-server",
            host = "https://updated-web.example.com",
            port = 8084,
            type = 2,
            environment = Environment.STAGE
        )

        whenever(serverHealthRepository.findById(existingServer.id)).thenReturn(Optional.of(existingServer))
        whenever(serverHealthRepository.save(any())).thenAnswer { invocation -> invocation.arguments[0] }

        // when
        val result = serverHealthService.updateServer(existingServer.id, updatedServerDto)

        // then
        assertEquals(updatedServerDto.trimmedHostName, result.hostName)
        assertEquals(updatedServerDto.port, result.port)
        assertEquals(updatedServerDto.type, result.type)

        verify(serverHealthRepository).save(any())
    }

    @Test
    @DisplayName("존재하지 않는 서버에 대한 모니터링 서버 수정")
    fun updateNonExistingServer() {
        // given
        val nonExistingServerId = 999L
        val updatedServerDto = ServerDto(
            hostName = "non-existing-server",
            host = "https://non-existing.example.com",
            port = 8085,
            type = 1,
            environment = Environment.DEV
        )

        whenever(serverHealthRepository.findById(nonExistingServerId)).thenReturn(Optional.empty())

        // when
        val exception = assertThrows<BusinessException> {
            serverHealthService.updateServer(nonExistingServerId, updatedServerDto)
        }

        // then
        assertEquals(ErrorCode.SERVER_NOT_FOUND, exception.errorCode)
        verify(serverHealthRepository, never()).save(any())
    }

    @Test
    @DisplayName("존재하는 서버 알림 설정을 ON/OFF")
    fun toggleNotificationsExistingServer() {
        // given
        val existingServer = testServers[0]
        val initialNotification = existingServer.notification
        whenever(serverHealthRepository.findById(existingServer.id)).thenReturn(Optional.of(existingServer))
        whenever(serverHealthRepository.save(any())).thenAnswer { invocation -> invocation.arguments[0] }

        // when
        val result = serverHealthService.toggleNotifications(existingServer.id)

        // then
        assertNotEquals(initialNotification, result.notification)
        verify(serverHealthRepository).save(any())
    }

    @Test
    @DisplayName("존재하지 않는 서버 알림 설정을 ON/OFF")
    fun toggleNotificationsNonExistingServer() {
        // given
        val nonExistingServerId = 999L
        whenever(serverHealthRepository.findById(nonExistingServerId)).thenReturn(Optional.empty())

        // when
        val exception = assertThrows<BusinessException> {
            serverHealthService.toggleNotifications(nonExistingServerId)
        }

        // then
        assertEquals(ErrorCode.SERVER_NOT_FOUND, exception.errorCode)
        verify(serverHealthRepository, never()).save(any())
    }

    @Test
    @DisplayName("존재하는 서버 삭제")
    fun deleteExistingServer() {
        // given
        val existingServer = testServers[0]
        whenever(serverHealthRepository.existsById(existingServer.id)).thenReturn(true)
        doNothing().whenever(serverHealthRepository).deleteById(existingServer.id)

        // when
        serverHealthService.deleteServer(existingServer.id)

        // then
        verify(serverHealthRepository).existsById(existingServer.id)
        verify(serverHealthRepository).deleteById(existingServer.id)
    }

    @Test
    @DisplayName("존재하지 않는 서버 삭제")
    fun deleteNonExistingServer() {
        // given
        val nonExistingServerId = 999L
        whenever(serverHealthRepository.existsById(nonExistingServerId)).thenReturn(false)

        // when
        val exception = assertThrows<BusinessException> {
            serverHealthService.deleteServer(nonExistingServerId)
        }

        // then
        assertEquals(ErrorCode.SERVER_NOT_FOUND, exception.errorCode)
        verify(serverHealthRepository).existsById(nonExistingServerId)
        verify(serverHealthRepository, never()).deleteById(nonExistingServerId)
    }
}