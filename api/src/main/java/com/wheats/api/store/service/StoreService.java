package com.wheats.api.store.service;

import com.wheats.api.store.dto.MenuItem;
import com.wheats.api.store.dto.Store;
import com.wheats.api.store.dto.StoreDetailResponse;
import com.wheats.api.store.dto.StoreStatus;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.entity.MenuEntity;
import com.wheats.api.store.repository.StoreRepository;
import com.wheats.api.store.repository.MenuRepository;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class StoreService {

    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public StoreService(StoreRepository storeRepository,
                        MenuRepository menuRepository) {
        this.storeRepository = storeRepository;
        this.menuRepository = menuRepository;
    }

    /** 전체 목록 조회 */
    public List<Store> getAllStores() {
        List<Store> result = new ArrayList<>();
        for (StoreEntity entity : storeRepository.findAll()) {
            result.add(toStoreDto(entity));
        }
        return result;
    }

    /** 상세 조회: Store + 메뉴 리스트 (실제 DB에서 메뉴 조회) */
    public StoreDetailResponse getStoreDetail(Long id) {
        StoreEntity entity = storeRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Store Not Found: " + id));

        Store storeDto = toStoreDto(entity);

        // 🔥 여기서 실제 DB에서 메뉴 가져오기
        // MenuRepository에 아래 메서드가 있다고 가정:
        // List<MenuEntity> findByStoreIdAndIsAvailableTrue(Long storeId);
        List<MenuEntity> menuEntities = menuRepository.findByStoreIdAndIsAvailableTrue(id);

        List<MenuItem> menus = new ArrayList<>();
        for (MenuEntity m : menuEntities) {
            menus.add(toMenuItemDto(m));
        }

        return new StoreDetailResponse(storeDto, menus);
    }

    /** Store Entity → DTO 변환 */
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

    /** Menu Entity → MenuItem DTO 변환 */
    private MenuItem toMenuItemDto(MenuEntity e) {
        MenuItem dto = new MenuItem();
        dto.setId(e.getId());
        dto.setName(e.getName());
        dto.setPrice(e.getPrice());
        dto.setDescription(e.getDescription());
        // 필드명은 프로젝트 실제 필드에 맞게 조정
        dto.setAvailable(e.getIsAvailable() != null && e.getIsAvailable());
        dto.setImageUrl(e.getImageUrl());
        return dto;
    }
}
