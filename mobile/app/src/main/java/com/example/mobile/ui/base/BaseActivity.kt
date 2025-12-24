package com.example.mobile.ui.base

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.data.auth.AuthStateManager
import com.example.mobile.ui.auth.LoginActivity
import com.example.mobile.ui.mypage.MyPageActivity
import com.example.mobile.ui.storelist.StoreListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected var bottomNavigation: BottomNavigationView? = null

    companion object {
        private const val TAG = "BaseActivity"
    }

    /**
     * 이 Activity가 인증(토큰 유효성) 체크가 필요한지 여부.
     * - 기본값: true (대부분의 보호된 화면)
     * - 예외: 가게 목록/가게 상세처럼 공개 화면은 false로 override
     */
    protected open fun requiresAuth(): Boolean = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // LoginActivity는 토큰 검증 제외
        if (this !is LoginActivity && requiresAuth()) {
            Log.d(TAG, "BaseActivity.onCreate() called from ${this::class.java.simpleName}")
            // 토큰이 유효하지 않으면 onCreate를 계속 진행하지 않음
            if (!checkAuthAndRedirect()) {
                Log.w(TAG, "checkAuthAndRedirect() returned false, stopping onCreate()")
                return
            }
            Log.d(TAG, "checkAuthAndRedirect() returned true, continuing onCreate()")
        }
    }
    
    /**
     * OAuth 토큰 유효성을 확인하고, 만료되었거나 유효하지 않으면 로그인 화면으로 리다이렉트
     * 모든 BaseActivity를 상속받는 Activity에서 자동으로 호출됨
     * 
     * @return 토큰이 유효하면 true, 유효하지 않으면 false
     */
    protected fun checkAuthAndRedirect(): Boolean {
        val authStateManager = AuthStateManager.getInstance(this)
        val authState = authStateManager.current
        val accessToken = authState.accessToken
        val expirationTime = authState.accessTokenExpirationTime
        
        // 토큰이 없거나 만료되었는지 확인 (LoginActivity와 동일하게 60초 여유 필요)
        // accessTokenExpirationTime은 밀리초 단위이므로 밀리초로 비교
        val currentTimeMs = System.currentTimeMillis()
        val timeUntilExpirySeconds = if (expirationTime != null) {
            (expirationTime - currentTimeMs) / 1000
        } else {
            0
        }
        val isTokenValid = accessToken != null && expirationTime != null && timeUntilExpirySeconds > 60
        
        if (!authState.isAuthorized || !isTokenValid) {
            Log.w(TAG, "Token invalid or expired. Redirecting to login.")
            Log.w(TAG, "  isAuthorized: ${authState.isAuthorized}")
            Log.w(TAG, "  accessToken: ${if (accessToken != null) "${accessToken.take(10)}..." else "null"}")
            Log.w(TAG, "  expirationTime: $expirationTime (ms)")
            Log.w(TAG, "  currentTime: $currentTimeMs (ms)")
            Log.w(TAG, "  timeUntilExpiry: $timeUntilExpirySeconds seconds")
            
            // 만료된 토큰 삭제
            if (accessToken != null) {
                authStateManager.clear()
            }
            
            redirectToLogin(clearAuth = accessToken != null)
            return false
        }
        
        Log.d(TAG, "Token valid. Time until expiry: $timeUntilExpirySeconds seconds")
        return true
    }

    /**
     * 현재 로그인 상태(토큰 존재 + 만료 여유 60초)를 반환.
     * 공개 화면에서 "보호 기능 호출" 전에 체크할 때 사용.
     */
    protected fun isLoggedIn(): Boolean {
        val authState = AuthStateManager.getInstance(this).current
        val accessToken = authState.accessToken
        val expirationTime = authState.accessTokenExpirationTime

        val currentTimeMs = System.currentTimeMillis()
        val timeUntilExpirySeconds = if (expirationTime != null) {
            (expirationTime - currentTimeMs) / 1000
        } else {
            0
        }
        return authState.isAuthorized && accessToken != null && expirationTime != null && timeUntilExpirySeconds > 60
    }

    /**
     * 로그인 화면으로 자연스럽게 이동(401 등 인증 필요 상황에서 사용)
     */
    protected fun redirectToLogin(clearAuth: Boolean = true) {
        if (clearAuth) {
            AuthStateManager.getInstance(this).clear()
        }
        val intent = Intent(this, LoginActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun setContentView(layoutResID: Int) {
        super.setContentView(layoutResID)
        setupBottomNavigation()
    }

    override fun onResume() {
        super.onResume()
        updateSelectedItem()
    }

    private fun setupBottomNavigation() {
        bottomNavigation = findViewById(R.id.bottomNavigation)
        bottomNavigation?.let { nav ->
            nav.setOnItemSelectedListener { item ->
                when (item.itemId) {
                    R.id.nav_home -> {
                        if (this !is StoreListActivity) {
                            val intent = Intent(this, StoreListActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                        }
                        true
                    }
                    R.id.nav_mypage -> {
                        if (this !is MyPageActivity) {
                            val intent = Intent(this, MyPageActivity::class.java)
                            intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
                            startActivity(intent)
                        }
                        true
                    }
                    else -> false
                }
            }
            
            // 현재 액티비티에 따라 선택된 아이템 표시
            updateSelectedItem()
        }
    }

    protected fun updateSelectedItem() {
        bottomNavigation?.let { nav ->
            when (this) {
                is StoreListActivity -> nav.selectedItemId = R.id.nav_home
                is MyPageActivity -> nav.selectedItemId = R.id.nav_mypage
                else -> nav.selectedItemId = -1 // 선택 없음
            }
        }
    }
}
