package com.example.mobile.ui.storelist

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mobile.R
import com.example.mobile.data.model.Store
import com.example.mobile.data.network.ApiClient
import com.example.mobile.ui.storedetail.StoreDetailActivity
import kotlinx.coroutines.*

class StoreListActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: StoreListAdapter

    // 코루틴 스코프 (Activity 생명주기 따라가게)
    private val job = SupervisorJob()
    private val uiScope = CoroutineScope(Dispatchers.Main + job)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_store_list)

        recyclerView = findViewById(R.id.rvStoreList)
        adapter = StoreListAdapter { store ->
            openStoreDetail(store)
        }

        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        // ★ API로 목록 불러오기
        loadStoresFromApi()
    }

    private fun loadStoresFromApi() {
        uiScope.launch {
            try {
                // 네트워크는 IO 스레드에서
                val stores: List<Store> = withContext(Dispatchers.IO) {
                    ApiClient.storeApi.getStores()
                }

                if (stores.isEmpty()) {
                    Toast.makeText(
                        this@StoreListActivity,
                        "가게 목록이 비어 있습니다.",
                        Toast.LENGTH_SHORT
                    ).show()
                }

                adapter.submitList(stores)

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(
                    this@StoreListActivity,
                    "가게 목록을 불러오는데 실패했습니다.",
                    Toast.LENGTH_SHORT
                ).show()
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
        }
        startActivity(intent)
    }

    override fun onDestroy() {
        super.onDestroy()
        job.cancel()
    }
}
