package com.wheats.api.store.service;

import com.wheats.api.store.dto.*;
import com.wheats.api.store.entity.MenuEntity;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.MenuRepository;
import com.wheats.api.store.repository.StoreRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public StoreService(StoreRepository storeRepository, MenuRepository menuRepository) {
        this.storeRepository = storeRepository;
        this.menuRepository = menuRepository;
    }

    /** 전체 목록 (리스트 화면) */
    public List<Store> getAllStores() {
        List<Store> result = new ArrayList<>();
        for (StoreEntity entity : storeRepository.findAll()) {
            result.add(toStoreDto(entity));
        }
        return result;
    }

    /** 단일 가게 + 메뉴 목록 (상세 화면) */
    public Optional<StoreDetailResponse> getStoreDetail(Long id) {
        return storeRepository.findById(id)
                .map(entity -> {
                    Store storeDto = toStoreDto(entity);
                    List<MenuItem> menuDtos = toMenuDtoList(menuRepository.findByStore_Id(id));
                    return new StoreDetailResponse(storeDto, menuDtos);
                });
    }

    /** Entity → Store DTO */
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

    /** 메뉴 Entity 리스트 → DTO 리스트 */
    private List<MenuItem> toMenuDtoList(List<MenuEntity> entities) {
        List<MenuItem> list = new ArrayList<>();
        for (MenuEntity e : entities) {
            MenuItem m = new MenuItem();
            m.setId(e.getId());
            m.setName(e.getName());
            m.setPrice(e.getPrice());
            m.setDescription(e.getDescription());
            m.setAvailable(e.getIsAvailable());
            m.setImageUrl(e.getImageUrl());
            list.add(m);
        }
        return list;
    }
}
