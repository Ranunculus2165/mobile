package com.wheats.api.order.repository;

import com.wheats.api.order.entity.CartItemEntity;
import com.wheats.api.order.entity.CartItemStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByCartIdAndStatus(Long cartId, CartItemStatus status);

    Optional<CartItemEntity> findByCartIdAndMenuIdAndStatus(Long cartId, Long menuId, CartItemStatus status);

    // 벌크 "소프트 삭제"(상태 업데이트)
    @Modifying
    @Query("UPDATE CartItemEntity c SET c.status = :status WHERE c.cartId = :cartId")
    int updateStatusByCartId(@Param("cartId") Long cartId, @Param("status") CartItemStatus status);
}
