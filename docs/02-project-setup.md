# 🚀 Spring Boot 프로젝트 생성 및 기본 설정

## 📝 1단계: 프로젝트 폴더 생성

먼저 프로젝트를 만들 폴더를 만들어요.

### Windows에서:
```cmd
# 원하는 위치로 이동 (예: D:\test)
cd D:\test

# 프로젝트 폴더 생성
mkdir play
cd play
```

### Mac/Linux에서:
```bash
# 원하는 위치로 이동 (예: ~/projects)
cd ~/projects

# 프로젝트 폴더 생성
mkdir play
cd play
```

## 📝 2단계: build.gradle 파일 생성

프로젝트 루트 폴더에 `build.gradle` 파일을 만들어요.

```gradle
plugins {
    id 'java'
    id 'org.springframework.boot' version '3.5.3'
    id 'io.spring.dependency-management' version '1.1.4'
}

group = 'com.example'
version = '0.0.1-SNAPSHOT'

java {
    sourceCompatibility = '17'
}

configurations {
    compileOnly {
        extendsFrom annotationProcessor
    }
}

repositories {
    mavenCentral()
}

dependencies {
    // Spring Boot 기본
    implementation 'org.springframework.boot:spring-boot-starter-web'
    implementation 'org.springframework.boot:spring-boot-starter-thymeleaf'
    
    // 데이터베이스
    implementation 'org.springframework.boot:spring-boot-starter-data-jpa'
    runtimeOnly 'com.h2database:h2'
    
    // 보안
    implementation 'org.springframework.boot:spring-boot-starter-security'
    
    // 개발 도구
    compileOnly 'org.projectlombok:lombok'
    annotationProcessor 'org.projectlombok:lombok'
    developmentOnly 'org.springframework.boot:spring-boot-devtools'
    
    // 테스트
    testImplementation 'org.springframework.boot:spring-boot-starter-test'
    testImplementation 'org.springframework.security:spring-security-test'
}

tasks.named('test') {
    useJUnitPlatform()
}
```

## 📝 3단계: 메인 애플리케이션 클래스 생성

`src/main/java/com/example/demo/PlayApplication.java` 파일을 만들어요.

```java
package com.example.demo;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class PlayApplication {

    public static void main(String[] args) {
        SpringApplication.run(PlayApplication.class, args);
    }

}
```

## 📝 4단계: application.properties 설정

`src/main/resources/application.properties` 파일을 만들어요.

```properties
spring.application.name=play

# H2 데이터베이스 설정 (로컬 테스트용)
spring.datasource.url=jdbc:h2:mem:testdb
spring.datasource.driverClassName=org.h2.Driver
spring.datasource.username=sa
spring.datasource.password=
spring.h2.console.enabled=true
spring.h2.console.path=/h2-console

# JPA 설정
spring.jpa.database-platform=org.hibernate.dialect.H2Dialect
spring.jpa.hibernate.ddl-auto=create-drop
spring.jpa.show-sql=true
spring.jpa.properties.hibernate.format_sql=true

# 서버 포트 (8080 포트 충돌 방지)
server.port=8081

# Spring Security 설정
spring.security.user.name=admin
spring.security.user.password=admin123

# 로깅 설정
logging.level.com.example.demo=DEBUG
logging.level.org.springframework.security=DEBUG
```

## 📝 5단계: 폴더 구조 생성

다음 폴더들을 만들어요:

```
src/
├── main/
│   ├── java/
│   │   └── com/
│   │       └── example/
│   │           └── demo/
│   │               ├── controller/
│   │               ├── model/
│   │               ├── repository/
│   │               ├── service/
│   │               └── config/
│   └── resources/
│       ├── templates/
│       └── static/
└── test/
    └── java/
        └── com/
            └── example/
                └── demo/
```

### Windows에서:
```cmd
mkdir src\main\java\com\example\demo\controller
mkdir src\main\java\com\example\demo\model
mkdir src\main\java\com\example\demo\repository
mkdir src\main\java\com\example\demo\service
mkdir src\main\java\com\example\demo\config
mkdir src\main\resources\templates
mkdir src\main\resources\static
mkdir src\test\java\com\example\demo
```

### Mac/Linux에서:
```bash
mkdir -p src/main/java/com/example/demo/{controller,model,repository,service,config}
mkdir -p src/main/resources/{templates,static}
mkdir -p src/test/java/com/example/demo
```

## 📝 6단계: Gradle Wrapper 생성

프로젝트 루트에 `gradlew` 파일들을 생성해요.

### Windows용 gradlew.bat:
```batch
@rem
@rem Copyright 2015 the original author or authors.
@rem
@rem Licensed under the Apache License, Version 2.0 (the "License");
@rem you may not use this file except in compliance with the License.
@rem You may obtain a copy of the License at
@rem
@rem      https://www.apache.org/licenses/LICENSE-2.0
@rem
@rem Unless required by applicable law or agreed to in writing, software
@rem distributed under the License is distributed on an "AS IS" BASIS,
@rem WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
@rem See the License for the specific language governing permissions and
@rem limitations under the License.
@rem

@if "%DEBUG%" == "" @echo off
@rem ##########################################################################
@rem
@rem  Gradle startup script for Windows
@rem
@rem ##########################################################################

@rem Set local scope for the variables with windows NT shell
if "%OS%"=="Windows_NT" setlocal

set DIRNAME=%~dp0
if "%DIRNAME%" == "" set DIRNAME=.
set APP_BASE_NAME=%~n0
set APP_HOME=%DIRNAME%

@rem Resolve any "." and ".." in APP_HOME to make it shorter.
for %%i in ("%APP_HOME%") do set APP_HOME=%%~fi

@rem Add default JVM options here. You can also use JAVA_OPTS and GRADLE_OPTS to pass JVM options to this script.
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

@rem Find java.exe
if defined JAVA_HOME goto findJavaFromJavaHome

set JAVA_EXE=java.exe
%JAVA_EXE% -version >NUL 2>&1
if "%ERRORLEVEL%" == "0" goto execute

echo.
echo ERROR: JAVA_HOME is not set and no 'java' command could be found in your PATH.
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:findJavaFromJavaHome
set JAVA_HOME=%JAVA_HOME:"=%
set JAVA_EXE=%JAVA_HOME%/bin/java.exe

if exist "%JAVA_EXE%" goto execute

echo.
echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
echo.
echo Please set the JAVA_HOME variable in your environment to match the
echo location of your Java installation.

goto fail

:execute
@rem Setup the command line

set CLASSPATH=%APP_HOME%\gradle\wrapper\gradle-wrapper.jar


@rem Execute Gradle
"%JAVA_EXE%" %DEFAULT_JVM_OPTS% %JAVA_OPTS% %GRADLE_OPTS% "-Dorg.gradle.appname=%APP_BASE_NAME%" -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

:end
@rem End local scope for the variables with windows NT shell
if "%ERRORLEVEL%"=="0" goto mainEnd

:fail
rem Set variable GRADLE_EXIT_CONSOLE if you need the _script_ return code instead of
rem the _cmd_ return code.
if not "" == "%GRADLE_EXIT_CONSOLE%" exit 1
exit /b 1

:mainEnd
if "%OS%"=="Windows_NT" endlocal

:omega
```

### gradle-wrapper.properties:
```properties
distributionBase=GRADLE_USER_HOME
distributionPath=wrapper/dists
distributionUrl=https\://services.gradle.org/distributions/gradle-8.7-bin.zip
networkTimeout=10000
validateDistributionUrl=true
zipStoreBase=GRADLE_USER_HOME
zipStorePath=wrapper/dists
```

## 📝 7단계: 프로젝트 테스트

이제 프로젝트가 제대로 설정되었는지 테스트해요.

### Windows에서:
```cmd
# Gradle 빌드
gradlew build

# 애플리케이션 실행
gradlew bootRun
```

### Mac/Linux에서:
```bash
# Gradle 빌드
./gradlew build

# 애플리케이션 실행
./gradlew bootRun
```

## ✅ 확인사항

프로젝트가 성공적으로 설정되면:

1. **빌드 성공**: `BUILD SUCCESSFUL` 메시지가 나타나요
2. **애플리케이션 시작**: `Started PlayApplication` 메시지가 나타나요
3. **웹 접속**: 브라우저에서 `http://localhost:8081` 접속 가능

## 🚨 문제 해결

### 오류 1: Java 버전 문제
```
Error: Java 17 or higher is required
```
**해결**: Java 17 이상을 설치하세요.

### 오류 2: 포트 충돌
```
Web server failed to start. Port 8081 was already in use.
```
**해결**: `application.properties`에서 `server.port=8082`로 변경하세요.

### 오류 3: Gradle 다운로드 실패
```
Could not download gradle-wrapper.jar
```
**해결**: 인터넷 연결을 확인하고 다시 시도하세요.

## 🚀 다음 단계

프로젝트가 성공적으로 실행되면 다음 문서로 이동하세요:
👉 **[데이터베이스 설정 및 모델 생성](./03-database-setup.md)**

---

## 💡 팁

- **Gradle**: Java 프로젝트를 빌드하는 도구예요
- **Spring Boot**: 웹 애플리케이션을 쉽게 만들 수 있게 도와주는 프레임워크예요
- **H2**: 메모리에 데이터를 저장하는 간단한 데이터베이스예요
- 모든 파일은 **UTF-8** 인코딩으로 저장하세요 