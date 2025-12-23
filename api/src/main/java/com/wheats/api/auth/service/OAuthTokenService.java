package com.wheats.api.auth.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.wheats.api.mypage.entity.UserEntity;
import com.wheats.api.mypage.repository.UserRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.Optional;

/**
 * OAuth 서버와 통신하여 토큰을 검증하고 사용자 정보를 가져오는 서비스
 */
@Service
public class OAuthTokenService {

    private final RestTemplate restTemplate;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;

    @Value("${oauth.server.url:http://oauth-server:3000}")
    private String oauthServerUrl;

    public OAuthTokenService(UserRepository userRepository) {
        this.restTemplate = new RestTemplate();
        this.objectMapper = new ObjectMapper();
        this.userRepository = userRepository;
    }

    /**
     * OAuth Access Token을 검증하고 사용자 정보를 가져옴
     * 
     * 처리 과정:
     * 1. OAuth 서버의 /api/me 엔드포인트를 호출하여 토큰 검증 및 사용자 정보 조회
     * 2. 응답에서 이메일 추출
     * 3. 추출한 이메일로 Wheats DB의 User 테이블에서 사용자 조회
     * 
     * @param accessToken OAuth Access Token (Bearer 접두사 없이 전달됨)
     * @return wheats DB의 UserEntity (Optional) - 토큰이 유효하고 사용자가 존재하면 UserEntity 반환
     */
    public Optional<UserEntity> validateTokenAndGetUser(String accessToken) {
        // 디버그: 전달된 토큰 정보 로깅 (보안을 위해 일부만)
        String tokenPreview = accessToken != null && accessToken.length() > 15 
            ? accessToken.substring(0, 10) + "..." + accessToken.substring(accessToken.length() - 5)
            : (accessToken != null ? accessToken : "null");
        System.out.println("🔍 OAuth Token Validation Request:");
        System.out.println("   Token Length: " + (accessToken != null ? accessToken.length() : 0));
        System.out.println("   Token Preview: " + tokenPreview);
        System.out.println("   OAuth Server URL: " + oauthServerUrl + "/api/me");
        
        try {
            // 1. OAuth 서버의 /api/me 엔드포인트 호출하여 토큰 검증 및 사용자 정보 조회
            HttpHeaders headers = new HttpHeaders();
            
            // Bearer 접두사 확인 및 추가
            String bearerToken = accessToken;
            if (accessToken != null && !accessToken.startsWith("Bearer ")) {
                bearerToken = "Bearer " + accessToken;
            }
            headers.set("Authorization", bearerToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = oauthServerUrl + "/api/me";
            System.out.println("   Request URL: " + url);
            System.out.println("   Authorization Header: " + bearerToken.substring(0, Math.min(20, bearerToken.length())) + "...");
            
            ResponseEntity<String> response = restTemplate.exchange(
                    url,
                    HttpMethod.GET,
                    entity,
                    String.class
            );

            if (response.getStatusCode().is2xxSuccessful() && response.getBody() != null) {
                // 2. JSON 응답 파싱하여 이메일 추출
                JsonNode jsonNode = objectMapper.readTree(response.getBody());
                String email = jsonNode.get("email").asText();

                // 3. 추출한 이메일로 Wheats DB의 User 테이블에서 사용자 조회
                Optional<UserEntity> userOpt = userRepository.findByEmail(email);
                
                if (userOpt.isPresent()) {
                    System.out.println("✅ OAuth 토큰 검증 성공: email=" + email + ", userId=" + userOpt.get().getId());
                } else {
                    System.out.println("⚠️ OAuth 토큰은 유효하지만 Wheats DB에 해당 사용자가 없음: email=" + email);
                }
                
                return userOpt;
            }

            System.out.println("⚠️ OAuth 서버 응답: " + response.getStatusCode() + " (Body: " + response.getBody() + ")");
            return Optional.empty();
        } catch (org.springframework.web.client.HttpClientErrorException.Unauthorized e) {
            // 401 Unauthorized 에러 상세 로깅
            System.err.println("❌ OAuth token validation failed: 401 UNAUTHORIZED");
            System.err.println("   Token Preview: " + tokenPreview);
            System.err.println("   Response Body: " + e.getResponseBodyAsString());
            System.err.println("   Status Code: " + e.getStatusCode());
            System.err.println("   Status Text: " + e.getStatusText());
            
            // 응답 본문에서 에러 상세 정보 추출
            try {
                if (e.getResponseBodyAsString() != null) {
                    JsonNode errorNode = objectMapper.readTree(e.getResponseBodyAsString());
                    String error = errorNode.has("error") ? errorNode.get("error").asText() : "unknown";
                    String errorDescription = errorNode.has("error_description") 
                        ? errorNode.get("error_description").asText() 
                        : "No description";
                    System.err.println("   Error: " + error);
                    System.err.println("   Error Description: " + errorDescription);
                }
            } catch (Exception parseEx) {
                System.err.println("   Failed to parse error response: " + parseEx.getMessage());
            }
            
            e.printStackTrace();
            return Optional.empty();
        } catch (Exception e) {
            // 기타 예외 처리
            System.err.println("❌ OAuth token validation failed: " + e.getClass().getSimpleName());
            System.err.println("   Token Preview: " + tokenPreview);
            System.err.println("   Error Message: " + e.getMessage());
            System.err.println("   OAuth Server URL: " + oauthServerUrl + "/api/me");
            
            // 연결 실패인지 확인
            if (e.getCause() instanceof java.net.ConnectException) {
                System.err.println("   ⚠️ OAuth 서버에 연결할 수 없습니다. 서버가 실행 중인지 확인하세요.");
            } else if (e.getCause() instanceof java.net.UnknownHostException) {
                System.err.println("   ⚠️ OAuth 서버 호스트를 찾을 수 없습니다: " + oauthServerUrl);
            }
            
            e.printStackTrace();
            return Optional.empty();
        }
    }

}

