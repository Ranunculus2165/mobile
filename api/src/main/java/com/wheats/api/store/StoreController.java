package com.wheats.api.store;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.*;

@RestController
@RequestMapping("/api/stores")
@CrossOrigin(origins = "*") // 모바일 앱에서 접근 허용
public class StoreController {

    private final List<Store> stores = new ArrayList<>();
    private final Map<Long, List<MenuItem>> menusByStore = new HashMap<>();

    public StoreController() {
        // --- 더미 가게 데이터 ---
        stores.add(new Store(1L, "화이트해커 치킨 연세점", "OPEN", 15000, "30~40분"));
        stores.add(new Store(2L, "버그없는 피자", "OPEN", 18000, "25~35분"));
        stores.add(new Store(3L, "SQL 인젝션 떡볶이", "CLOSED", 12000, "준비중"));
        stores.add(new Store(4L, "XSS 버거", "OPEN", 10000, "20~30분"));
        stores.add(new Store(5L, "CSRF 돈까스", "OPEN", 13000, "35~45분"));

        // --- 더미 메뉴 데이터 ---
        menusByStore.put(1L, List.of(
                new MenuItem(1L, 1L, "화이트해커 후라이드", "기본에 충실한 후라이드 치킨", 18000),
                new MenuItem(2L, 1L, "화이트해커 양념치킨", "달콤 매콤 양념", 19000)
        ));

        menusByStore.put(2L, List.of(
                new MenuItem(3L, 2L, "버그없는 페퍼로니 피자", "토핑 듬뿍 페퍼로니", 21000),
                new MenuItem(4L, 2L, "디버깅 콤비네이션 피자", "다양한 토핑 조합", 23000)
        ));

        menusByStore.put(3L, List.of(
                new MenuItem(5L, 3L, "SQL 인젝션 떡볶이 보통맛", "기본 떡볶이", 8000)
        ));

        // 나머지는 공통 메뉴
        menusByStore.putIfAbsent(4L, List.of(
                new MenuItem(6L, 4L, "XSS 치즈버거 세트", "버거 + 감튀 + 콜라", 12000)
        ));
        menusByStore.putIfAbsent(5L, List.of(
                new MenuItem(7L, 5L, "CSRF 등심돈까스", "바삭한 돈까스 정식", 11000)
        ));
    }

    // 1. 가게 목록
    @GetMapping
    public List<Store> getStores() {
        return stores;
    }

    // 2. 가게 상세 + 메뉴
    @GetMapping("/{id}")
    public ResponseEntity<StoreDetailResponse> getStoreDetail(@PathVariable Long id) {
        return stores.stream()
                .filter(s -> s.getId().equals(id))
                .findFirst()
                .map(store -> {
                    List<MenuItem> menus = menusByStore.getOrDefault(id, List.of());
                    return ResponseEntity.ok(new StoreDetailResponse(store, menus));
                })
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}
