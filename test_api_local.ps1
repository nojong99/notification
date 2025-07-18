# 로컬 테스트용 API 스크립트
param(
    [string]$BaseUrl = "http://localhost:8080"
)

Write-Host "========================================" -ForegroundColor Green
Write-Host "알림 시스템 로컬 API 테스트" -ForegroundColor Green
Write-Host "========================================" -ForegroundColor Green

# 서버 상태 확인
function Test-ServerHealth {
    Write-Host "`n1. 서버 상태 확인..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/test/health" -Method GET
        Write-Host "서버 상태: $($response.status)" -ForegroundColor Green
        Write-Host "메시지: $($response.message)" -ForegroundColor Green
        return $true
    }
    catch {
        Write-Host "서버에 연결할 수 없습니다: $($_.Exception.Message)" -ForegroundColor Red
        return $false
    }
}

# 기본 정보 조회
function Get-ServerInfo {
    Write-Host "`n2. 서버 정보 조회..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/test/info" -Method GET
        Write-Host "애플리케이션: $($response.application)" -ForegroundColor Green
        Write-Host "버전: $($response.version)" -ForegroundColor Green
        Write-Host "Java 버전: $($response.'java.version')" -ForegroundColor Green
        Write-Host "OS: $($response.'os.name')" -ForegroundColor Green
    }
    catch {
        Write-Host "서버 정보 조회 실패: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Hello 메시지 테스트
function Test-HelloMessage {
    Write-Host "`n3. Hello 메시지 테스트..." -ForegroundColor Yellow
    try {
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/test/hello" -Method GET
        Write-Host "응답: $response" -ForegroundColor Green
    }
    catch {
        Write-Host "Hello 테스트 실패: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# Echo 테스트
function Test-Echo {
    Write-Host "`n4. Echo 테스트..." -ForegroundColor Yellow
    try {
        $testData = @{
            "name" = "테스트 사용자"
            "message" = "안녕하세요!"
            "timestamp" = Get-Date -Format "yyyy-MM-dd HH:mm:ss"
        }
        
        $response = Invoke-RestMethod -Uri "$BaseUrl/api/test/echo" -Method POST -Body ($testData | ConvertTo-Json) -ContentType "application/json"
        Write-Host "요청 데이터: $($response.data | ConvertTo-Json)" -ForegroundColor Green
        Write-Host "응답 메시지: $($response.message)" -ForegroundColor Green
    }
    catch {
        Write-Host "Echo 테스트 실패: $($_.Exception.Message)" -ForegroundColor Red
    }
}

# H2 콘솔 접속 정보
function Show-H2Console {
    Write-Host "`n5. H2 데이터베이스 콘솔 정보..." -ForegroundColor Yellow
    Write-Host "H2 콘솔 URL: $BaseUrl/h2-console" -ForegroundColor Cyan
    Write-Host "JDBC URL: jdbc:h2:mem:testdb" -ForegroundColor Cyan
    Write-Host "사용자명: sa" -ForegroundColor Cyan
    Write-Host "비밀번호: (비어있음)" -ForegroundColor Cyan
}

# 메인 실행
Write-Host "`n테스트를 시작합니다..." -ForegroundColor Green

if (Test-ServerHealth) {
    Get-ServerInfo
    Test-HelloMessage
    Test-Echo
    Show-H2Console
    
    Write-Host "`n========================================" -ForegroundColor Green
    Write-Host "모든 테스트가 완료되었습니다!" -ForegroundColor Green
    Write-Host "========================================" -ForegroundColor Green
} else {
    Write-Host "`n서버가 실행되지 않았습니다." -ForegroundColor Red
    Write-Host "먼저 'test_local.bat'를 실행하여 서버를 시작해주세요." -ForegroundColor Yellow
}

Write-Host "`n아무 키나 누르면 종료됩니다..." -ForegroundColor Gray
$null = $Host.UI.RawUI.ReadKey("NoEcho,IncludeKeyDown") 