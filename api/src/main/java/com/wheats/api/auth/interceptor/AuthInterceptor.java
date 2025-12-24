package com.wheats.api.auth.interceptor;

import com.wheats.api.auth.service.OAuthTokenService;
import com.wheats.api.mypage.entity.UserEntity;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.Optional;

@Component
public class AuthInterceptor implements HandlerInterceptor {

    private static final String AUTHORIZATION_HEADER = "Authorization";
    private static final String BEARER_PREFIX = "Bearer ";
    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USER_ROLE_ATTRIBUTE = "role";
    private static final String USER_ENTITY_ATTRIBUTE = "user";

    private final OAuthTokenService oauthTokenService;

    public AuthInterceptor(OAuthTokenService oauthTokenService) {
        this.oauthTokenService = oauthTokenService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        // 1. Authorization 헤더에서 토큰 추출
        String authHeader = request.getHeader(AUTHORIZATION_HEADER);
        
        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"인증 토큰이 필요합니다.\"}");
            return false;
        }

        // 2. Bearer 접두사 제거
        String token = authHeader.substring(BEARER_PREFIX.length());

        // 3. OAuth 토큰 검증 및 사용자 정보 가져오기
        //    - OAuth 서버의 /api/me를 호출하여 토큰 검증 및 이메일 추출
        //    - 추출한 이메일로 Wheats DB의 User 테이블에서 사용자 조회
        Optional<UserEntity> userOpt = oauthTokenService.validateTokenAndGetUser(token);
        
        if (userOpt.isEmpty()) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json;charset=UTF-8");
            response.getWriter().write("{\"error\": \"유효하지 않은 토큰입니다.\"}");
            return false;
        }

        UserEntity user = userOpt.get();
        
        // 4. 사용자 정보를 Request Attribute에 저장
        //    - userId: 개별 속성으로 저장 (기존 호환성 유지)
        //    - role: 개별 속성으로 저장 (기존 호환성 유지)
        //    - user: UserEntity 전체 객체 저장 (새로 추가)
        request.setAttribute(USER_ID_ATTRIBUTE, user.getId());
        request.setAttribute(USER_ROLE_ATTRIBUTE, user.getRole().name());
        request.setAttribute(USER_ENTITY_ATTRIBUTE, user);
        
        return true;
    }
}
