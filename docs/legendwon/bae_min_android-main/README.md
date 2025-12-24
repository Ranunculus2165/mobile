# 화햇딜리버리 - Android 앱

OAuth 2.0 Refresh Token Scope 권한 상승 취약점 데모 앱

## 프로젝트 구조

```
DeliveryApp/
├── app/
│   ├── build.gradle.kts          # 빌드 설정 (AppAuth 라이브러리 포함)
│   └── src/main/
│       ├── AndroidManifest.xml   # 앱 매니페스트 (OAuth 리다이렉트 설정)
│       ├── java/com/example/deliveryapp/
│       │   ├── MainActivity.kt            # 로그인 화면
│       │   ├── DashboardActivity.kt       # 대시보드 (API 테스트)
│       │   └── AuthStateManager.kt        # 토큰 저장 관리
│       └── res/layout/
│           ├── activity_main.xml          # 로그인 화면 레이아웃
│           └── activity_dashboard.xml     # 대시보드 레이아웃
└── README.md
```

## 주요 기능

### 1. OAuth 2.0 로그인
- **Authorization Code + PKCE Flow** 구현
- Customer scope 로그인
- Store scope 로그인
- Refresh Token 자동 발급 및 저장

### 2. 🚨 취약점 익스플로잇
- **Scope 권한 상승 공격 버튼**
- Customer scope로 로그인 후 Refresh Token을 사용하여 Store scope 권한 획득
- 실시간 로그로 공격 과정 확인

### 3. API 테스트
- Customer API 호출 (`/api/customer/orders`)
- Store API 호출 (`/api/store/dashboard`) - 권한 상승 후 접근 가능

## 설치 및 실행

### 1. OAuth 서버 실행

먼저 Flask OAuth 서버가 실행 중이어야 합니다:

```bash
cd ../auth
. venv/Scripts/activate
python app.py
```

서버가 `http://localhost:3000`에서 실행됩니다.

### 2. Android 앱 빌드

#### Android Studio에서:
1. Android Studio 실행
2. `DeliveryApp` 폴더 열기
3. Gradle sync 완료 대기
4. 에뮬레이터 또는 실제 기기에서 실행

#### 명령줄에서:
```bash
# Debug APK 빌드
./gradlew assembleDebug

# 에뮬레이터에 설치
adb install app/build/outputs/apk/debug/app-debug.apk
```

### 3. 네트워크 설정

**중요**: Android 에뮬레이터에서 localhost 접근:
- `10.0.2.2`가 호스트 머신의 `localhost`를 가리킴
- 앱 코드에서 `AUTH_SERVER_URL = "http://10.0.2.2:3000"` 사용

**실제 기기에서 테스트**:
- 호스트 PC의 IP 주소 확인 (예: `192.168.1.10`)
- `MainActivity.kt`에서 `AUTH_SERVER_URL` 수정
- Flask 서버를 `0.0.0.0`에서 실행: `python app.py --host=0.0.0.0`

## 취약점 테스트 시나리오

### Step 1: Customer로 로그인

1. 앱 실행
2. **"고객으로 로그인 (Customer Scope)"** 버튼 클릭
3. 브라우저가 열리고 OAuth 인증 진행
4. 대시보드로 이동

### Step 2: Customer API 테스트

1. **"고객 API 호출"** 버튼 클릭
2. 성공적으로 주문 목록 조회 확인

### Step 3: Store API 접근 시도 (차단됨)

1. **"점주 API 호출"** 버튼 클릭
2. `insufficient_scope` 에러 확인
3. Customer scope로는 Store API 접근 불가

### Step 4: 🚨 취약점 익스플로잇

1. 로그아웃 후 메인 화면으로 돌아가기
2. 다시 **"고객으로 로그인"** (customer scope)
3. 대시보드 대신 **로그아웃 버튼**으로 메인 화면 복귀
4. **"🚨 Scope 권한 상승 공격"** 버튼 클릭
5. 토스트 메시지: "권한 상승 성공! customer → store"

### Step 5: 권한 상승 확인

1. 대시보드로 자동 이동
2. **"점주 API 호출"** 버튼 다시 클릭
3. 🎯 **성공!** 점주 대시보드 데이터 조회:
   - 매출 정보 (revenue)
   - 고객 개인정보 (customer_data)
   - 대기 주문 수 (pending_orders)

## 코드 주요 부분

### MainActivity.kt - 취약점 익스플로잇 코드

```kotlin
private fun exploitScopeEscalation() {
    val authState = authStateManager.current

    // Refresh Token으로 Store scope 요청
    val formBody = FormBody.Builder()
        .add("grant_type", "refresh_token")
        .add("refresh_token", authState.refreshToken!!)
        .add("scope", "store")  // 🚨 권한 상승!
        .add("client_id", CLIENT_ID)
        .add("client_secret", CLIENT_SECRET)
        .build()

    // 서버는 검증 없이 store scope 토큰 발급
    // ...
}
```

### 서버 측 취약점 (auth/oauth2.py)

```python
class RefreshTokenGrant(grants.RefreshTokenGrant):
    def _validate_token_scope(self, token):
        # 🚨 VULNERABILITY: No scope validation!
        pass  # 아무 검증도 하지 않음
```

## 보안 권장 사항

### 올바른 구현

Refresh Token으로 토큰 갱신 시 **반드시 원래 scope의 부분집합만 허용**해야 합니다:

```python
def _validate_token_scope(self, token):
    requested_scope = self.request.data.get('scope', token.scope)
    requested_set = set(requested_scope.split())
    original_set = set(token.scope.split())

    if not requested_set.issubset(original_set):
        raise InvalidScopeError('Scope escalation detected')
```

## 의존성

- **AppAuth-Android**: OAuth 2.0 클라이언트 라이브러리
- **OkHttp**: HTTP 클라이언트
- **Retrofit**: REST API 클라이언트
- **Kotlin Coroutines**: 비동기 처리

## 참고 자료

- [AppAuth-Android Documentation](https://github.com/openid/AppAuth-Android)
- [RFC 6749 - OAuth 2.0](https://datatracker.ietf.org/doc/html/rfc6749)
- [RFC 7636 - PKCE](https://datatracker.ietf.org/doc/html/rfc7636)
- [OWASP - Broken Access Control](https://owasp.org/Top10/A01_2021-Broken_Access_Control/)
