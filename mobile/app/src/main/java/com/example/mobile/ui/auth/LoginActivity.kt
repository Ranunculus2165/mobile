package com.example.mobile.ui.auth

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.data.auth.AuthStateManager
import com.example.mobile.data.auth.OAuthConfig
import com.example.mobile.ui.storelist.StoreListActivity
import net.openid.appauth.*

class LoginActivity : AppCompatActivity() {

    private lateinit var authService: AuthorizationService
    private lateinit var authStateManager: AuthStateManager

    companion object {
        private const val TAG = "LoginActivity"
        private const val RC_AUTH = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        // OAuth 설정 (HTTP 허용)
        val appAuthConfig = AppAuthConfiguration.Builder()
            .setConnectionBuilder { uri ->
                val url = java.net.URL(uri.toString())
                val conn = url.openConnection() as java.net.HttpURLConnection
                conn.instanceFollowRedirects = false
                conn
            }
            .build()

        authService = AuthorizationService(this, appAuthConfig)
        authStateManager = AuthStateManager.getInstance(this)

        // 토큰 유효성 확인 후 메인 화면으로 이동
        val authState = authStateManager.current
        val accessToken = authState.accessToken
        val expirationTime = authState.accessTokenExpirationTime
        
        Log.d(TAG, "Checking auth state:")
        Log.d(TAG, "  isAuthorized: ${authState.isAuthorized}")
        Log.d(TAG, "  accessToken: ${if (accessToken != null) "${accessToken.take(10)}..." else "null"}")
        Log.d(TAG, "  expirationTime: $expirationTime")
        
        // 토큰이 존재하고 유효한지 엄격하게 확인
        // accessTokenExpirationTime은 밀리초 단위이므로 밀리초로 비교
        if (accessToken != null && expirationTime != null) {
            val currentTimeMs = System.currentTimeMillis()
            val timeUntilExpirySeconds = (expirationTime - currentTimeMs) / 1000
            val isExpired = expirationTime <= currentTimeMs
            
            Log.d(TAG, "  currentTime: $currentTimeMs (ms)")
            Log.d(TAG, "  timeUntilExpiry: $timeUntilExpirySeconds seconds")
            Log.d(TAG, "  isExpired: $isExpired")
            
            // 토큰이 유효하고 만료되지 않았는지 확인 (최소 60초 여유)
            if (!isExpired && timeUntilExpirySeconds > 60) {
                Log.d(TAG, "Valid token found, navigating to store list")
                navigateToStoreList()
                return
            } else {
                Log.w(TAG, "Token expired or expiring soon. Clearing token and requiring re-login")
                Log.w(TAG, "  Expiration: $expirationTime (ms), Current: $currentTimeMs (ms), Time until expiry: $timeUntilExpirySeconds seconds")
                authStateManager.clear()
            }
        } else {
            Log.w(TAG, "Token missing or invalid (accessToken: ${accessToken != null}, expirationTime: ${expirationTime != null})")
            Log.w(TAG, "Clearing invalid token and requiring re-login")
            authStateManager.clear()
        }

        setupUI()
    }

    private fun setupUI() {
        // WhiteHat 로그인 버튼 클릭 시 Customer + Profile scope로 로그인
        // Profile scope는 OAuth 서버의 /api/me 엔드포인트 접근에 필요
        findViewById<Button>(R.id.btnLoginCustomer).setOnClickListener {
            startOAuthLogin("${OAuthConfig.SCOPE_CUSTOMER} ${OAuthConfig.SCOPE_PROFILE}", forceLogin = true)
        }
    }

    private fun startOAuthLogin(scope: String, forceLogin: Boolean = false) {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse(OAuthConfig.AUTHORIZATION_ENDPOINT),
            Uri.parse(OAuthConfig.TOKEN_ENDPOINT)
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            OAuthConfig.CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(OAuthConfig.REDIRECT_URI)
        ).setScope(scope)

        // forceLogin이 true면 setPrompt("login")을 사용하여 강제 로그인
        if (forceLogin) {
            authRequestBuilder.setPrompt("login")
        }

        // PKCE 활성화
        val authRequest = authRequestBuilder.build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_AUTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            val response = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)

            authStateManager.updateAfterAuthorization(response, ex)

            if (response != null) {
                Log.d(TAG, "Authorization successful, exchanging code for token")
                exchangeAuthorizationCode(response)
            } else {
                Log.e(TAG, "Authorization failed: ${ex?.error}")
                Toast.makeText(this, "로그인에 실패했습니다: ${ex?.error}", Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun exchangeAuthorizationCode(authResponse: AuthorizationResponse) {
        val tokenRequest = authResponse.createTokenExchangeRequest()

        authService.performTokenRequest(tokenRequest) { tokenResponse, exception ->
            runOnUiThread {
                if (tokenResponse != null) {
                    Log.d(TAG, "Token exchange successful")
                    Log.d(TAG, "Access Token: ${tokenResponse.accessToken}")
                    Log.d(TAG, "Refresh Token: ${tokenResponse.refreshToken}")
                    Log.d(TAG, "Scope: ${tokenResponse.scope}")
                    val expiresAtMs = tokenResponse.accessTokenExpirationTime
                    val expiresInSeconds = expiresAtMs?.let { (it - System.currentTimeMillis()) / 1000 }
                    Log.d(TAG, "Expires At: $expiresAtMs (ms)")
                    Log.d(TAG, "Expires In: $expiresInSeconds seconds")

                    // AuthState 저장
                    authStateManager.updateAfterTokenResponse(tokenResponse, exception)
                    
                    // 저장 후 확인
                    val savedState = authStateManager.current
                    Log.d(TAG, "Saved AuthState:")
                    Log.d(TAG, "  isAuthorized: ${savedState.isAuthorized}")
                    Log.d(TAG, "  accessToken: ${savedState.accessToken?.take(10)}...")
                    val savedExpiresAtMs = savedState.accessTokenExpirationTime
                    val savedExpiresInSeconds = savedExpiresAtMs?.let { (it - System.currentTimeMillis()) / 1000 }
                    Log.d(TAG, "  expirationTime: $savedExpiresAtMs (ms)")
                    Log.d(TAG, "  timeUntilExpiry: $savedExpiresInSeconds seconds")

                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
                    
                    // 바로 이동 (commit()으로 동기 저장되므로 딜레이 불필요)
                    navigateToStoreList()
                } else {
                    Log.e(TAG, "Token exchange failed", exception)
                    Toast.makeText(
                        this,
                        "토큰 교환에 실패했습니다: ${exception?.error}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }
    }

    private fun navigateToStoreList() {
        val intent = Intent(this, StoreListActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}
