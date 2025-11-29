package com.wheats.api.store.controller;

import com.wheats.api.store.dto.Store;
import com.wheats.api.store.dto.StoreStatus;
import com.wheats.api.store.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

/**
 * /api/stores 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /** 가게 목록 조회 */
    @GetMapping
    public List<Store> getStores() {
        return storeService.getAllStores();
    }

    /** 가게 상세 조회 */
    @GetMapping("/{storeId}")
    public ResponseEntity<Store> getStoreDetail(@PathVariable Long storeId) {
        return storeService.getStore(storeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // ======================================================
    // 점주 대시보드 API
    // GET /api/stores/owners/{ownerId}/dashboard
    // ======================================================
    @GetMapping("/owners/{ownerId}/dashboard")
    public ResponseEntity<OwnerDashboardResponse> getOwnerDashboard(@PathVariable Long ownerId) {

        List<Store> stores = storeService.getAllStores();

        List<StoreSummary> storeSummaries = stores.stream()
                .map(s -> new StoreSummary(
                        s.getId(),
                        s.getName(),
                        s.getStatus()
                ))
                .collect(Collectors.toList());

        OwnerDashboardResponse resp = new OwnerDashboardResponse();
        resp.setOwnerId(ownerId);
        resp.setStores(storeSummaries);

        resp.setTodaySalesTotal(350000);   // 더미
        resp.setTodayOrderCount(42);       // 더미

        if (ownerId.equals(4242L)) {
            resp.setDeeplinkFragment("th/wheat");
        }

        return ResponseEntity.ok(resp);
    }

    // 내부 DTO들
    public static class OwnerDashboardResponse {
        private Long ownerId;
        private List<StoreSummary> stores;
        private int todaySalesTotal;
        private int todayOrderCount;

        private String flag;
        private String deeplinkFragment;

        public Long getOwnerId() { return ownerId; }
        public void setOwnerId(Long ownerId) { this.ownerId = ownerId; }

        public List<StoreSummary> getStores() { return stores; }
        public void setStores(List<StoreSummary> stores) { this.stores = stores; }

        public int getTodaySalesTotal() { return todaySalesTotal; }
        public void setTodaySalesTotal(int todaySalesTotal) { this.todaySalesTotal = todaySalesTotal; }

        public int getTodayOrderCount() { return todayOrderCount; }
        public void setTodayOrderCount(int todayOrderCount) { this.todayOrderCount = todayOrderCount; }

        public String getFlag() { return flag; }
        public void setFlag(String flag) { this.flag = flag; }

        public String getDeeplinkFragment() { return deeplinkFragment; }
        public void setDeeplinkFragment(String deeplinkFragment) { this.deeplinkFragment = deeplinkFragment; }
    }

    public static class StoreSummary {
        private Long id;
        private String name;
        private StoreStatus status;

        public StoreSummary(Long id, String name, StoreStatus status) {
            this.id = id;
            this.name = name;
            this.status = status;
        }

        public Long getId() { return id; }
        public String getName() { return name; }
        public StoreStatus getStatus() { return status; }
    }
}
