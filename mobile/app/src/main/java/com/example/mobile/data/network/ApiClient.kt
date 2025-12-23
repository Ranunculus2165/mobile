package com.example.mobile.data.network

import android.content.Context
import com.example.mobile.WhEatsApplication
import com.example.mobile.data.auth.AuthStateManager
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiClient {

    private val loggingInterceptor = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        // 토큰이 로그로 노출되지 않도록 마스킹
        redactHeader("Authorization")
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

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .writeTimeout(30, TimeUnit.SECONDS)
        .addInterceptor(authInterceptor)
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
