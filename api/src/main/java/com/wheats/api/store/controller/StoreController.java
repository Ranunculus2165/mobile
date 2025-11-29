package com.wheats.api.store.controller;

import com.wheats.api.store.dto.Store;
import com.wheats.api.store.dto.StoreDetailResponse;
import com.wheats.api.store.dto.StoreStatus;
import com.wheats.api.store.service.StoreService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/stores")
public class StoreController {

    private final StoreService storeService;

    public StoreController(StoreService storeService) {
        this.storeService = storeService;
    }

    /** 목록 */
    @GetMapping
    public List<Store> getStores() {
        return storeService.getAllStores();
    }

    /** 상세 (가게 + 메뉴들) */
    @GetMapping("/{storeId}")
    public ResponseEntity<StoreDetailResponse> getStoreDetail(@PathVariable Long storeId) {
        return storeService.getStoreDetail(storeId)
                .map(ResponseEntity::ok)
                .orElseGet(() -> ResponseEntity.status(HttpStatus.NOT_FOUND).build());
    }

    // 아래 점주 대시보드 API는 그대로 두면 됨 (필요하면 나중에 메뉴수까지 포함해서 커스터마이징 가능)
    // ...
}
