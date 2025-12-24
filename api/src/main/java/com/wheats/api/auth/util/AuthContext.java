package com.wheats.api.auth.util;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

public class AuthContext {

    private static final String USER_ID_ATTRIBUTE = "userId";
    private static final String USER_ROLE_ATTRIBUTE = "role";

    /**
     * 현재 요청에서 사용자 ID 추출
     * @return 사용자 ID
     * @throws IllegalStateException 요청 컨텍스트가 없거나 인증되지 않은 경우
     */
    public static Long getCurrentUserId() {
        HttpServletRequest request = getCurrentRequest();
        Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
        
        if (userId == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        
        return (Long) userId;
    }

    /**
     * 현재 요청에서 사용자 권한 추출
     * @return 사용자 권한 (CONSUMER, MERCHANT, ADMIN)
     * @throws IllegalStateException 요청 컨텍스트가 없거나 인증되지 않은 경우
     */
    public static String getCurrentUserRole() {
        HttpServletRequest request = getCurrentRequest();
        Object role = request.getAttribute(USER_ROLE_ATTRIBUTE);
        
        if (role == null) {
            throw new IllegalStateException("인증된 사용자 정보를 찾을 수 없습니다.");
        }
        
        return (String) role;
    }

    /**
     * 현재 요청에서 사용자 ID를 안전하게 추출 (없으면 null 반환)
     * @return 사용자 ID 또는 null
     */
    public static Long getCurrentUserIdOrNull() {
        try {
            HttpServletRequest request = getCurrentRequest();
            Object userId = request.getAttribute(USER_ID_ATTRIBUTE);
            return userId != null ? (Long) userId : null;
        } catch (Exception e) {
            return null;
        }
    }

    /**
     * 현재 HTTP 요청 가져오기
     * @return HttpServletRequest
     * @throws IllegalStateException 요청 컨텍스트가 없는 경우
     */
    private static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        
        if (attributes == null) {
            throw new IllegalStateException("요청 컨텍스트를 찾을 수 없습니다.");
        }
        
        return attributes.getRequest();
    }
}
