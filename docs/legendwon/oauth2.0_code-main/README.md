# OAuth 2.0 Authentication Server for Android App

Flask 기반의 OAuth 2.0 인증 서버입니다. 안드로이드 앱과 연동하여 사용자 인증 및 권한 부여를 처리합니다.

## 기능

- **OAuth 2.0 표준 지원**
  - Authorization Code Grant (PKCE 지원)
  - Password Grant
  - Refresh Token Grant

- **사용자 관리**
  - 사용자 회원가입
  - 로그인/로그아웃
  - 비밀번호 해싱 (bcrypt)

- **토큰 관리**
  - Access Token 발급
  - Refresh Token 발급
  - 토큰 폐기 (Revoke)

## 설치 방법

### 1. 가상환경 생성 및 활성화

```bash
# Windows
python -m venv venv
venv\Scripts\activate

# macOS/Linux
python3 -m venv venv
source venv/bin/activate
```

### 2. 의존성 설치

```bash
pip install -r requirements.txt
```

### 3. 환경변수 설정

`.env` 파일이 이미 생성되어 있습니다. 프로덕션 환경에서는 `SECRET_KEY`를 반드시 변경하세요.

### 4. 데이터베이스 초기화

```bash
flask initdb
```

### 5. 테스트 데이터 생성 (선택사항)

```bash
flask create-test-data
```

테스트 계정:
- Username: `testuser`
- Password: `password123`
- Client ID: `android_app_client`
- Client Secret: `secret123`

### 6. 서버 실행

```bash
python app.py
```

서버가 `http://localhost:3000`에서 실행됩니다.

## API 엔드포인트

### 1. 사용자 회원가입
```http
POST /auth/register
Content-Type: application/json

{
  "username": "user1",
  "email": "user1@example.com",
  "password": "password123"
}
```

**응답:**
```json
{
  "message": "User registered successfully",
  "user_id": 1,
  "username": "user1"
}
```

### 2. 사용자 로그인
```http
POST /auth/login
Content-Type: application/json

{
  "username": "user1",
  "password": "password123"
}
```

**응답:**
```json
{
  "message": "Login successful",
  "user_id": 1,
  "username": "user1"
}
```

### 3. OAuth 클라이언트 등록 (관리자)
```http
POST /admin/register_client
Content-Type: application/json

{
  "client_name": "My Android App",
  "redirect_uris": "app://oauth2callback",
  "scope": "profile email"
}
```

**응답:**
```json
{
  "client_id": "abc123...",
  "client_secret": "xyz789...",
  "client_name": "My Android App"
}
```

### 4. Authorization Code 요청
```http
GET /oauth/authorize?response_type=code&client_id=CLIENT_ID&redirect_uri=REDIRECT_URI&scope=profile&state=STATE
```

사용자가 브라우저에서 승인하면 Authorization Code가 redirect_uri로 전달됩니다.

### 5. Access Token 발급 (Authorization Code Grant)
```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=authorization_code
&code=AUTHORIZATION_CODE
&client_id=CLIENT_ID
&client_secret=CLIENT_SECRET
&redirect_uri=REDIRECT_URI
```

### 6. Access Token 발급 (Password Grant)
```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=password
&username=user1
&password=password123
&client_id=CLIENT_ID
&client_secret=CLIENT_SECRET
&scope=profile
```

**응답:**
```json
{
  "access_token": "eyJ0eXAiOiJKV1QiLCJhbGc...",
  "token_type": "Bearer",
  "expires_in": 3600,
  "refresh_token": "def50200e7b3c8b...",
  "scope": "profile"
}
```

### 7. Refresh Token으로 갱신
```http
POST /oauth/token
Content-Type: application/x-www-form-urlencoded

grant_type=refresh_token
&refresh_token=REFRESH_TOKEN
&client_id=CLIENT_ID
&client_secret=CLIENT_SECRET
```

### 8. 사용자 정보 조회 (보호된 리소스)
```http
GET /api/me
Authorization: Bearer ACCESS_TOKEN
```

**응답:**
```json
{
  "id": 1,
  "username": "user1",
  "email": "user1@example.com"
}
```

### 9. 토큰 폐기
```http
POST /oauth/revoke
Content-Type: application/x-www-form-urlencoded

token=ACCESS_TOKEN
&token_type_hint=access_token
```

## 안드로이드 앱 연동 가이드

### 1. OAuth 클라이언트 등록
먼저 `/admin/register_client` 엔드포인트로 안드로이드 앱을 OAuth 클라이언트로 등록합니다.

### 2. Redirect URI 설정
안드로이드 앱의 `AndroidManifest.xml`에 커스텀 스킴을 등록:
```xml
<intent-filter>
    <action android:name="android.intent.action.VIEW" />
    <category android:name="android.intent.category.DEFAULT" />
    <category android:name="android.intent.category.BROWSABLE" />
    <data android:scheme="app" android:host="oauth2callback" />
</intent-filter>
```

### 3. Authorization Code Flow 구현

**Step 1: 사용자를 인증 페이지로 리다이렉트**
```kotlin
val authUrl = "http://YOUR_SERVER:3000/oauth/authorize?" +
    "response_type=code" +
    "&client_id=$CLIENT_ID" +
    "&redirect_uri=app://oauth2callback" +
    "&scope=profile" +
    "&state=$STATE"

val intent = Intent(Intent.ACTION_VIEW, Uri.parse(authUrl))
startActivity(intent)
```

**Step 2: Authorization Code 받기**
```kotlin
override fun onNewIntent(intent: Intent?) {
    super.onNewIntent(intent)
    val uri = intent?.data
    if (uri?.scheme == "app") {
        val code = uri.getQueryParameter("code")
        // Exchange code for token
    }
}
```

**Step 3: Access Token 교환**
```kotlin
val tokenUrl = "http://YOUR_SERVER:3000/oauth/token"
val params = mapOf(
    "grant_type" to "authorization_code",
    "code" to code,
    "client_id" to CLIENT_ID,
    "client_secret" to CLIENT_SECRET,
    "redirect_uri" to "app://oauth2callback"
)
// HTTP POST 요청으로 토큰 획득
```

### 4. Password Grant Flow (간단한 방법)
```kotlin
val tokenUrl = "http://YOUR_SERVER:3000/oauth/token"
val params = mapOf(
    "grant_type" to "password",
    "username" to username,
    "password" to password,
    "client_id" to CLIENT_ID,
    "client_secret" to CLIENT_SECRET,
    "scope" to "profile"
)
// HTTP POST 요청으로 토큰 획득
```

## 보안 권장사항

1. **HTTPS 사용**: 프로덕션 환경에서는 반드시 HTTPS를 사용하세요.
2. **SECRET_KEY 변경**: `.env` 파일의 `SECRET_KEY`를 강력한 랜덤 값으로 변경하세요.
3. **Client Secret 보호**: 안드로이드 앱에 Client Secret을 하드코딩하지 마세요.
4. **PKCE 사용**: Authorization Code Flow에서 PKCE를 사용하여 보안을 강화하세요.
5. **토큰 저장**: Access Token을 안전하게 저장하세요 (Android Keystore 사용 권장).

## 프로젝트 구조

```
auth/
├── app.py              # Flask 애플리케이션 메인 파일
├── models.py           # 데이터베이스 모델 (User, Client, Token)
├── oauth2.py           # OAuth 2.0 설정 및 Grant 구현
├── requirements.txt    # Python 의존성
├── .env                # 환경 변수
├── .env.example        # 환경 변수 예시
├── .gitignore          # Git 무시 파일
└── README.md           # 프로젝트 문서
```

## 문제 해결

### 데이터베이스 초기화 오류
```bash
rm oauth2.db
flask initdb
```

### 포트 변경
`.env` 파일에서 `PORT` 값을 변경하세요.

## 라이선스

MIT License
