package com.wheats.api.order.repository;

import com.wheats.api.order.entity.OrderEntity;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface OrderRepository extends JpaRepository<OrderEntity, Long> {

    // 내 주문 상세용 (본인 주문만)
    Optional<OrderEntity> findByIdAndUserId(Long id, Long userId);

    // 내 주문 목록 조회용 (필요 시)
    List<OrderEntity> findByUserIdOrderByCreatedAtDesc(Long userId);
}
