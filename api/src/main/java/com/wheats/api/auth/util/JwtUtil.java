package com.wheats.api.auth.util;

import com.wheats.api.mypage.entity.UserRole;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    // ⚠️ 프로덕션에서는 환경변수로 관리해야 함
    private static final String SECRET_KEY = "wheats-secret-key-for-jwt-token-generation-change-in-production";
    private static final long EXPIRATION_TIME = 86400000; // 24시간 (밀리초)

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(SECRET_KEY.getBytes(StandardCharsets.UTF_8));
    }

    /**
     * JWT 토큰 생성
     * @param userId 사용자 ID
     * @param role 사용자 권한 (CONSUMER, MERCHANT, ADMIN)
     * @return JWT 토큰 문자열
     */
    public String generateToken(Long userId, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + EXPIRATION_TIME);

        return Jwts.builder()
                .subject(String.valueOf(userId))
                .claim("role", role)  // 권한 정보 추가
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();
    }

    /**
     * JWT 토큰 생성 (UserRole enum 사용)
     * @param userId 사용자 ID
     * @param userRole UserRole enum (CONSUMER, MERCHANT, ADMIN)
     * @return JWT 토큰 문자열
     */
    public String generateToken(Long userId, UserRole userRole) {
        String role = convertUserRoleToScope(userRole);
        return generateToken(userId, role);
    }

    /**
     * UserRole enum을 OAuth scope로 변환
     * @param userRole UserRole enum
     * @return scope 문자열 (customer, store, admin)
     */
    public String convertUserRoleToScope(UserRole userRole) {
        if (userRole == null) {
            return null;
        }
        return switch (userRole) {
            case CONSUMER -> "CONSUMER";
            case MERCHANT -> "MERCHANT";
            case ADMIN -> "ADMIN";
        };
    }

    /**
     * JWT 토큰 생성 (기존 호환성 유지)
     * @param userId 사용자 ID
     * @return JWT 토큰 문자열
     * @deprecated role 정보가 없는 토큰 생성. generateToken(Long, String) 사용 권장
     */
    @Deprecated
    public String generateToken(Long userId) {
        return generateToken(userId, (String) null);
    }

    /**
     * JWT 토큰에서 userId 추출
     * @param token JWT 토큰
     * @return 사용자 ID
     */
    public Long getUserIdFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return Long.parseLong(claims.getSubject());
    }

    /**
     * JWT 토큰 검증
     * @param token JWT 토큰
     * @return 유효하면 true, 아니면 false
     */
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                    .verifyWith(getSigningKey())
                    .build()
                    .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    /**
     * JWT 토큰에서 권한(role) 추출
     * @param token JWT 토큰
     * @return 사용자 권한 (customer, store, admin) 또는 null
     */
    public String getRoleFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.get("role", String.class);
    }

    /**
     * JWT 토큰에서 만료 시간 추출
     * @param token JWT 토큰
     * @return 만료 시간
     */
    public Date getExpirationDateFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getExpiration();
    }
}
