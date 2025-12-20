# OAuth 서버 클라이언트 등록 가이드

## 📋 개요

안드로이드 앱을 OAuth 2.0 서버에 클라이언트로 등록하는 방법입니다.

---

## 🚀 사전 준비

### 1. OAuth 서버 실행

Flask OAuth 서버가 실행 중이어야 합니다:

```bash
cd ../auth  # OAuth 서버 디렉토리로 이동
. venv/Scripts/activate  # Windows
# 또는
source venv/bin/activate  # macOS/Linux

python app.py
```

서버가 `http://localhost:3000`에서 실행됩니다.

---

## 📝 클라이언트 등록 방법

### 방법 1: curl 사용 (터미널)

```bash
curl -X POST http://localhost:3000/admin/register_client \
  -H "Content-Type: application/json" \
  -d '{
    "client_name": "Android App",
    "redirect_uris": "app://oauth2callback",
    "scope": "customer store admin"
  }'
```

**응답 예시:**
```json
{
  "client_id": "android_app_client",
  "client_secret": "secret123",
  "client_name": "Android App"
}
```

### 방법 2: Postman 사용

1. **새 요청 생성**
   - Method: `POST`
   - URL: `http://localhost:3000/admin/register_client`

2. **Headers 설정**
   - Key: `Content-Type`
   - Value: `application/json`

3. **Body 설정 (raw JSON)**
   ```json
   {
     "client_name": "Android App",
     "redirect_uris": "app://oauth2callback",
     "scope": "customer store admin"
   }
   ```

4. **Send 클릭**

### 방법 3: Python 스크립트

```python
import requests

url = "http://localhost:3000/admin/register_client"
data = {
    "client_name": "Android App",
    "redirect_uris": "app://oauth2callback",
    "scope": "customer store admin"
}

response = requests.post(url, json=data)
print(response.json())
```

---

## ✅ 등록 확인

### 1. 응답 확인

성공적으로 등록되면 다음과 같은 응답을 받습니다:

```json
{
  "client_id": "생성된_클라이언트_ID",
  "client_secret": "생성된_클라이언트_시크릿",
  "client_name": "Android App"
}
```

### 2. OAuthConfig.kt 업데이트

응답으로 받은 `client_id`와 `client_secret`을 `OAuthConfig.kt`에 반영:

```kotlin
object OAuthConfig {
    const val CLIENT_ID = "생성된_클라이언트_ID"  // 응답에서 받은 값
    const val CLIENT_SECRET = "생성된_클라이언트_시크릿"  // 응답에서 받은 값
    // ...
}
```

---

## 🔍 기존 클라이언트 확인

README에 따르면 테스트 계정이 이미 등록되어 있을 수 있습니다:

- **Client ID:** `android_app_client`
- **Client Secret:** `secret123`
- **Redirect URI:** `app://oauth2callback`

이미 등록되어 있다면 추가 등록이 필요 없습니다. `OAuthConfig.kt`에 이미 올바른 값이 설정되어 있습니다.

---

## ⚠️ 주의사항

### 1. Redirect URI 일치

- 서버에 등록한 `redirect_uris`와 앱의 `OAuthConfig.REDIRECT_URI`가 정확히 일치해야 합니다.
- 현재 설정: `app://oauth2callback`

### 2. Scope 설정

- 등록 시 `scope`에 `customer`, `store`, `admin`을 모두 포함해야 합니다.
- 권한 상승 취약점 테스트를 위해 모든 scope가 필요합니다.

### 3. Client Secret 보안

- ⚠️ **데모/테스트 환경에서만 사용**
- 프로덕션에서는 Public Client (Client Secret 없음) 또는 PKCE만 사용해야 합니다.
- Client Secret을 하드코딩하지 마세요.

---

## 🧪 등록 테스트

등록이 완료되면 다음 명령으로 테스트할 수 있습니다:

```bash
# 클라이언트 정보 확인 (서버에 해당 엔드포인트가 있는 경우)
curl http://localhost:3000/admin/clients
```

또는 OAuth 인증 플로우를 테스트:

1. 앱에서 로그인 시도
2. 브라우저가 열리고 OAuth 인증 페이지 표시
3. 인증 완료 후 앱으로 리다이렉트

---

## 📚 참고

- OAuth 서버 README: `../auth/README.md`
- OAuth 2.0 RFC 6749: https://datatracker.ietf.org/doc/html/rfc6749

---

**작성일:** 2024년
**목적:** OAuth 클라이언트 등록 가이드
