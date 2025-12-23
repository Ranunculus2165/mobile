package com.example.mobile.data.network

import android.content.Intent
import android.os.Handler
import android.os.Looper
import com.example.mobile.WhEatsApplication
import com.example.mobile.data.auth.AuthStateManager
import com.example.mobile.ui.auth.LoginActivity
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicBoolean

object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        // 토큰이 로그로 노출되지 않도록 마스킹
        redactHeader("Authorization")
    }

    private val isRedirectingToLogin = AtomicBoolean(false)

    private fun redirectToLoginOnce() {
        if (!isRedirectingToLogin.compareAndSet(false, true)) return

        val context = WhEatsApplication.instance
        // 토큰/상태 정리
        AuthStateManager.getInstance(context).clear()

        // UI 스레드에서 로그인으로 이동
        Handler(Looper.getMainLooper()).post {
            val intent = Intent(context, LoginActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            }
            context.startActivity(intent)
        }

        // 연속 호출 방지 (짧은 쿨다운)
        Handler(Looper.getMainLooper()).postDelayed(
            { isRedirectingToLogin.set(false) },
            1200
        )
    }

    // OAuth Access Token을 Authorization 헤더에 자동으로 추가하는 인터셉터
    private val authInterceptor = Interceptor { chain ->
        val context = WhEatsApplication.instance
        val authStateManager = AuthStateManager.getInstance(context)
        val authState = authStateManager.current
        
        val accessToken = authState.accessToken
        val expirationTime = authState.accessTokenExpirationTime
        
        // 토큰이 존재하고 만료되지 않았는지 확인 (60초 여유)
        // accessTokenExpirationTime은 밀리초 단위이므로 밀리초로 비교
        val currentTimeMs = System.currentTimeMillis()
        val timeUntilExpirySeconds = if (expirationTime != null) {
            (expirationTime - currentTimeMs) / 1000
        } else {
            0
        }
        val isValidToken = accessToken != null && expirationTime != null && timeUntilExpirySeconds > 60

        // 디버그 로그(토큰 전체는 절대 출력하지 않음)
        val tokenPreview = if (!accessToken.isNullOrBlank()) {
            if (accessToken.length > 15) "${accessToken.substring(0, 10)}...${accessToken.substring(accessToken.length - 5)}"
            else accessToken.take(15)
        } else {
            "null"
        }
        
        val request = if (isValidToken) {
            android.util.Log.d(
                "ApiClient",
                "✅ Adding Authorization header (expires in ~${timeUntilExpirySeconds}s, token=$tokenPreview)"
            )
            chain.request().newBuilder()
                .addHeader("Authorization", "Bearer $accessToken")
                .build()
        } else {
            // 만료된 토큰이면 헤더에 추가하지 않음 (삭제는 BaseActivity에서 처리)
            android.util.Log.w(
                "ApiClient",
                "⛔ Not adding Authorization header (accessToken=${accessToken != null}, expirationTime=${expirationTime != null}, remaining=${timeUntilExpirySeconds}s, token=$tokenPreview)"
            )
            chain.request()
        }
        chain.proceed(request)
    }

    // 전역 401 처리: 개별 Activity에서 매번 처리하지 않아도 로그인 화면으로 자연스럽게 전환
    private val unauthorizedInterceptor = Interceptor { chain ->
        val response = chain.proceed(chain.request())
        if (response.code == 401) {
            android.util.Log.w("ApiClient", "🔐 HTTP 401 detected. Redirecting to LoginActivity.")
            redirectToLoginOnce()
        }
        response
    }

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
        .addInterceptor(unauthorizedInterceptor)
        // authInterceptor가 헤더를 붙인 뒤에 로깅하도록 순서 조정
        .addInterceptor(loggingInterceptor)
        .build()

    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/")   // 에뮬레이터에서 localhost
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val storeApi: StoreApi = retrofit.create(StoreApi::class.java)
    val myPageApi: MyPageApi = retrofit.create(MyPageApi::class.java)
    val cartApi: CartApi = retrofit.create(CartApi::class.java)
    val orderApi: OrderApi = retrofit.create(OrderApi::class.java)
}
