# Docker 컨테이너에서 API 테스트 가이드

## 📋 Docker 컨테이너에서 curl 테스트 방법

### 1. API 컨테이너에 접속

```bash
# 컨테이너 이름 확인
docker ps

# API 컨테이너에 접속
docker exec -it wh-eats-api /bin/bash

# 또는 sh 사용 (bash가 없는 경우)
docker exec -it wh-eats-api /bin/sh
```

### 2. 컨테이너 내부에서 curl 테스트

컨테이너 내부에서는 `localhost:8080`으로 접근합니다:

```bash
# 1. 테스트 엔드포인트 확인 (AuthController 등록 확인)
curl http://localhost:8080/api/auth/test

# 예상 결과: "AuthController가 정상적으로 등록되었습니다!"

# 2. 로그인 테스트
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"consumer1@wheats.local"}'

# 예상 결과: JWT 토큰이 포함된 JSON 응답
```

### 3. 컨테이너 밖에서 테스트 (호스트에서)

호스트에서 테스트할 때는 컨테이너 포트로 접근:

```bash
# localhost 사용 (호스트에서)
curl http://localhost:8080/api/auth/test

# 또는 안드로이드 에뮬레이터와 동일하게
curl http://10.0.2.2:8080/api/auth/test
```

---

## 🔍 컨테이너 로그 확인

### 실시간 로그 확인
```bash
# API 컨테이너 로그 확인
docker logs -f wh-eats-api

# 또는 docker-compose 사용
docker-compose logs -f api
```

### 로그에서 확인할 내용
- `✅ AuthController가 등록되었습니다.` 메시지
- `Mapped "{[/api/auth/login]` 메시지
- 컴파일 오류나 런타임 오류

---

## 🧪 단계별 테스트

### Step 1: 컨테이너 접속
```bash
docker exec -it wh-eats-api /bin/sh
```

### Step 2: curl 설치 확인 (필요시)
```bash
# curl이 없으면 설치 (Alpine Linux인 경우)
apk add curl

# 또는 Debian/Ubuntu 기반
apt-get update && apt-get install -y curl
```

### Step 3: 테스트 엔드포인트 확인
```bash
curl http://localhost:8080/api/auth/test
```

**예상 결과:**
```
AuthController가 정상적으로 등록되었습니다!
```

**404가 나오면:**
- 서버가 재시작되지 않았거나
- AuthController가 등록되지 않음

### Step 4: 로그인 테스트
```bash
curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"consumer1@wheats.local"}'
```

**예상 결과 (성공):**
```json
{
  "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
  "userId": 1,
  "name": "테스트 소비자",
  "email": "consumer1@wheats.local",
  "role": "CONSUMER"
}
```

**예상 결과 (사용자 없음 - 400):**
```json
{
  "message": "이메일 또는 비밀번호를 확인해주세요.",
  "error": "USER_NOT_FOUND"
}
```

---

## 🔧 컨테이너 재시작

### API 컨테이너만 재시작
```bash
docker-compose restart api
```

### 전체 재시작
```bash
docker-compose down
docker-compose up -d
```

### 재빌드 후 재시작 (코드 변경 반영)
```bash
docker-compose up -d --build api
```

---

## 📝 빠른 테스트 명령어

### 한 줄로 테스트
```bash
# 컨테이너 내부에서
docker exec wh-eats-api curl http://localhost:8080/api/auth/test

# 호스트에서
curl http://localhost:8080/api/auth/test
```

### 로그인 테스트 (한 줄)
```bash
docker exec wh-eats-api curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"consumer1@wheats.local"}'
```

---

## ⚠️ 주의사항

1. **컨테이너 내부에서**: `localhost:8080` 사용
2. **호스트에서**: `localhost:8080` 또는 `10.0.2.2:8080` 사용
3. **안드로이드 에뮬레이터에서**: `10.0.2.2:8080` 사용 (호스트의 localhost를 가리킴)

---

**작성일:** 2024년
**목적:** Docker 컨테이너에서 API 테스트 방법
