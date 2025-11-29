package com.wheats.api.store.repository;

import com.wheats.api.store.entity.MenuEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface MenuRepository extends JpaRepository<MenuEntity, Long> {

    // store.id 기준으로 메뉴 찾기
    List<MenuEntity> findByStore_Id(Long storeId);
}
