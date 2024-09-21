package health_check.notification.service

import health_check.common.error.exception.BusinessException
import health_check.common.error.exception.ErrorCode
import health_check.notification.repository.NotificationRepository
import health_check.slack.service.SlackChannelService
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.InjectMocks
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.never
import org.mockito.kotlin.whenever
import kotlin.test.assertEquals

@ExtendWith(MockitoExtension::class)
class NotificationServiceTest {
    @Mock
    private lateinit var slackChannelService: SlackChannelService

    @Mock
    private lateinit var notificationRepository: NotificationRepository

    @InjectMocks
    private lateinit var notificationService: NotificationService

    private val channelIds: List<String> = listOf("C02E1D5E2T1", "C01E1D5E2T2")

    @Test
    @DisplayName("notification 업데이트 성공")
    fun successfulUpdateNotifications() {
        // given
        whenever(slackChannelService.isValidChannelId(any())).thenReturn(true)

        // when
        notificationService.updateNotifications(channelIds)

        // then
        verify(slackChannelService, times(2)).isValidChannelId(any())
        verify(notificationRepository).saveAll(anyList())
    }

    @Test
    @DisplayName("notification 업데이트 실패후 emptyList 반환")
    fun failToUpdateNotifications() {
        // given
        whenever(slackChannelService.isValidChannelId(any())).thenReturn(false)

        // when
        val results = notificationService.updateNotifications(channelIds)

        // then
        assertEquals(0, results.size)
        verify(slackChannelService, times(2)).isValidChannelId(any())
        verify(notificationRepository, never()).saveAll(anyList())
    }

    @Test
    @DisplayName("알림채널 삭제 성공")
    fun successfulDeleteNotificationChannel() {
        // given
        val channelId = channelIds[0]
        whenever(notificationRepository.existsById(channelId)).thenReturn(true)

        // when
        notificationService.deleteNotificationChannel(channelId)

        // then
        verify(notificationRepository).existsById(channelId)
        verify(notificationRepository).deleteById(channelId)
    }

    @Test
    @DisplayName("존재하지않는 ID로 인한 알림채널 삭제 실패")
    fun failToDeleteNotificationChannel() {
        // given
        val channelId = channelIds[0]
        whenever(notificationRepository.existsById(channelId)).thenReturn(false)

        // when
        val exception = assertThrows<BusinessException> {
            notificationService.deleteNotificationChannel(channelId)
        }

        // then
        assertEquals(ErrorCode.SLACK_INVALID_CHANNEL_ID, exception.errorCode)
        verify(notificationRepository).existsById(channelId)
        verify(notificationRepository, never()).deleteById(channelId)
    }
}