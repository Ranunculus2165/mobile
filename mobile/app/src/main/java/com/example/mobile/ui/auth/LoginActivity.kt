package com.example.mobile.ui.auth

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.data.auth.TokenManager
import com.example.mobile.data.model.LoginRequest
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.storelist.StoreListActivity
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.*

class LoginActivity : AppCompatActivity() {

    private lateinit var etEmail: TextInputEditText
    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)

        etEmail = findViewById(R.id.etEmail)
        val btnLogin = findViewById<android.widget.Button>(R.id.btnLogin)
        val tvError = findViewById<android.widget.TextView>(R.id.tvError)
        val progressBar = findViewById<android.widget.ProgressBar>(R.id.progressBar)

        btnLogin.setOnClickListener {
            val email = etEmail.text?.toString()?.trim()

            if (email.isNullOrEmpty()) {
                tvError.text = "이메일을 입력해주세요."
                tvError.visibility = android.view.View.VISIBLE
                return@setOnClickListener
            }

            // 로그인 진행
            progressBar.visibility = android.view.View.VISIBLE
            tvError.visibility = android.view.View.GONE
            btnLogin.isEnabled = false

            uiScope.launch {
                try {
                    val request = LoginRequest(email)
                    val response = withContext(Dispatchers.IO) {
                        ApiClient.authApi.login(request)
                    }

                    // 토큰 저장
                    TokenManager.saveToken(
                        token = response.token,
                        userId = response.userId,
                        name = response.name,
                        email = response.email,
                        role = response.role
                    )

                    // 메인 화면으로 이동
                    val intent = Intent(this@LoginActivity, StoreListActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                    finish()

                } catch (e: retrofit2.HttpException) {
                    // HTTP 에러 처리
                    e.printStackTrace()
                    val errorBody = e.response()?.errorBody()?.string()
                    
                    when (e.code()) {
                        400 -> {
                            // 사용자를 찾을 수 없음 (이메일/비밀번호 확인 요청)
                            tvError.text = "이메일 또는 비밀번호를 확인해주세요."
                        }
                        404 -> {
                            // URL을 찾을 수 없음 (서버 엔드포인트 문제)
                            tvError.text = "서버 연결에 실패했습니다. 서버가 실행 중인지 확인해주세요."
                        }
                        500 -> {
                            // 서버 내부 오류
                            tvError.text = "서버 오류가 발생했습니다. 잠시 후 다시 시도해주세요."
                        }
                        else -> {
                            // 기타 HTTP 에러
                            tvError.text = "로그인에 실패했습니다. (오류 코드: ${e.code()})"
                        }
                    }
                    tvError.visibility = android.view.View.VISIBLE
                } catch (e: java.net.UnknownHostException) {
                    // 네트워크 연결 실패
                    e.printStackTrace()
                    tvError.text = "서버에 연결할 수 없습니다. 네트워크 연결을 확인해주세요."
                    tvError.visibility = android.view.View.VISIBLE
                } catch (e: java.net.SocketTimeoutException) {
                    // 타임아웃
                    e.printStackTrace()
                    tvError.text = "서버 응답 시간이 초과되었습니다. 다시 시도해주세요."
                    tvError.visibility = android.view.View.VISIBLE
                } catch (e: Exception) {
                    // 기타 예외
                    e.printStackTrace()
                    tvError.text = "로그인에 실패했습니다: ${e.message ?: "알 수 없는 오류"}"
                    tvError.visibility = android.view.View.VISIBLE
                } finally {
                    progressBar.visibility = android.view.View.GONE
                    btnLogin.isEnabled = true
                }
            }
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
