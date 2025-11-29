package com.wheats.api.store.service;

import com.wheats.api.store.dto.Store;
import com.wheats.api.store.dto.StoreStatus;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    private final StoreRepository storeRepository;

    public StoreService(StoreRepository storeRepository) {
        this.storeRepository = storeRepository;
    }

    /** 전체 목록 */
    public List<Store> getAllStores() {
        List<Store> result = new ArrayList<>();
        for (StoreEntity entity : storeRepository.findAll()) {
            result.add(toStoreDto(entity));
        }
        return result;
    }

    /** 단일 가게 조회 */
    public Optional<Store> getStore(Long id) {
        return storeRepository.findById(id)
                .map(this::toStoreDto);
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
        dto.setStatus(
                e.getIsOpen() != null && e.getIsOpen()
                        ? StoreStatus.OPEN
                        : StoreStatus.CLOSED
        );
        return dto;
    }
}
