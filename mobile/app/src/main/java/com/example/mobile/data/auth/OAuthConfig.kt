package com.example.mobile.data.auth

/**
 * OAuth 2.0 설정 상수
 * OAuth 서버와의 통신에 필요한 모든 설정값을 관리
 */
object OAuthConfig {
    /**
     * OAuth 서버 URL
     * - 에뮬레이터: 10.0.2.2 (호스트 머신의 localhost)
     * - 실제 기기: 호스트 머신의 IP 주소 (예: 192.168.0.100)
     * - Docker: localhost 또는 컨테이너 IP
     */
    const val AUTH_SERVER_URL = "http://10.0.2.2:3000"
    
    /**
     * OAuth 클라이언트 ID
     * OAuth 서버에 등록된 클라이언트 식별자
     */
    const val CLIENT_ID = "android_app_client"
    
    /**
     * OAuth 클라이언트 Secret
     * ⚠️ 데모/테스트용 - 프로덕션에서는 제거하거나 안전하게 관리해야 함
     */
    const val CLIENT_SECRET = "secret123"
    
    /**
     * OAuth Redirect URI
     * AndroidManifest.xml의 intent-filter와 일치해야 함
     * 형식: {applicationId}://oauth2callback
     */
    const val REDIRECT_URI = "com.example.mobile://oauth2callback"
    
    /**
     * OAuth Scopes (권한 범위)
     */
    const val SCOPE_CUSTOMER = "customer"  // 고객 권한
    const val SCOPE_STORE = "store"         // 점주 권한
    const val SCOPE_ADMIN = "admin"         // 관리자 권한 (🚨 권한 상승 목표)
    const val SCOPE_PROFILE = "profile"     // 프로필 정보
    
    /**
     * Authorization Endpoint
     */
    val AUTHORIZATION_ENDPOINT = "$AUTH_SERVER_URL/oauth/authorize"
    
    /**
     * Token Endpoint
     */
    val TOKEN_ENDPOINT = "$AUTH_SERVER_URL/oauth/token"
    
    /**
     * Revoke Endpoint
     */
    val REVOKE_ENDPOINT = "$AUTH_SERVER_URL/oauth/revoke"
}
