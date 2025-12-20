package com.example.mobile.data.auth

import android.content.Context
import android.content.SharedPreferences

object TokenManager {

    private const val PREFS_NAME = "auth_prefs"
    private const val KEY_TOKEN = "jwt_token"
    private const val KEY_USER_ID = "user_id"
    private const val KEY_USER_NAME = "user_name"
    private const val KEY_USER_EMAIL = "user_email"
    private const val KEY_USER_ROLE = "user_role"

    private var context: Context? = null

    fun init(context: Context) {
        TokenManager.context = context.applicationContext
    }

    private fun getPrefs(): SharedPreferences {
        val ctx = context ?: throw IllegalStateException("TokenManager가 초기화되지 않았습니다. Application에서 init()을 호출하세요.")
        return ctx.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
    }

    /**
     * 토큰 저장
     */
    fun saveToken(token: String, userId: Long, name: String, email: String, role: String) {
        getPrefs().edit()
            .putString(KEY_TOKEN, token)
            .putLong(KEY_USER_ID, userId)
            .putString(KEY_USER_NAME, name)
            .putString(KEY_USER_EMAIL, email)
            .putString(KEY_USER_ROLE, role)
            .apply()
    }

    /**
     * JWT 토큰 조회
     */
    fun getToken(): String? {
        return getPrefs().getString(KEY_TOKEN, null)
    }

    /**
     * 사용자 ID 조회
     */
    fun getUserId(): Long {
        return getPrefs().getLong(KEY_USER_ID, -1L)
    }

    /**
     * 사용자 이름 조회
     */
    fun getUserName(): String? {
        return getPrefs().getString(KEY_USER_NAME, null)
    }

    /**
     * 사용자 이메일 조회
     */
    fun getUserEmail(): String? {
        return getPrefs().getString(KEY_USER_EMAIL, null)
    }

    /**
     * 사용자 권한 조회
     */
    fun getUserRole(): String? {
        return getPrefs().getString(KEY_USER_ROLE, null)
    }

    /**
     * 토큰 존재 여부 확인
     */
    fun hasToken(): Boolean {
        return getToken() != null
    }

    /**
     * 모든 인증 정보 삭제 (로그아웃)
     */
    fun clearToken() {
        getPrefs().edit()
            .remove(KEY_TOKEN)
            .remove(KEY_USER_ID)
            .remove(KEY_USER_NAME)
            .remove(KEY_USER_EMAIL)
            .remove(KEY_USER_ROLE)
            .apply()
    }
}
