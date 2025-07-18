@echo off
echo ========================================
echo 알림 시스템 로컬 테스트 시작
echo ========================================

echo.
echo 1. Java 버전 확인...
java -version
if %errorlevel% neq 0 (
    echo 오류: Java가 설치되지 않았습니다.
    echo Java 17 이상을 설치해주세요.
    pause
    exit /b 1
)

echo.
echo 2. 프로젝트 빌드...
call gradlew.bat clean build -x test
if %errorlevel% neq 0 (
    echo 오류: 빌드에 실패했습니다.
    pause
    exit /b 1
)

echo.
echo 3. 애플리케이션 실행 (테스트 모드)...
echo 테스트 설정으로 실행합니다...
call gradlew.bat bootRun --args='--spring.profiles.active=test'

echo.
echo 애플리케이션이 종료되었습니다.
pause 