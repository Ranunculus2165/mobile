package com.example.mobile

import android.app.Application
import com.example.mobile.data.auth.TokenManager

class WhEatsApplication : Application() {

    override fun onCreate() {
        super.onCreate()
        // TokenManager 초기화
        TokenManager.init(this)
    }
}
