-- 알림 시스템 데이터베이스 쿼리문
-- H2 데이터베이스용

-- ========================================
-- 테이블 생성 (JPA가 자동으로 생성하지만 수동으로도 가능)
-- ========================================

-- 사용자 테이블
CREATE TABLE IF NOT EXISTS USERS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USERNAME VARCHAR(50) NOT NULL UNIQUE,
    EMAIL VARCHAR(100) NOT NULL UNIQUE,
    PHONE_NUMBER VARCHAR(20),
    ACTIVE BOOLEAN DEFAULT TRUE
);

-- 알림 테이블
CREATE TABLE IF NOT EXISTS NOTIFICATIONS (
    ID BIGINT AUTO_INCREMENT PRIMARY KEY,
    USER_ID VARCHAR(50) NOT NULL,
    TITLE VARCHAR(100) NOT NULL,
    MESSAGE TEXT NOT NULL,
    TYPE VARCHAR(20) NOT NULL,
    STATUS VARCHAR(20) DEFAULT 'PENDING',
    CREATED_AT TIMESTAMP DEFAULT CURRENT_TIMESTAMP,
    SENT_AT TIMESTAMP NULL
);

-- ========================================
-- 샘플 데이터 삽입
-- ========================================

-- 기본 관리자 계정 추가
INSERT INTO USERS (USERNAME, EMAIL, PHONE_NUMBER, ACTIVE) 
VALUES ('admin', 'admin@example.com', '010-1234-5678', true);

-- 추가 사용자들 (선택사항)
INSERT INTO USERS (USERNAME, EMAIL, PHONE_NUMBER, ACTIVE) 
VALUES 
    ('user1', 'user1@example.com', '010-1111-2222', true),
    ('user2', 'user2@example.com', '010-3333-4444', true),
    ('user3', 'user3@example.com', '010-5555-6666', true),
    ('manager', 'manager@example.com', '010-7777-8888', true);

-- 샘플 알림 데이터
INSERT INTO NOTIFICATIONS (USER_ID, TITLE, MESSAGE, TYPE, STATUS, CREATED_AT) 
VALUES 
    ('admin', '시스템 시작', '알림 시스템이 성공적으로 시작되었습니다.', 'INFO', 'SENT', CURRENT_TIMESTAMP),
    ('user1', '환영 메시지', 'user1님, 알림 시스템에 오신 것을 환영합니다!', 'INFO', 'PENDING', CURRENT_TIMESTAMP),
    ('user2', '시스템 점검 안내', '2024년 1월 15일 오전 2시부터 4시까지 시스템 점검이 예정되어 있습니다.', 'WARNING', 'PENDING', CURRENT_TIMESTAMP);

-- ========================================
-- 조회 쿼리
-- ========================================

-- 모든 사용자 조회
SELECT * FROM USERS ORDER BY ID;

-- 활성 사용자만 조회
SELECT * FROM USERS WHERE ACTIVE = true ORDER BY USERNAME;

-- 사용자 수 통계
SELECT 
    COUNT(*) as total_users,
    COUNT(CASE WHEN ACTIVE = true THEN 1 END) as active_users,
    COUNT(CASE WHEN ACTIVE = false THEN 1 END) as inactive_users
FROM USERS;

-- 모든 알림 조회 (최신순)
SELECT * FROM NOTIFICATIONS ORDER BY CREATED_AT DESC;

-- 특정 사용자의 알림 조회
SELECT * FROM NOTIFICATIONS WHERE USER_ID = 'admin' ORDER BY CREATED_AT DESC;

-- 알림 상태별 통계
SELECT 
    STATUS,
    COUNT(*) as count
FROM NOTIFICATIONS 
GROUP BY STATUS;

-- 알림 타입별 통계
SELECT 
    TYPE,
    COUNT(*) as count
FROM NOTIFICATIONS 
GROUP BY TYPE;

-- 오늘 생성된 알림 조회
SELECT * FROM NOTIFICATIONS 
WHERE DATE(CREATED_AT) = CURRENT_DATE 
ORDER BY CREATED_AT DESC;

-- ========================================
-- 업데이트 쿼리
-- ========================================

-- 사용자 활성 상태 변경
UPDATE USERS SET ACTIVE = false WHERE USERNAME = 'user1';

-- 알림 상태 업데이트
UPDATE NOTIFICATIONS SET STATUS = 'SENT', SENT_AT = CURRENT_TIMESTAMP 
WHERE STATUS = 'PENDING' AND ID = 1;

-- 사용자 정보 수정
UPDATE USERS 
SET EMAIL = 'newemail@example.com', PHONE_NUMBER = '010-9999-8888' 
WHERE USERNAME = 'admin';

-- ========================================
-- 삭제 쿼리
-- ========================================

-- 특정 사용자 삭제 (주의: 관련 알림도 함께 삭제해야 함)
DELETE FROM NOTIFICATIONS WHERE USER_ID = 'user1';
DELETE FROM USERS WHERE USERNAME = 'user1';

-- 특정 알림 삭제
DELETE FROM NOTIFICATIONS WHERE ID = 1;

-- 실패한 알림 삭제
DELETE FROM NOTIFICATIONS WHERE STATUS = 'FAILED';

-- ========================================
-- 고급 조회 쿼리
-- ========================================

-- 사용자별 알림 통계
SELECT 
    u.USERNAME,
    u.EMAIL,
    COUNT(n.ID) as total_notifications,
    COUNT(CASE WHEN n.STATUS = 'SENT' THEN 1 END) as sent_notifications,
    COUNT(CASE WHEN n.STATUS = 'PENDING' THEN 1 END) as pending_notifications,
    COUNT(CASE WHEN n.STATUS = 'FAILED' THEN 1 END) as failed_notifications
FROM USERS u
LEFT JOIN NOTIFICATIONS n ON u.USERNAME = n.USER_ID
WHERE u.ACTIVE = true
GROUP BY u.ID, u.USERNAME, u.EMAIL
ORDER BY total_notifications DESC;

-- 일별 알림 통계
SELECT 
    DATE(CREATED_AT) as notification_date,
    COUNT(*) as total_notifications,
    COUNT(CASE WHEN STATUS = 'SENT' THEN 1 END) as sent_count,
    COUNT(CASE WHEN STATUS = 'PENDING' THEN 1 END) as pending_count,
    COUNT(CASE WHEN STATUS = 'FAILED' THEN 1 END) as failed_count
FROM NOTIFICATIONS
GROUP BY DATE(CREATED_AT)
ORDER BY notification_date DESC;

-- 알림 타입별 성공률
SELECT 
    TYPE,
    COUNT(*) as total,
    COUNT(CASE WHEN STATUS = 'SENT' THEN 1 END) as sent,
    ROUND(COUNT(CASE WHEN STATUS = 'SENT' THEN 1 END) * 100.0 / COUNT(*), 2) as success_rate
FROM NOTIFICATIONS
GROUP BY TYPE;

-- ========================================
-- 데이터 정리 쿼리
-- ========================================

-- 30일 이전의 완료된 알림 삭제
DELETE FROM NOTIFICATIONS 
WHERE STATUS = 'SENT' 
AND CREATED_AT < DATEADD('DAY', -30, CURRENT_TIMESTAMP);

-- 비활성 사용자의 알림 삭제
DELETE FROM NOTIFICATIONS 
WHERE USER_ID IN (SELECT USERNAME FROM USERS WHERE ACTIVE = false);

-- ========================================
-- 인덱스 생성 (성능 향상)
-- ========================================

-- 사용자명 인덱스
CREATE INDEX IF NOT EXISTS idx_users_username ON USERS(USERNAME);

-- 이메일 인덱스
CREATE INDEX IF NOT EXISTS idx_users_email ON USERS(EMAIL);

-- 알림 생성일 인덱스
CREATE INDEX IF NOT EXISTS idx_notifications_created_at ON NOTIFICATIONS(CREATED_AT);

-- 알림 상태 인덱스
CREATE INDEX IF NOT EXISTS idx_notifications_status ON NOTIFICATIONS(STATUS);

-- 알림 사용자 인덱스
CREATE INDEX IF NOT EXISTS idx_notifications_user_id ON NOTIFICATIONS(USER_ID);

-- ========================================
-- 백업 및 복원
-- ========================================

-- 데이터 백업 (H2 콘솔에서 실행)
-- SCRIPT TO 'backup.sql';

-- 데이터 복원 (H2 콘솔에서 실행)
-- RUNSCRIPT FROM 'backup.sql'; 