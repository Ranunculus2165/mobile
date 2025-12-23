package com.example.deliveryapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import okhttp3.*
import org.json.JSONObject
import java.io.IOException

class DashboardActivity : AppCompatActivity() {

    private lateinit var authStateManager: AuthStateManager
    private val AUTH_SERVER_URL = "http://10.210.130.19:3000"

    companion object {
        private const val TAG = "DashboardActivity"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_dashboard)

        authStateManager = AuthStateManager.getInstance(this)

        if (!authStateManager.current.isAuthorized) {
            Log.d(TAG, "Not authorized, navigating to login")
            navigateToLogin()
            return
        }

        setupUI()
        loadUserInfo()
    }

    private fun setupUI() {
        val authState = authStateManager.current
        val scope = authState.scope ?: "unknown"

        findViewById<TextView>(R.id.tvScope).text = "Current Scope: $scope"

        findViewById<Button>(R.id.btnTestCustomerAPI).setOnClickListener {
            testAPI("/api/customer/orders", "Customer Orders")
        }

        findViewById<Button>(R.id.btnTestStoreAPI).setOnClickListener {
            testAPI("/api/store/dashboard", "Store Dashboard (Privileged)")
        }

        findViewById<Button>(R.id.btnLogout).setOnClickListener {
            logout()
        }
    }

    private fun loadUserInfo() {
        testAPI("/api/me", "User Profile")
    }

    private fun testAPI(endpoint: String, apiName: String) {
        val authState = authStateManager.current
        val accessToken = authState.accessToken

        if (accessToken == null) {
            Toast.makeText(this, "No access token available", Toast.LENGTH_SHORT).show()
            return
        }

        Log.d(TAG, "Testing API: $endpoint")

        val client = OkHttpClient()
        val request = Request.Builder()
            .url("$AUTH_SERVER_URL$endpoint")
            .header("Authorization", "Bearer $accessToken")
            .get()
            .build()

        client.newCall(request).enqueue(object : Callback {
            override fun onFailure(call: Call, e: IOException) {
                runOnUiThread {
                    Log.e(TAG, "API call failed: $endpoint", e)
                    updateResultText("❌ $apiName Failed:\n${e.message}")
                }
            }

            override fun onResponse(call: Call, response: Response) {
                val responseBody = response.body?.string()
                runOnUiThread {
                    if (response.isSuccessful && responseBody != null) {
                        Log.d(TAG, "API call successful: $endpoint")
                        Log.d(TAG, "Response: $responseBody")

                        val json = JSONObject(responseBody)
                        val formattedResponse = json.toString(2)

                        // Check if this is a privileged endpoint access
                        if (endpoint.contains("store")) {
                            val scope = authState.scope ?: ""
                            val role = json.optString("role", "unknown")

                            updateResultText(
                                "🚨 권한 상승 성공! 🚨\n\n" +
                                        "API: $apiName\n" +
                                        "User Role: $role\n" +
                                        "Token Scope: $scope\n\n" +
                                        "민감 정보 접근 가능:\n$formattedResponse"
                            )
                        } else {
                            updateResultText("✅ $apiName:\n$formattedResponse")
                        }
                    } else {
                        Log.e(TAG, "API call failed: ${response.code} - $responseBody")
                        updateResultText("❌ $apiName Failed:\n${response.code} - $responseBody")
                    }
                }
            }
        })
    }

    private fun updateResultText(text: String) {
        findViewById<TextView>(R.id.tvApiResult).text = text
    }

    private fun logout() {
        // Clear auth state
        authStateManager.replace(net.openid.appauth.AuthState())
        navigateToLogin()
    }

    private fun navigateToLogin() {
        val intent = Intent(this, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        startActivity(intent)
        finish()
    }
}
