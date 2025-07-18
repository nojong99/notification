@echo off
echo ========================================
echo    사용자 알림 시스템 실행 스크립트
echo ========================================
echo.

echo [1/5] Java 버전 확인...
java -version
if %errorlevel% neq 0 (
    echo ❌ Java가 설치되지 않았습니다. Java 17 이상을 설치해주세요.
    pause
    exit /b 1
)
echo ✅ Java 확인 완료
echo.

echo [2/5] MongoDB 연결 확인...
echo MongoDB가 실행 중인지 확인해주세요.
echo Windows 서비스에서 'MongoDB' 서비스가 실행 중이어야 합니다.
echo 또는 MongoDB Compass를 실행해주세요.
echo.
pause

echo [3/5] Kafka 연결 확인...
echo Kafka가 실행 중인지 확인해주세요.
echo Zookeeper와 Kafka 서버가 모두 실행 중이어야 합니다.
echo.
pause

echo [4/5] 프로젝트 빌드...
echo Gradle을 사용하여 프로젝트를 빌드합니다...
call gradlew.bat clean build
if %errorlevel% neq 0 (
    echo ❌ 빌드 실패
    pause
    exit /b 1
)
echo ✅ 빌드 완료
echo.

echo [5/5] 애플리케이션 실행...
echo Spring Boot 애플리케이션을 시작합니다...
echo.
echo 📝 로그를 확인하여 다음 메시지가 나타나는지 확인하세요:
echo    "Started PlayApplication in X.XXX seconds"
echo.
echo 🛑 애플리케이션을 중지하려면 Ctrl+C를 누르세요.
echo.
call gradlew.bat bootRun

pause 