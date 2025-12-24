package com.example.deliveryapp

import android.content.Context
import android.content.SharedPreferences
import android.util.Log
import net.openid.appauth.AuthState
import org.json.JSONException

class AuthStateManager private constructor(context: Context) {

    private val prefs: SharedPreferences = context.getSharedPreferences(STORE_NAME, Context.MODE_PRIVATE)
    private val lock = Object()

    var current: AuthState
        get() {
            synchronized(lock) {
                val stateJson = prefs.getString(KEY_STATE, null)
                return if (stateJson != null) {
                    try {
                        AuthState.jsonDeserialize(stateJson)
                    } catch (ex: JSONException) {
                        Log.w(TAG, "Failed to deserialize stored auth state - discarding")
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

    fun replace(state: AuthState) {
        synchronized(lock) {
            current = state
        }
    }

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

    companion object {
        private const val TAG = "AuthStateManager"
        private const val STORE_NAME = "auth_prefs"
        private const val KEY_STATE = "state"

        @Volatile
        private var INSTANCE: AuthStateManager? = null

        fun getInstance(context: Context): AuthStateManager {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: AuthStateManager(context.applicationContext).also {
                    INSTANCE = it
                }
            }
        }
    }
}
