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
}
