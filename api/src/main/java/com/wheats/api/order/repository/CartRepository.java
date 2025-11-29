package com.wheats.api.order.repository;

import com.wheats.api.order.entity.CartEntity;
import com.wheats.api.order.entity.CartStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CartRepository extends JpaRepository<CartEntity, Long> {

    // user의 최근 ACTIVE 장바구니 1개
    Optional<CartEntity> findFirstByUserIdAndStatusOrderByCreatedAtDesc(Long userId, CartStatus status);

    // user + store 기준 ACTIVE 장바구니
    Optional<CartEntity> findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(
            Long userId,
            Long storeId,
            CartStatus status
    );
}
