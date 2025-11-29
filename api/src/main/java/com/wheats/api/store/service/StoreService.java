package com.wheats.api.store.service;

import com.wheats.api.store.dto.MenuItem;
import com.wheats.api.store.dto.Store;
import com.wheats.api.store.dto.StoreDetailResponse;
import com.wheats.api.store.dto.StoreStatus;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /** 전체 목록 조회 */
    public List<Store> getAllStores() {
        List<Store> result = new ArrayList<>();
        for (StoreEntity entity : storeRepository.findAll()) {
            result.add(toStoreDto(entity));
        }
        return result;
    }

    /** 상세 조회: Store + 메뉴 리스트 (메뉴는 일단 빈 리스트) */
    public StoreDetailResponse getStoreDetail(Long id) {
        StoreEntity entity = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store Not Found: " + id));

        Store storeDto = toStoreDto(entity);

        // 메뉴는 일단 DB 안 쓰고 빈 리스트로 (필요하면 menuRepository 붙이면 됨)
        List<MenuItem> menus = new ArrayList<>();

        return new StoreDetailResponse(storeDto, menus);
    }

    /** Entity → DTO 변환 */
    private Store toStoreDto(StoreEntity e) {
        Store dto = new Store();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setCategory(e.getCategory());
        dto.setDescription(e.getDescription());
        dto.setMinOrderPrice(e.getMinOrderPrice());
        dto.setDeliveryTip(e.getDeliveryTip());
        dto.setRating(e.getRating());
        dto.setReviewCount(e.getReviewCount());
        dto.setImageUrl(e.getImageUrl());

        dto.setStatus(e.getIsOpen() != null && e.getIsOpen()
                ? StoreStatus.OPEN
                : StoreStatus.CLOSED);

        return dto;
    }
}
