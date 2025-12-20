package com.wheats.api.order.repository;

import com.wheats.api.order.entity.CartItemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface CartItemRepository extends JpaRepository<CartItemEntity, Long> {

    List<CartItemEntity> findByCartId(Long cartId);

    Optional<CartItemEntity> findByCartIdAndMenuId(Long cartId, Long menuId);

    // 벌크 삭제 (더 효율적)
    @Modifying
    @Query("DELETE FROM CartItemEntity c WHERE c.cartId = :cartId")
    void deleteAllByCartId(@Param("cartId") Long cartId);
}
