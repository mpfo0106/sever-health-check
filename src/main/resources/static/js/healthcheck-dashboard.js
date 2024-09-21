document.addEventListener('DOMContentLoaded', function () {
    // 서버 자동 추가
    // document.getElementById('autoAddServer').addEventListener('click', autoAddServer);
    document.getElementById('autoAddServer').addEventListener('click', showAutoAddServerError);

    // 서버 수동 추가 모달 표시
    document.getElementById('manualAddServer').addEventListener('click', showAddServerModal);

    // 서버 저장 (추가 또는 수정)
    document.getElementById('saveServer').addEventListener('click', saveServer);

    // 모든 서버 삭제
    document.getElementById('deleteAllServers').addEventListener('click', deleteAllServers);

    // 서버 필터링
    document.getElementById('environmentFilter').addEventListener('change', filterServers);
});


function showAutoAddServerError() {
    alert('죄송합니다. 현재 서버 자동 추가 기능은 제공되지 않습니다.');
}

function showAddServerModal() {
    $('#serverModal').modal('show');
}

function saveServer() {
    const serverId = document.getElementById('serverId').value;
    const serverData = {
        hostName: document.getElementById('hostName').value,
        host: document.getElementById('host').value,
        port: parseInt(document.getElementById('port').value),
        type: parseInt(document.getElementById('type').value),
        environment: document.getElementById('environment').value
    };

    const url = serverId ? `/api/servers/${serverId}` : '/api/servers';
    const method = serverId ? 'PUT' : 'POST';

    fetch(url, {
        method: method,
        headers: {
            'Content-Type': 'application/json',
        },
        body: JSON.stringify(serverData)
    })
        .then(response => {
            if (!response.ok) {
                return response.json().then(err => {
                    throw new Error(err.message || '서버 처리 중 오류가 발생했습니다.')
                });
            }
            return response.json();
        })
        .then(data => {
            alert(serverId ? '서버가 성공적으로 수정되었습니다.' : '새로운 서버가 성공적으로 추가되었습니다.');
            $('#serverModal').modal('hide');
            location.reload();
        })
        .catch((error) => {
            alert(error.message)
        });
}

function toggleMonitoring(id) {
    $.ajax({
        url: '/api/servers/' + encodeURIComponent(id) + '/notification',
        type: 'PATCH',
        success: function (data) {
            var btn = $('#toggleBtn-' + id);
            if (data.notification) {
                btn.text('ON').removeClass('btn-secondary').addClass('btn-success');
            } else {
                btn.text('OFF').removeClass('btn-success').addClass('btn-secondary');
            }
        },
        error: function (error) {
            console.error('Error toggling monitoring:', error);
        }
    });
}


function editServer(id, name) {
    $('#modalTitle').text('서버 수정');
    $('#serverId').val(id);

    fetch(`/api/servers/${id}`)
        .then(response => response.json())
        .then(server => {
            $('#hostName').val(server.hostName);
            $('#host').val(server.host);
            $('#port').val(server.port);
            $('#type').val(server.type);
            $('#environment').val(server.environment);
            $('#serverModal').modal('show');
        })
        .catch(error => {
            console.error('Error fetching server details:', error);
            alert('서버 정보를 가져오는 중 오류가 발생했습니다.');
        });
}

function deleteServer(id) {
    if (confirm('정말로 이 서버를 삭제하시겠습니까?')) {
        fetch(`/api/servers/${id}`, {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    alert('서버가 성공적으로 삭제되었습니다.');
                    location.reload();
                } else {
                    throw new Error('서버 삭제 중 오류가 발생했습니다.');
                }
            })
            .catch((error) => {
                console.error('Error:', error);
                alert('서버 삭제 중 오류가 발생했습니다.');
            });
    }
}

function deleteAllServers() {
    if (confirm('정말로 모든 서버를 삭제하시겠습니까? 이 작업은 되돌릴 수 없습니다.')) {
        fetch('/api/servers/all', {
            method: 'DELETE',
        })
            .then(response => {
                if (response.ok) {
                    alert('모든 서버가 성공적으로 삭제되었습니다.');
                    location.reload();
                } else {
                    throw new Error('서버 삭제 중 오류가 발생했습니다.');
                }
            })
            .catch((error) => {
                console.error('Error:', error);
                alert('서버 삭제 중 오류가 발생했습니다.');
            });
    }
}

function filterServers() {
    const selectedEnvironment = document.getElementById('environmentFilter').value;
    const rows = document.querySelectorAll('table tbody tr');

    rows.forEach(row => {
        const rowEnvironment = row.getAttribute('data-environment');
        if (selectedEnvironment === '' || rowEnvironment === selectedEnvironment) {
            row.style.display = '';
        } else {
            row.style.display = 'none'
        }

    })
}