# 사용자 알림 시스템 API 테스트 스크립트
# PowerShell에서 실행하세요

Write-Host "========================================" -ForegroundColor Green
Write-Host "    사용자 알림 시스템 API 테스트" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""

# 기본 URL
$baseUrl = "http://localhost:8080/api"

# 색상 함수
function Write-Success { param($message) Write-Host "✅ $message" -ForegroundColor Green }
function Write-Error { param($message) Write-Host "❌ $message" -ForegroundColor Red }
function Write-Info { param($message) Write-Host "ℹ️  $message" -ForegroundColor Cyan }

# 1. 사용자 생성 테스트
Write-Host "1️⃣ 사용자 생성 테스트" -ForegroundColor Yellow
$userData = @{
    username = "testuser"
    email = "test@example.com"
    phoneNumber = "010-1234-5678"
} | ConvertTo-Json

try {
    $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -Body $userData -ContentType "application/json"
    Write-Success "사용자 생성 성공"
    Write-Host "생성된 사용자 ID: $($response.id)" -ForegroundColor Green
    $userId = $response.id
} catch {
    Write-Error "사용자 생성 실패: $($_.Exception.Message)"
    exit 1
}

Write-Host ""

# 2. 사용자 목록 조회 테스트
Write-Host "2️⃣ 사용자 목록 조회 테스트" -ForegroundColor Yellow
try {
    $users = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
    Write-Success "사용자 목록 조회 성공"
    Write-Host "총 사용자 수: $($users.Count)" -ForegroundColor Green
} catch {
    Write-Error "사용자 목록 조회 실패: $($_.Exception.Message)"
}

Write-Host ""

# 3. 특정 사용자 조회 테스트
Write-Host "3️⃣ 특정 사용자 조회 테스트" -ForegroundColor Yellow
try {
    $user = Invoke-RestMethod -Uri "$baseUrl/users/$userId" -Method GET
    Write-Success "사용자 조회 성공"
    Write-Host "사용자명: $($user.username)" -ForegroundColor Green
    Write-Host "이메일: $($user.email)" -ForegroundColor Green
} catch {
    Write-Error "사용자 조회 실패: $($_.Exception.Message)"
}

Write-Host ""

# 4. 알림 생성 테스트
Write-Host "4️⃣ 알림 생성 테스트" -ForegroundColor Yellow
$notificationData = @{
    userId = $userId
    title = "환영합니다!"
    message = "알림 시스템에 가입해주셔서 감사합니다."
    type = "WELCOME"
} | ConvertTo-Json

try {
    $notification = Invoke-RestMethod -Uri "$baseUrl/notifications" -Method POST -Body $notificationData -ContentType "application/json"
    Write-Success "알림 생성 성공"
    Write-Host "생성된 알림 ID: $($notification.id)" -ForegroundColor Green
    $notificationId = $notification.id
} catch {
    Write-Error "알림 생성 실패: $($_.Exception.Message)"
}

Write-Host ""

# 5. 사용자별 알림 조회 테스트
Write-Host "5️⃣ 사용자별 알림 조회 테스트" -ForegroundColor Yellow
try {
    $notifications = Invoke-RestMethod -Uri "$baseUrl/users/$userId/notifications" -Method GET
    Write-Success "사용자별 알림 조회 성공"
    Write-Host "총 알림 수: $($notifications.Count)" -ForegroundColor Green
} catch {
    Write-Error "사용자별 알림 조회 실패: $($_.Exception.Message)"
}

Write-Host ""

# 6. 알림 상태 변경 테스트
Write-Host "6️⃣ 알림 상태 변경 테스트" -ForegroundColor Yellow
$statusData = @{
    status = "READ"
} | ConvertTo-Json

try {
    $updatedNotification = Invoke-RestMethod -Uri "$baseUrl/notifications/$notificationId/status" -Method PUT -Body $statusData -ContentType "application/json"
    Write-Success "알림 상태 변경 성공"
    Write-Host "새로운 상태: $($updatedNotification.status)" -ForegroundColor Green
} catch {
    Write-Error "알림 상태 변경 실패: $($_.Exception.Message)"
}

Write-Host ""

# 7. 고급 테스트: 다중 사용자 생성
Write-Host "7️⃣ 다중 사용자 생성 테스트" -ForegroundColor Yellow
$testUsers = @(
    @{ username = "user1"; email = "user1@example.com"; phoneNumber = "010-1111-1111" },
    @{ username = "user2"; email = "user2@example.com"; phoneNumber = "010-2222-2222" },
    @{ username = "user3"; email = "user3@example.com"; phoneNumber = "010-3333-3333" }
)

$createdUserIds = @()

foreach ($user in $testUsers) {
    try {
        $userJson = $user | ConvertTo-Json
        $response = Invoke-RestMethod -Uri "$baseUrl/users" -Method POST -Body $userJson -ContentType "application/json"
        $createdUserIds += $response.id
        Write-Host "사용자 생성: $($user.username)" -ForegroundColor Green
    } catch {
        Write-Error "사용자 생성 실패 ($($user.username)): $($_.Exception.Message)"
    }
}

Write-Host "생성된 사용자 수: $($createdUserIds.Count)" -ForegroundColor Green

Write-Host ""

# 8. 시스템 상태 요약
Write-Host "8️⃣ 시스템 상태 요약" -ForegroundColor Yellow
try {
    $allUsers = Invoke-RestMethod -Uri "$baseUrl/users" -Method GET
    $allNotifications = @()
    
    foreach ($user in $allUsers) {
        try {
            $userNotifications = Invoke-RestMethod -Uri "$baseUrl/users/$($user.id)/notifications" -Method GET
            $allNotifications += $userNotifications
        } catch {
            # 알림이 없는 경우 무시
        }
    }
    
    Write-Success "시스템 상태 확인 완료"
    Write-Host "총 사용자 수: $($allUsers.Count)" -ForegroundColor Green
    Write-Host "총 알림 수: $($allNotifications.Count)" -ForegroundColor Green
    
    # 알림 상태별 통계
    $statusStats = $allNotifications | Group-Object status | ForEach-Object {
        Write-Host "  - $($_.Name): $($_.Count)개" -ForegroundColor Gray
    }
    
} catch {
    Write-Error "시스템 상태 확인 실패: $($_.Exception.Message)"
}

Write-Host ""
Write-Host "========================================" -ForegroundColor Green
Write-Host "    모든 테스트 완료!" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green
Write-Host ""
Write-Host "📝 다음 단계:" -ForegroundColor Cyan
Write-Host "1. MongoDB Compass에서 데이터 확인" -ForegroundColor White
Write-Host "2. Kafka 토픽에서 메시지 확인" -ForegroundColor White
Write-Host "3. 애플리케이션 로그 확인" -ForegroundColor White
Write-Host ""

Read-Host "계속하려면 Enter를 누르세요" 