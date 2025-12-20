# Docker 이미지 재빌드 가이드

## 🔍 문제 상황

터미널 로그 확인 결과:
- ✅ `/api/stores` → 정상 작동 (200 OK)
- ❌ `/api/auth/test` → 404 Not Found

**원인:** Docker 이미지에 새로 추가한 `AuthController` 코드가 포함되지 않음

---

## ✅ 해결 방법: 이미지 재빌드

### 방법 1: API 컨테이너만 재빌드 (권장)

```bash
cd /Users/namd0ng/02_Programming/vulnDin/mobile

# 이미지 재빌드 및 컨테이너 재시작
docker-compose up -d --build api
```

### 방법 2: 전체 재빌드

```bash
cd /Users/namd0ng/02_Programming/vulnDin/mobile

# 기존 컨테이너 중지 및 제거
docker-compose down

# 이미지 재빌드 및 시작
docker-compose up -d --build
```

### 방법 3: 강제 재빌드 (캐시 무시)

```bash
cd /Users/namd0ng/02_Programming/vulnDin/mobile

# 캐시 없이 재빌드
docker-compose build --no-cache api
docker-compose up -d api
```

---

## 🔍 재빌드 후 확인

### 1. 서버 로그 확인

```bash
# 실시간 로그 확인
docker logs -f wh-eats-api

# 또는 최근 로그만
docker logs --tail 50 wh-eats-api
```

**확인할 내용:**
- `✅ AuthController가 등록되었습니다.` 메시지
- `Mapped "{[/api/auth/test]` 메시지
- `Mapped "{[/api/auth/login]` 메시지

### 2. 테스트 엔드포인트 확인

```bash
# 컨테이너 내부에서
docker exec wh-eats-api curl http://localhost:8080/api/auth/test

# 예상 결과: "AuthController가 정상적으로 등록되었습니다!"
```

### 3. 로그인 테스트

```bash
docker exec wh-eats-api curl -X POST http://localhost:8080/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"email":"consumer1@wheats.local"}'
```

---

## 📋 단계별 가이드

### Step 1: 현재 상태 확인
```bash
docker ps | grep wh-eats-api
```

### Step 2: 이미지 재빌드
```bash
docker-compose up -d --build api
```

### Step 3: 빌드 로그 확인
빌드 과정에서 다음을 확인:
- `COPY api/ .` - 소스 코드 복사
- `./gradlew bootJar` - JAR 파일 빌드
- 빌드 성공 메시지

### Step 4: 서버 시작 로그 확인
```bash
docker logs wh-eats-api | grep -i "auth\|mapped\|started"
```

### Step 5: 테스트
```bash
docker exec wh-eats-api curl http://localhost:8080/api/auth/test
```

---

## ⚠️ 주의사항

1. **재빌드 시간**: 처음 빌드 시 몇 분 소요될 수 있음
2. **데이터베이스**: DB 컨테이너는 그대로 유지됨 (데이터 손실 없음)
3. **포트**: 8080 포트가 사용 중이면 충돌 가능

---

## 🐛 빌드 오류 발생 시

### 컴파일 오류 확인
```bash
# 빌드 로그에서 오류 확인
docker-compose build api 2>&1 | grep -i "error\|exception\|failed"
```

### 수동 빌드 테스트
```bash
cd /Users/namd0ng/02_Programming/vulnDin/mobile/api
./gradlew clean build
```

컴파일 오류가 있다면 수정 후 다시 재빌드

---

**작성일:** 2024년
**목적:** Docker 이미지 재빌드로 AuthController 등록 문제 해결
