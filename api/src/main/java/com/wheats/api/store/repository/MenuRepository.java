package com.wheats.api.store.repository;

import com.wheats.api.store.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {

    // store_id로 메뉴 조회 (판매 중인 것만)
    List<MenuEntity> findByStoreIdAndIsAvailableTrue(Long storeId);
}
