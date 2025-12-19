package com.example.mobile.ui.base

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.example.mobile.R
import com.example.mobile.ui.mypage.MyPageActivity
import com.example.mobile.ui.storelist.StoreListActivity
import com.google.android.material.bottomnavigation.BottomNavigationView

abstract class BaseActivity : AppCompatActivity() {

    protected var bottomNavigation: BottomNavigationView? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
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
