package com.wheats.api.store;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

/**
 * /api/stores 관련 REST API 컨트롤러
 */
@RestController
@RequestMapping("/api/stores")
public class StoreController {

    // 간단하게 메모리 안에 가게/메뉴 데이터 보관
    private final Map<Long, Store> storeMap = new LinkedHashMap<>();

    public StoreController() {
        initDummyData();
    }

    // 초기 더미 데이터 - 나중에 DB 붙이면 여기 부분만 교체하면 됨
    private void initDummyData() {
        // 가게 1번 메뉴
        List<MenuItem> store1Menus = Arrays.asList(
                new MenuItem(1L, "치즈버거 세트", 7500, "두툼한 패티와 치즈가 들어간 버거 세트"),
                new MenuItem(2L, "감자튀김", 2500, "바삭바삭 감자튀김"),
                new MenuItem(3L, "콜라", 1500, "탄산 가득 콜라")
        );

        // 가게 2번 메뉴
        List<MenuItem> store2Menus = Arrays.asList(
                new MenuItem(1L, "마르게리따 피자", 13000, "기본에 충실한 클래식 피자"),
                new MenuItem(2L, "고르곤졸라 피자", 15000, "꿀과 함께 먹는 치즈 피자"),
                new MenuItem(3L, "제로 콜라", 2000, "칼로리 부담 없는 콜라")
        );

        // 가게 3번 메뉴
        List<MenuItem> store3Menus = Arrays.asList(
                new MenuItem(1L, "후라이드 치킨", 17000, "겉바속촉 기본 치킨"),
                new MenuItem(2L, "양념 치킨", 18000, "달콤한 양념 소스 치킨"),
                new MenuItem(3L, "치즈볼", 5000, "달콤 짭조름 치즈볼")
        );

        storeMap.clear();
        storeMap.put(1L, new Store(1L, "버거하우스", StoreStatus.OPEN, store1Menus));
        storeMap.put(2L, new Store(2L, "피자공방", StoreStatus.PREPARING, store2Menus));
        storeMap.put(3L, new Store(3L, "치킨타운", StoreStatus.CLOSED, store3Menus));

    }

    /**
     * 가게 목록 조회
     * GET /api/stores
     */
    @GetMapping
    public List<Store> getStores() {
        // Map → List 로 변환해서 반환
        return new ArrayList<>(storeMap.values());
    }

    /**
     * 가게 상세 조회
     * GET /api/stores/{storeId}
     * (가게 정보 + 메뉴까지 한 번에 내려줌)
     */
    @GetMapping("/{storeId}")
    public ResponseEntity<Store> getStoreDetail(@PathVariable Long storeId) {
        Store store = storeMap.get(storeId);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        return ResponseEntity.ok(store);
    }

    /**
     * 특정 가게의 메뉴만 별도로 조회
     * GET /api/stores/{storeId}/menus
     */
    @GetMapping("/{storeId}/menus")
    public ResponseEntity<List<MenuItem>> getStoreMenus(@PathVariable Long storeId) {
        Store store = storeMap.get(storeId);
        if (store == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        }
        List<MenuItem> menus = store.getMenus();
        if (menus == null) {
            menus = Collections.emptyList();
        }
        return ResponseEntity.ok(menus);
    }

    // ======================================================
    // 🔥 점주 대시보드 API
    //    GET /api/stores/owners/{ownerId}/dashboard
    //
    //  - OwnerDashboardActivity 에서 ownerId만 넘겨서 호출한다고 가정
    //  - 특정 ownerId(예: 4242)일 때만 flag / deeplinkFragment 내려줌
    // ======================================================
    @GetMapping("/owners/{ownerId}/dashboard")
    public ResponseEntity<OwnerDashboardResponse> getOwnerDashboard(@PathVariable Long ownerId) {

        // 👉 여기서는 간단하게:
        //  - ownerId 아무거나 들어와도 공통 더미 데이터 내려주고
        //  - ownerId == 4242 일 때만 flag / fragment 추가

        // 가게 요약 리스트 (실제라면 ownerId 기준으로 필터링하겠지만, 지금은 전체 사용)
        List<StoreSummary> storeSummaries = new ArrayList<>();
        for (Store s : storeMap.values()) {
            storeSummaries.add(new StoreSummary(
                    s.getId(),
                    s.getName(),
                    s.getStatus()
            ));
        }

        OwnerDashboardResponse resp = new OwnerDashboardResponse();
        resp.setOwnerId(ownerId);
        resp.setStores(storeSummaries);

        // 더미 매출/주문 건수
        resp.setTodaySalesTotal(350000);   // 오늘 전체 매출 합산 (더미)
        resp.setTodayOrderCount(42);       // 오늘 주문 건수 (더미)

        // ⭐ 특정 ownerId일 때만 CTF용 값 추가
        if (ownerId.equals(4242L)) {
            resp.setDeeplinkFragment("th/wheat");
        }

        return ResponseEntity.ok(resp);
    }

    // ============================================
    // 🔽 점주 대시보드용 응답 DTO
    // ============================================

    public static class OwnerDashboardResponse {
        private Long ownerId;
        private List<StoreSummary> stores;
        private int todaySalesTotal;
        private int todayOrderCount;

        // 🔥 취약점/CTF용 필드
        private String flag;              // 플래그
        private String deeplinkFragment;  // 예: "#admin"

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
