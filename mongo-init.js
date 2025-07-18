// MongoDB 초기화 스크립트
db = db.getSiblingDB('notification_system');

// 사용자 생성
db.createUser({
  user: 'notification_user',
  pwd: 'notification_pass',
  roles: [
    {
      role: 'readWrite',
      db: 'notification_system'
    }
  ]
});

// 컬렉션 생성
db.createCollection('users');
db.createCollection('notifications');

// 인덱스 생성
db.users.createIndex({ "username": 1 }, { unique: true });
db.users.createIndex({ "email": 1 }, { unique: true });
db.notifications.createIndex({ "userId": 1 });
db.notifications.createIndex({ "status": 1 });
db.notifications.createIndex({ "createdAt": -1 });

print('MongoDB 초기화 완료'); 