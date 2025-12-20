package com.wheats.api.auth.controller;

import com.wheats.api.auth.dto.ErrorResponse;
import com.wheats.api.auth.dto.LoginRequest;
import com.wheats.api.auth.dto.LoginResponse;
import com.wheats.api.auth.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;

    public AuthController(AuthService authService) {
        this.authService = authService;
        // 디버깅: 컨트롤러가 등록되었는지 확인
        System.out.println("✅ AuthController가 등록되었습니다. /api/auth/login 엔드포인트 사용 가능");
    }

    /**
     * 테스트 엔드포인트 (서버 등록 확인용)
     * GET /api/auth/test
     */
    @GetMapping("/test")
    public ResponseEntity<String> test() {
        return ResponseEntity.ok("AuthController가 정상적으로 등록되었습니다!");
    }

    /**
     * 로그인 API
     * POST /api/auth/login
     * 
     * Request Body:
     * {
     *   "email": "user@example.com"
     * }
     * 
     * Response (성공):
     * {
     *   "token": "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9...",
     *   "userId": 1,
     *   "name": "사용자 이름",
     *   "email": "user@example.com",
     *   "role": "CONSUMER"
     * }
     * 
     * Response (사용자 없음 - 400):
     * {
     *   "message": "이메일 또는 비밀번호를 확인해주세요.",
     *   "error": "USER_NOT_FOUND"
     * }
     * 
     * Response (서버 오류 - 500):
     * {
     *   "message": "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
     *   "error": "INTERNAL_SERVER_ERROR"
     * }
     */
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest request) {
        System.out.println("🔍 /api/auth/login 엔드포인트 호출됨. email: " + request.getEmail());
        try {
            LoginResponse response = authService.login(request);
            System.out.println("✅ 로그인 성공: userId=" + response.getUserId());
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            // 사용자를 찾을 수 없을 때 400 Bad Request 반환
            // (404는 URL을 찾을 수 없을 때 사용하므로, 잘못된 요청이므로 400이 적절)
            ErrorResponse errorResponse = new ErrorResponse(
                    "이메일 또는 비밀번호를 확인해주세요.",
                    "USER_NOT_FOUND"
            );
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponse);
        } catch (Exception e) {
            // 기타 서버 오류 (DB 연결 실패, JWT 생성 실패 등)
            e.printStackTrace(); // 로그 출력
            ErrorResponse errorResponse = new ErrorResponse(
                    "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요.",
                    "INTERNAL_SERVER_ERROR"
            );
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(errorResponse);
        }
    }
}
