document.addEventListener('DOMContentLoaded', function () {
    initializeEventListeners();
});

function initializeEventListeners() {
    document.getElementById('addChannelBtn').addEventListener('click', showAddChannelModal);
    document.getElementById('manageNotificationChannelsBtn').addEventListener('click', toggleNotificationEditMode);
    document.getElementById('saveChannelBtn').addEventListener('click', saveChannel);
    document.getElementById('saveNotificationChannelsBtn').addEventListener('click', updateNotificationChannels);

    document.querySelectorAll('.edit-channel-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            showEditChannelModal(this.getAttribute('data-channel-id'), this.getAttribute('data-channel-name'));
        });
    });

    document.querySelectorAll('.archive-channel-btn').forEach(btn => {
        btn.addEventListener('click', function () {
            archiveChannel(this.getAttribute('data-channel-id'));
        });
    });
}

function showAddChannelModal() {
    showModal('채널 추가', '', '');
}

function showEditChannelModal(channelId, channelName) {
    showModal('채널 수정', channelId, channelName);
}

function showModal(title, channelId, channelName) {
    document.getElementById('modalTitle').textContent = title;
    document.getElementById('channelId').value = channelId;
    document.getElementById('channelName').value = channelName;
    $('#channelModal').modal('show');
}

function toggleNotificationEditMode() {
    const checkboxes = document.querySelectorAll('.notification-checkbox');
    const saveButton = document.getElementById('saveNotificationChannelsBtn');
    const isEditMode = checkboxes[0].style.display === 'none';

    checkboxes.forEach(checkbox => {
        checkbox.style.display = isEditMode ? 'inline' : 'none';
        if (isEditMode) {
            const notificationBadge = checkbox.closest('tr').querySelector('.notification-badge');
            checkbox.checked = notificationBadge !== null;
        }
    });

    saveButton.style.display = isEditMode ? 'inline-block' : 'none';

    document.getElementById('manageNotificationChannelsBtn').textContent =
        isEditMode ? '알림 채널 관리 취소' : '알림 채널 관리';
}

function saveChannel() {
    const channelId = document.getElementById('channelId').value;
    const channelName = document.getElementById('channelName').value;
    const url = channelId ? `/api/slack-channels/${channelId}/rename` : '/api/slack-channels';
    const method = channelId ? 'PATCH' : 'POST';

    fetchWithErrorHandling(url, method, {name: channelName})
        .then(() => {
            alert(channelId ? '채널이 성공적으로 수정되었습니다.' : '새로운 채널이 성공적으로 추가되었습니다.');
            $('#channelModal').modal('hide');
            location.reload();
        });
}

function updateNotificationChannels() {
    const selectedChannels = Array.from(document.querySelectorAll('.notification-checkbox:checked'))
        .map(checkbox => checkbox.getAttribute('data-channel-id'))
        .filter(id => id && id.trim() !== '');  // 빈 문자열 필터링

    fetchWithErrorHandling('/api/notification/update', 'PUT', {channelIds: selectedChannels})
        .then(response => {
            if (response.length === 0) {
                alert('알림 채널이 성공적으로 업데이트되었습니다. 선택된 채널이 없습니다.');
            } else {
                alert('알림 채널이 성공적으로 업데이트되었습니다.');
                console.log('Updated notifications:', response);
            }
            location.reload();
        })
        .catch(error => {
            console.error('Error updating notification channels:', error);
            alert('알림 채널 업데이트 중 오류가 발생했습니다: ' + error.message);
        });
}

function archiveChannel(channelId) {
    if (confirm('정말로 이 채널을 아카이브(삭제)하시겠습니까?')) {
        fetchWithErrorHandling(`/api/slack-channels/${channelId}`, 'DELETE')
            .then(() => {
                alert('채널이 성공적으로 아카이브되었습니다.');
                location.reload();
            });
    }
}

function fetchWithErrorHandling(url, method, body = null) {
    return fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: body ? JSON.stringify(body) : null
    })
        .then(response => {
            if (!response.ok) {
                if (response.status === 204) {
                    return [];
                }
                return response.text().then(text => {
                    throw new Error(text || `HTTP error! status: ${response.status}`);
                });
            }
            return response.text().then(text => {
                try {
                    return text ? JSON.parse(text) : [];
                } catch (e) {
                    console.error("Error parsing JSON:", e);
                    throw new Error("Invalid JSON response from server");
                }
            });
        })
        .catch(error => {
            console.error('Error:', error);
            throw error;
        });
}