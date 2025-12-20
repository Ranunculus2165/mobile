# 로그인 기능 구현 단계

## 📋 현재 상황

### 서버 측 (Spring Boot API)
- **하드코딩된 userId=1L 위치:**
  - `MyPageController`: `/api/users/me`, `/api/users/me/page`
  - `SupportTicketController`: `/api/users/me/support-tickets` (GET, POST)
  - `CartController`: `/api/cart` (GET, POST, PATCH, DELETE)
  - `OrderService`: `createOrder()`, `getOrderDetail()`

### 안드로이드 앱
- 인증 없이 API 호출 중
- 모든 API가 익명으로 동작

### DB
- `users` 테이블 존재 (id, name, email, role, point)
- **비밀번호 필드 없음** → 이메일만으로 인증 (개발용, 나중에 OAuth로 교체)

---

## 🎯 구현 목표

1. **서버**: JWT 토큰 기반 인증 구현
2. **서버**: 로그인 API (`POST /api/auth/login`) - **이메일만 입력** (비밀번호 없음)
3. **서버**: 인증 인터셉터로 토큰 검증 및 userId 추출
4. **서버**: 기존 API들에 인증 적용
5. **안드로이드**: 로그인 화면 구현 (이메일만 입력)
6. **안드로이드**: 토큰 저장 및 API 호출 시 헤더 추가

**⚠️ 주의**: 이는 개발용 간단한 인증 방식입니다. 나중에 OAuth로 교체할 예정입니다.

---

## 📝 구현 단계

### **Phase 1: 서버 측 - 인증 인프라 구축**

#### **Step 1-1: DB 스키마 확인**

**파일:** `db/schema.sql`

**현재 상태:**
- `users` 테이블에 password 필드 없음 → **그대로 유지**
- 이메일만으로 사용자 식별
- 나중에 OAuth로 교체할 예정이므로 비밀번호 필드 추가 불필요

**작업:**
- [x] DB 스키마 수정 불필요 (현재 상태 유지)

---

#### **Step 1-2: JWT 의존성 추가**

**파일:** `api/build.gradle.kts`

**추가할 의존성:**
```kotlin
dependencies {
    // ... 기존 의존성 ...
    
    // JWT 라이브러리
    implementation("io.jsonwebtoken:jjwt-api:0.12.3")
    implementation("io.jsonwebtoken:jjwt-impl:0.12.3")
    implementation("io.jsonwebtoken:jjwt-jackson:0.12.3")
    
    // ⚠️ 비밀번호 해싱 불필요 (이메일만으로 인증)
}
```

**작업:**
- [x] `build.gradle.kts`에 의존성 추가
- [ ] Gradle Sync

---

#### **Step 1-3: UserEntity 확인 (수정 불필요)**

**파일:** `api/src/main/java/com/wheats/api/mypage/entity/UserEntity.java`

**현재 상태:**
- `UserEntity`에 password 필드 없음 → **그대로 유지**
- 이메일만으로 사용자 식별

**작업:**
- [x] UserEntity 수정 불필요 (현재 상태 유지)

---

#### **Step 1-4: JWT 유틸리티 클래스 생성**

**파일:** `api/src/main/java/com/wheats/api/auth/util/JwtUtil.java` (새로 생성)

**기능:**
- JWT 토큰 생성
- JWT 토큰 검증
- userId 추출

**주요 메서드:**
```java
public class JwtUtil {
    private static final String SECRET_KEY = "your-secret-key-change-in-production"; // ⚠️ 프로덕션에서는 환경변수로
    private static final long EXPIRATION_TIME = 86400000; // 24시간
    
    public String generateToken(Long userId) { ... }
    public Long getUserIdFromToken(String token) { ... }
    public boolean validateToken(String token) { ... }
}
```

**작업:**
- [x] `JwtUtil.java` 파일 생성
- [x] 토큰 생성/검증 로직 구현

---

#### **Step 1-5: (건너뜀) 비밀번호 유틸리티 불필요**

**⚠️ 비밀번호 검증 없이 이메일만으로 인증하므로 PasswordUtil 불필요**

**작업:**
- [x] PasswordUtil 생성 불필요

---

### **Phase 2: 서버 측 - 로그인 API 구현**

#### **Step 2-1: 로그인 DTO 생성**

**파일:** `api/src/main/java/com/wheats/api/auth/dto/LoginRequest.java` (새로 생성)
**파일:** `api/src/main/java/com/wheats/api/auth/dto/LoginResponse.java` (새로 생성)

**LoginRequest:**
```java
public class LoginRequest {
    private String email;  // ✅ 이메일만 입력 (비밀번호 없음)
    // Getter/Setter
}
```

**LoginResponse:**
```java
public class LoginResponse {
    private String token;
    private Long userId;
    private String name;
    private String email;
    private String role;
    // Getter/Setter
}
```

**작업:**
- [x] `LoginRequest.java` 생성
- [x] `LoginResponse.java` 생성

---

#### **Step 2-2: AuthService 생성**

**파일:** `api/src/main/java/com/wheats/api/auth/service/AuthService.java` (새로 생성)

**기능:**
- 이메일로 로그인 (비밀번호 검증 없음)
- 사용자 존재 여부 확인
- JWT 토큰 생성

**주요 메서드:**
```java
@Service
public class AuthService {
    private final UserRepository userRepository;
    private final JwtUtil jwtUtil;
    
    public LoginResponse login(LoginRequest request) {
        // 1. 이메일로 사용자 조회
        UserEntity user = userRepository.findByEmail(request.getEmail())
            .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다."));
        
        // 2. 비밀번호 검증 없음 (이메일만 확인)
        
        // 3. JWT 토큰 생성
        String token = jwtUtil.generateToken(user.getId());
        
        // 4. LoginResponse 반환
        return new LoginResponse(token, user.getId(), user.getName(), user.getEmail(), user.getRole().name());
    }
}
```

**작업:**
- [x] `AuthService.java` 생성
- [x] 이메일로 사용자 조회 및 토큰 생성 로직 구현
- [x] `UserRepository`에 `findByEmail` 메서드 추가

---

#### **Step 2-3: AuthController 생성**

**파일:** `api/src/main/java/com/wheats/api/auth/controller/AuthController.java` (새로 생성)

**엔드포인트:**
```java
@RestController
@RequestMapping("/api/auth")
public class AuthController {
    private final AuthService authService;
    
    @PostMapping("/login")
    public ResponseEntity<LoginResponse> login(@RequestBody LoginRequest request) {
        LoginResponse response = authService.login(request);
        return ResponseEntity.ok(response);
    }
}
```

**작업:**
- [x] `AuthController.java` 생성
- [x] 로그인 엔드포인트 구현

---

### **Phase 3: 서버 측 - 인증 인터셉터 구현**

#### **Step 3-1: 인증 인터셉터 생성**

**파일:** `api/src/main/java/com/wheats/api/auth/interceptor/AuthInterceptor.java` (새로 생성)

**기능:**
- 요청 헤더에서 JWT 토큰 추출 (`Authorization: Bearer {token}`)
- 토큰 검증
- userId를 Request Attribute에 저장
- 인증 실패 시 401 반환

**작업:**
- [x] `AuthInterceptor.java` 생성
- [x] 토큰 추출/검증 로직 구현
- [x] Request Attribute에 userId와 role 저장

---

#### **Step 3-2: 인터셉터 등록**

**파일:** `api/src/main/java/com/wheats/api/config/WebConfig.java` (새로 생성)

**내용:**
```java
@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final AuthInterceptor authInterceptor;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns(
                    "/api/auth/login",
                    "/api/auth/register",  // 회원가입 (선택)
                    "/api/stores/**"       // 가게 목록은 공개 (선택)
                );
    }
}
```

**작업:**
- [x] `WebConfig.java` 생성
- [x] 인터셉터 등록
- [x] 인증 제외 경로 설정

---

#### **Step 3-3: 현재 사용자 조회 유틸리티**

**파일:** `api/src/main/java/com/wheats/api/auth/util/AuthContext.java` (새로 생성)

**기능:**
- Request에서 userId 추출
- 컨트롤러에서 쉽게 사용할 수 있는 유틸리티

**작업:**
- [x] `AuthContext.java` 생성
- [x] `getCurrentUserId()` 메서드 구현
- [x] `getCurrentUserRole()` 메서드 구현

---

### **Phase 4: 서버 측 - 기존 API에 인증 적용**

#### **Step 4-1: MyPageController 수정**

**파일:** `api/src/main/java/com/wheats/api/mypage/controller/MyPageController.java`

**수정 내용:**
```java
@GetMapping("/me")
public ResponseEntity<MyPageProfileResponse> getMyProfile() {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 변경
    
    MyPageProfileResponse response = myPageService.getMyProfile(userId);
    return ResponseEntity.ok(response);
}
```

**작업:**
- [x] `getMyProfile()` 수정
- [x] `getMyPage()` 수정

---

#### **Step 4-2: SupportTicketController 수정**

**파일:** `api/src/main/java/com/wheats/api/mypage/controller/SupportTicketController.java`

**수정 내용:**
```java
@GetMapping
public ResponseEntity<List<SupportTicketResponse>> getMySupportTickets() {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 변경
    // ...
}

@PostMapping
public ResponseEntity<SupportTicketResponse> createSupportTicket(
        @RequestBody CreateSupportTicketRequest request) {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 변경
    // ...
}
```

**작업:**
- [x] `getMySupportTickets()` 수정
- [x] `createSupportTicket()` 수정

---

#### **Step 4-3: CartController 수정**

**파일:** `api/src/main/java/com/wheats/api/order/controller/CartController.java`

**수정 내용:**
```java
@GetMapping
public ResponseEntity<CartResponse> getMyCart() {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 변경
    // ...
}

@PostMapping("/items")
public ResponseEntity<?> addItem(...) {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 변경
    // ...
}

// 모든 메서드에 동일하게 적용
```

**작업:**
- [x] 모든 메서드의 `userId = 1L` 제거
- [x] `AuthContext.getCurrentUserId()` 적용

---

#### **Step 4-4: OrderService 수정**

**파일:** `api/src/main/java/com/wheats/api/order/service/OrderService.java`

**수정 내용:**
```java
// createOrder 메서드에 userId 파라미터 추가
public OrderResponse createOrder(Long userId, OrderRequest request) {
    // Long userId = 1L; 제거
    // ...
}

// getOrderDetail 메서드에 userId 파라미터 추가
public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {
    // Long userId = 1L; 제거
    // ...
}
```

**작업:**
- [x] `createOrder()` 메서드 시그니처 변경
- [x] `getOrderDetail()` 메서드 시그니처 변경

---

#### **Step 4-5: OrderController 수정**

**파일:** `api/src/main/java/com/wheats/api/order/controller/OrderController.java`

**수정 내용:**
```java
@PostMapping
public ResponseEntity<OrderResponse> createOrder(@RequestBody OrderRequest request) {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 추가
    OrderResponse response = orderService.createOrder(userId, request); // ✅ 변경
    return ResponseEntity.ok(response);
}

@GetMapping("/{orderId}")
public ResponseEntity<OrderDetailResponse> getOrderDetail(@PathVariable Long orderId) {
    Long userId = AuthContext.getCurrentUserId(); // ✅ 추가
    OrderDetailResponse response = orderService.getOrderDetail(userId, orderId); // ✅ 변경
    return ResponseEntity.ok(response);
}
```

**작업:**
- [x] `createOrder()` 수정
- [x] `getOrderDetail()` 수정

---

### **Phase 5: 안드로이드 - 로그인 기능 구현**

#### **Step 5-1: 로그인 API 인터페이스 추가**

**파일:** `mobile/app/src/main/java/com/example/mobile/data/network/AuthApi.kt` (새로 생성)

**내용:**
```kotlin
interface AuthApi {
    @POST("api/auth/login")
    suspend fun login(@Body request: LoginRequest): LoginResponse
}
```

**작업:**
- [x] `AuthApi.kt` 생성
- [x] `LoginRequest.kt`, `LoginResponse.kt` 모델 생성

---

#### **Step 5-2: 토큰 저장소 구현**

**파일:** `mobile/app/src/main/java/com/example/mobile/data/auth/TokenManager.kt` (새로 생성)

**기능:**
- JWT 토큰 저장 (SharedPreferences)
- 토큰 조회
- 토큰 삭제 (로그아웃)

**작업:**
- [x] `TokenManager.kt` 생성
- [x] SharedPreferences로 토큰 저장/로드 구현
- [x] `WhEatsApplication.kt` 생성 (TokenManager 초기화)

---

#### **Step 5-3: Authorization Interceptor 추가**

**파일:** `mobile/app/src/main/java/com/example/mobile/data/network/ApiClient.kt`

**수정 내용:**
```kotlin
private val okHttpClient = OkHttpClient.Builder()
    .connectTimeout(30, TimeUnit.SECONDS)
    .readTimeout(30, TimeUnit.SECONDS)
    .writeTimeout(30, TimeUnit.SECONDS)
    .addInterceptor(loggingInterceptor)
    .addInterceptor { chain ->
        val token = TokenManager.getToken()
        val request = if (token != null) {
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $token")
                .build()
        } else {
            chain.request()
        }
        chain.proceed(request)
    }
    .build()
```

**작업:**
- [x] `ApiClient.kt`에 Authorization Interceptor 추가
- [x] `TokenManager` 연동
- [x] `AuthApi` 추가

---

#### **Step 5-4: 로그인 Activity 구현**

**파일:** `mobile/app/src/main/java/com/example/mobile/ui/auth/LoginActivity.kt` (새로 생성)
**레이아웃:** `mobile/app/src/main/res/layout/activity_login.xml` (새로 생성)

**기능:**
- **이메일만 입력** (비밀번호 입력 필드 없음)
- 로그인 버튼
- 로그인 성공 시 토큰 저장 및 메인 화면 이동
- 로그인 실패 시 에러 메시지 표시

**레이아웃 예시:**
```xml
<EditText
    android:id="@+id/etEmail"
    android:hint="이메일 입력"
    android:inputType="textEmailAddress" />

<Button
    android:id="@+id/btnLogin"
    android:text="로그인" />
```

**작업:**
- [x] `LoginActivity.kt` 생성
- [x] `activity_login.xml` 레이아웃 생성 (이메일 입력만)
- [x] 로그인 로직 구현

---

#### **Step 5-5: 앱 시작 시 로그인 체크**

**파일:** `mobile/app/src/main/java/com/example/mobile/ui/storelist/StoreListActivity.kt`

**수정 내용:**
```kotlin
override fun onCreate(savedInstanceState: Bundle?) {
    super.onCreate(savedInstanceState)
    
    // 로그인 체크
    if (!TokenManager.hasToken()) {
        val intent = Intent(this, LoginActivity::class.java)
        startActivity(intent)
        finish()
        return
    }
    
    setContentView(R.layout.activity_store_list)
    // ...
}
```

**작업:**
- [x] `StoreListActivity`에 로그인 체크 추가
- [x] AndroidManifest.xml에 LoginActivity 등록
- [x] AndroidManifest.xml에 Application 클래스 등록

---

#### **Step 5-6: 로그아웃 기능 추가**

**파일:** `mobile/app/src/main/java/com/example/mobile/ui/mypage/MyPageActivity.kt`

**추가 기능:**
- 로그아웃 버튼
- 토큰 삭제
- LoginActivity로 이동

**작업:**
- [x] 마이페이지에 로그아웃 버튼 추가
- [x] 로그아웃 로직 구현

---

### **Phase 6: 테스트 및 DB 초기화**

#### **Step 6-1: 테스트 계정 확인**

**파일:** `db/init.sql` 확인

**현재 상태:**
- `init.sql`에 이미 테스트 계정이 등록되어 있음:
  - `consumer1@wheats.local` (id=1, CONSUMER)
  - `merchant1@wheats.local` (id=2, MERCHANT)

**추가 테스트 계정 (선택):**
```sql
-- 추가 테스트 계정 생성 (비밀번호 필드 없음)
INSERT INTO users (name, email, role, point) VALUES
  ('테스트 사용자', 'test@example.com', 'CONSUMER', 100000);
```

**작업:**
- [x] 비밀번호 설정 불필요 (이메일만으로 인증)
- [ ] 테스트 계정 이메일 확인 (기존 계정 사용 또는 새로 생성)

---

#### **Step 6-2: 전체 플로우 테스트**

**테스트 시나리오:**
1. 안드로이드 앱 실행 → 로그인 화면 표시
2. **이메일만 입력** → 로그인 성공 (비밀번호 없음)
3. 메인 화면 이동
4. 장바구니 추가 → 성공 (인증된 사용자)
5. 주문하기 → 성공
6. 마이페이지 조회 → 성공
7. 1:1 문의 작성 → 성공
8. 로그아웃 → 로그인 화면으로 이동

**작업:**
- [ ] 각 기능별 테스트
- [ ] 에러 케이스 테스트 (존재하지 않는 이메일, 만료된 토큰 등)

---

## ✅ 체크리스트

### Phase 1: 서버 인증 인프라
- [x] Step 1-1: DB 스키마 확인 (수정 불필요)
- [x] Step 1-2: JWT 의존성 추가
- [x] Step 1-3: UserEntity 수정 (불필요, password 필드 없음)
- [x] Step 1-4: JwtUtil 생성
- [x] Step 1-5: PasswordUtil 생성 (불필요)

### Phase 2: 로그인 API
- [x] Step 2-1: 로그인 DTO 생성
- [x] Step 2-2: AuthService 생성
- [x] Step 2-3: AuthController 생성

### Phase 3: 인증 인터셉터
- [x] Step 3-1: AuthInterceptor 생성
- [x] Step 3-2: 인터셉터 등록
- [x] Step 3-3: AuthContext 생성

### Phase 4: 기존 API 수정
- [x] Step 4-1: MyPageController 수정
- [x] Step 4-2: SupportTicketController 수정
- [x] Step 4-3: CartController 수정
- [x] Step 4-4: OrderService 수정
- [x] Step 4-5: OrderController 수정

### Phase 5: 안드로이드 구현
- [x] Step 5-1: AuthApi 생성
- [x] Step 5-2: TokenManager 생성
- [x] Step 5-3: Authorization Interceptor 추가
- [x] Step 5-4: LoginActivity 구현
- [x] Step 5-5: 로그인 체크 추가
- [x] Step 5-6: 로그아웃 기능

### Phase 6: 테스트
- [ ] Step 6-1: 테스트 계정 생성
- [ ] Step 6-2: 전체 플로우 테스트

---

## 📚 참고사항

### 보안 고려사항
1. **JWT Secret Key**: 프로덕션에서는 환경변수로 관리
2. **⚠️ 개발용 인증**: 현재는 이메일만으로 인증 (비밀번호 없음)
   - 프로덕션에서는 절대 사용하지 않음
   - 나중에 OAuth로 교체 예정
3. **HTTPS**: 프로덕션에서는 반드시 HTTPS 사용
4. **토큰 만료**: 24시간 또는 적절한 시간 설정

### 향후 OAuth 통합
- 현재 구현한 JWT 기반 인증은 나중에 OAuth로 교체 예정
- `AuthContext.getCurrentUserId()` 부분만 OAuth 토큰에서 추출하도록 변경
- 로그인 API (`/api/auth/login`)는 OAuth 인증으로 대체
- 이메일만 입력하는 방식은 개발용이며, OAuth 통합 시 제거

---

**작성일:** 2024년
**목적:** 로그인 기능 및 DB 연동 구현 단계
