package com.example.mobile.ui.storelist

import android.content.Intent
import android.os.Bundle
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.Toast
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.Store
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.base.BaseActivity
import com.example.mobile.ui.storedetail.StoreDetailActivity
import kotlinx.coroutines.*

class StoreListActivity : BaseActivity() {

    // 가게 목록은 공개 화면: 인증 불필요
    override fun requiresAuth(): Boolean = false

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoreListAdapter
    private lateinit var btnReload: ImageButton
    private lateinit var pbReload: ProgressBar

    private var isLoading = false

    // 코루틴 스코프 (Activity 생명주기 따라가게)
    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // BaseActivity의 checkAuthAndRedirect()가 자동으로 호출됨
        // 토큰이 유효하지 않으면 자동으로 LoginActivity로 리다이렉트됨
        
        setContentView(R.layout.activity_store_list)

        recyclerView = findViewById(R.id.rvStoreList)
        btnReload = findViewById(R.id.btnReload)
        pbReload = findViewById(R.id.pbReload)

        adapter = StoreListAdapter { store ->
            openStoreDetail(store)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // 새로고침 버튼: API 지연/실패 시 사용자가 직접 재조회 가능
        btnReload.setOnClickListener {
            loadStoresFromApi(force = true)
        }

        // ★ API로 목록 불러오기
        loadStoresFromApi(force = false)
    }

    private fun setLoading(loading: Boolean) {
        isLoading = loading
        btnReload.isEnabled = !loading
        pbReload.visibility = if (loading) android.view.View.VISIBLE else android.view.View.GONE
        // 아이콘과 로딩이 겹치지 않게: 로딩 중에는 아이콘을 숨김
        btnReload.alpha = if (loading) 0.25f else 1.0f
    }

    private fun loadStoresFromApi(force: Boolean) {
        if (isLoading) return
        setLoading(true)

        uiScope.launch {
            try {
                // 네트워크는 IO 스레드에서
                val stores: List<Store> = withContext(Dispatchers.IO) {
                    ApiClient.storeApi.getStores()
                }

                adapter.submitList(stores)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@StoreListActivity,
                    "가게 목록을 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                setLoading(false)
            }
        }
    }

    private fun openStoreDetail(store: Store) {
        val intent = Intent(this, StoreDetailActivity::class.java).apply {
            putExtra(StoreDetailActivity.EXTRA_STORE_ID, store.id)
            putExtra(StoreDetailActivity.EXTRA_STORE_NAME, store.name)
            putExtra(StoreDetailActivity.EXTRA_STORE_STATUS, store.status)
            putExtra(StoreDetailActivity.EXTRA_STORE_MIN_ORDER, store.minOrderPrice)
            putExtra(StoreDetailActivity.EXTRA_STORE_TIME, "약 30~40분" )
            putExtra(StoreDetailActivity.EXTRA_STORE_DELIVERY_FEE, store.deliveryTip)
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
