package com.example.mobile.data.auth

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import net.openid.appauth.AuthState
import org.json.JSONException

/**
 * OAuth 2.0 인증 상태를 관리하는 싱글톤 클래스
 * SharedPreferences를 사용하여 AuthState를 영구 저장
 */
class AuthStateManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    private val lock = Object()

    /**
     * 현재 저장된 AuthState를 반환
     * 저장된 상태가 없거나 파싱 실패 시 빈 AuthState 반환
     */
    var current: AuthState
        get() {
            synchronized(lock) {
                val stateJson = prefs.getString(KEY_STATE, null)
                return if (stateJson != null) {
                    try {
                        AuthState.jsonDeserialize(stateJson)
                    } catch (ex: JSONException) {
                        Log.w(TAG, "Failed to deserialize stored auth state - discarding", ex)
                        AuthState()
                    }
                } else {
                    AuthState()
                }
            }
        }
        private set(value) {
            synchronized(lock) {
                val stateJson = value.jsonSerializeString()
                prefs.edit()
                    .putString(KEY_STATE, stateJson)
                    .apply()
            }
        }

    /**
     * AuthState를 완전히 교체
     */
    fun replace(state: AuthState) {
        synchronized(lock) {
            current = state
        }
    }

    /**
     * Authorization 응답 후 AuthState 업데이트
     */
    fun updateAfterAuthorization(
        response: net.openid.appauth.AuthorizationResponse?,
        ex: net.openid.appauth.AuthorizationException?
    ) {
        synchronized(lock) {
            val state = current
            state.update(response, ex)
            current = state
        }
    }

    /**
     * Token 응답 후 AuthState 업데이트
     */
    fun updateAfterTokenResponse(
        response: net.openid.appauth.TokenResponse?,
        ex: net.openid.appauth.AuthorizationException?
    ) {
        synchronized(lock) {
            val state = current
            state.update(response, ex)
            current = state
        }
    }

    /**
     * 모든 인증 상태 삭제 (로그아웃)
     */
    fun clear() {
        synchronized(lock) {
            prefs.edit()
                .remove(KEY_STATE)
                .apply()
        }
    }

    companion object {
        private const val TAG = "AuthStateManager"
        private const val STORE_NAME = "oauth_auth_prefs"
        private const val KEY_STATE = "auth_state"

        @Volatile
        private var INSTANCE: AuthStateManager? = null

        /**
         * 싱글톤 인스턴스 반환
         * @param context Application Context 권장
         */
        fun getInstance(context: Context): AuthStateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthStateManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}

