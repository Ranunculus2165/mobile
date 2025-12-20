package com.example.mobile.data.auth

object OAuthConfig {
    // OAuth 서버 URL (에뮬레이터: 10.0.2.2, 실제 기기: 호스트 IP)
    const val AUTH_SERVER_URL = "http://10.0.2.2:3000"
    
    // OAuth 클라이언트 정보 (서버에서 등록한 값)
    const val CLIENT_ID = "android_app_client"
    const val CLIENT_SECRET = "secret123"  // ⚠️ 데모용, 프로덕션에서는 제거
    
    // Redirect URI
    const val REDIRECT_URI = "app://oauth2callback"
    
    // Scopes
    const val SCOPE_CUSTOMER = "customer"
    const val SCOPE_STORE = "store"
    const val SCOPE_ADMIN = "admin"  // 🚨 권한 상승 목표
}
