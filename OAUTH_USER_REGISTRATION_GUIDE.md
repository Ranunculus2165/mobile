# OAuth 사용자 계정 등록 가이드

## 📋 개요

OAuth 서버에 customer, store, admin 계정을 등록하는 방법입니다.

**중요:** OAuth 계정은 **OAuth 서버의 DB**에 등록해야 합니다. 현재 프로젝트의 `wheats` DB가 아닙니다.

---

## 🔍 현재 상황

### 1. 프로젝트 DB (`wheats` 데이터베이스)
- 위치: `db/schema.sql`, `db/init.sql`
- 용도: 기존 Spring Boot API 서버용 (주문, 장바구니, 가게 등)
- 사용자 테이블: `users` (CONSUMER, MERCHANT, ADMIN 역할)
- **OAuth 계정과는 별개입니다**

### 2. OAuth 서버 DB
- 위치: Flask OAuth 서버가 관리하는 별도 DB
- 용도: OAuth 2.0 인증 전용
- 사용자 등록: `/auth/register` API 엔드포인트 사용

---

## ✅ OAuth 계정 등록 방법

### 방법 1: OAuth 서버 API 사용 (권장)

OAuth 서버가 실행 중이어야 합니다:

```bash
cd ../auth  # OAuth 서버 디렉토리
python app.py
```

#### 1-1. Customer 계정 등록

```bash
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "email": "customer1@example.com",
    "password": "password123"
  }'
```

**응답:**
```json
{
  "message": "User registered successfully",
  "user_id": 1,
  "username": "customer1"
}
```

#### 1-2. Store 계정 등록

```bash
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "store1",
    "email": "store1@example.com",
    "password": "password123"
  }'
```

#### 1-3. Admin 계정 등록

```bash
curl -X POST http://localhost:3000/auth/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "admin1",
    "email": "admin1@example.com",
    "password": "password123"
  }'
```

### 방법 2: Postman 사용

1. **새 요청 생성**
   - Method: `POST`
   - URL: `http://localhost:3000/auth/register`

2. **Headers 설정**
   - Key: `Content-Type`
   - Value: `application/json`

3. **Body 설정 (raw JSON)**

   **Customer:**
   ```json
   {
     "username": "customer1",
     "email": "customer1@example.com",
     "password": "password123"
   }
   ```

   **Store:**
   ```json
   {
     "username": "store1",
     "email": "store1@example.com",
     "password": "password123"
   }
   ```

   **Admin:**
   ```json
   {
     "username": "admin1",
     "email": "admin1@example.com",
     "password": "password123"
   }
   ```

4. **Send 클릭**

### 방법 3: Python 스크립트

```python
import requests

BASE_URL = "http://localhost:3000"

# Customer 계정
customer_data = {
    "username": "customer1",
    "email": "customer1@example.com",
    "password": "password123"
}
response = requests.post(f"{BASE_URL}/auth/register", json=customer_data)
print("Customer:", response.json())

# Store 계정
store_data = {
    "username": "store1",
    "email": "store1@example.com",
    "password": "password123"
}
response = requests.post(f"{BASE_URL}/auth/register", json=store_data)
print("Store:", response.json())

# Admin 계정
admin_data = {
    "username": "admin1",
    "email": "admin1@example.com",
    "password": "password123"
}
response = requests.post(f"{BASE_URL}/auth/register", json=admin_data)
print("Admin:", response.json())
```

---

## 🔐 로그인 테스트

등록한 계정으로 로그인 테스트:

### Customer 로그인

```bash
curl -X POST http://localhost:3000/auth/login \
  -H "Content-Type: application/json" \
  -d '{
    "username": "customer1",
    "password": "password123"
  }'
```

### OAuth 토큰 발급 (Password Grant)

```bash
curl -X POST http://localhost:3000/oauth/token \
  -H "Content-Type: application/x-www-form-urlencoded" \
  -d "grant_type=password" \
  -d "username=customer1" \
  -d "password=password123" \
  -d "client_id=android_app_client" \
  -d "client_secret=secret123" \
  -d "scope=customer"
```

**응답:**
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "def50200e7b3c8b...",
  "scope": "customer"
}
```

---

## 📝 권장 테스트 계정

다음 계정들을 등록해두면 테스트에 편리합니다:

| 계정 타입 | Username | Email | Password | Scope |
|---------|----------|-------|----------|-------|
| Customer | `customer1` | `customer1@example.com` | `password123` | `customer` |
| Store | `store1` | `store1@example.com` | `password123` | `store` |
| Admin | `admin1` | `admin1@example.com` | `password123` | `admin` |

---

## ⚠️ 주의사항

### 1. OAuth 서버 DB vs 프로젝트 DB

- **OAuth 서버 DB**: OAuth 인증용 사용자 계정
- **프로젝트 DB (`wheats`)**: 기존 Spring Boot API용 사용자 계정

**두 DB는 별개입니다!**

### 2. Scope와 Role의 차이

- **OAuth Scope**: `customer`, `store`, `admin` (OAuth 권한)
- **프로젝트 Role**: `CONSUMER`, `MERCHANT`, `ADMIN` (애플리케이션 역할)

OAuth scope는 OAuth 서버에서 관리하고, 프로젝트 role은 프로젝트 DB에서 관리합니다.

### 3. 통합 필요 시

만약 OAuth 계정과 프로젝트 사용자를 연결해야 한다면:
- `oauth_accounts` 테이블을 사용하여 매핑
- 또는 OAuth 서버에서 사용자 정보를 조회하여 프로젝트 DB와 동기화

---

## 🧪 전체 테스트 플로우

1. **OAuth 서버 실행**
   ```bash
   cd ../auth
   python app.py
   ```

2. **계정 등록**
   ```bash
   # Customer
   curl -X POST http://localhost:3000/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "customer1", "email": "customer1@example.com", "password": "password123"}'
   
   # Store
   curl -X POST http://localhost:3000/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "store1", "email": "store1@example.com", "password": "password123"}'
   
   # Admin
   curl -X POST http://localhost:3000/auth/register \
     -H "Content-Type: application/json" \
     -d '{"username": "admin1", "email": "admin1@example.com", "password": "password123"}'
   ```

3. **안드로이드 앱에서 로그인 테스트**
   - Customer scope로 로그인
   - Store scope로 로그인
   - Admin scope로 로그인 (권한 상승 취약점 테스트)

---

## 📚 참고

- OAuth 서버 README: `../auth/README.md`
- OAuth 클라이언트 등록: `OAUTH_CLIENT_REGISTRATION_GUIDE.md`

---

**작성일:** 2024년
**목적:** OAuth 사용자 계정 등록 가이드
