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
     * @param accessToken OAuth Access Token
     * @return wheats DB의 UserEntity (Optional) - 토큰이 유효하고 사용자가 존재하면 UserEntity 반환
     */
    public Optional<UserEntity> validateTokenAndGetUser(String accessToken) {
        try {
            // 1. OAuth 서버의 /api/me 엔드포인트 호출하여 토큰 검증 및 사용자 정보 조회
            HttpHeaders headers = new HttpHeaders();
            headers.set("Authorization", "Bearer " + accessToken);
            HttpEntity<String> entity = new HttpEntity<>(headers);

            String url = oauthServerUrl + "/api/me";
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

            return Optional.empty();
        } catch (Exception e) {
            // OAuth 서버 통신 실패 또는 토큰이 유효하지 않음
            System.err.println("❌ OAuth token validation failed: " + e.getMessage());
            e.printStackTrace();
            return Optional.empty();
        }
    }

}

