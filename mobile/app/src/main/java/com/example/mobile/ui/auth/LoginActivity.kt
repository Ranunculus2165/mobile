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

        // 이미 로그인된 경우 메인 화면으로 이동
        if (authStateManager.current.isAuthorized) {
            Log.d(TAG, "Already logged in, navigating to store list")
            navigateToStoreList()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        // WhiteHat 로그인 버튼 클릭 시 Customer scope로 로그인
        findViewById<Button>(R.id.btnLoginCustomer).setOnClickListener {
            startOAuthLogin(OAuthConfig.SCOPE_CUSTOMER)
        }
    }

    private fun startOAuthLogin(scope: String) {
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

                    // AuthState 저장
                    authStateManager.updateAfterTokenResponse(tokenResponse, exception)

                    Toast.makeText(this, "로그인 성공!", Toast.LENGTH_SHORT).show()
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
