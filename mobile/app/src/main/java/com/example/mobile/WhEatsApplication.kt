package com.example.mobile

import android.app.Application
import com.example.mobile.data.auth.TokenManager

class WhEatsApplication : Application() {

    companion object {
        @Volatile
        private var INSTANCE: WhEatsApplication? = null

        val instance: WhEatsApplication
            get() = INSTANCE ?: throw IllegalStateException("Application이 초기화되지 않았습니다.")
    }

    override fun onCreate() {
        super.onCreate()
        INSTANCE = this
        
        // TokenManager 초기화 (하위 호환성을 위해 유지)
        TokenManager.init(this)
    }
}
