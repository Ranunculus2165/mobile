package com.wheats.api.mypage.service;

import com.wheats.api.mypage.dto.MyPageProfileResponse;
import com.wheats.api.mypage.dto.MyPageResponse;
import com.wheats.api.mypage.dto.OrderHistoryItemResponse;
import com.wheats.api.mypage.entity.UserEntity;
import com.wheats.api.mypage.repository.UserRepository;
import com.wheats.api.order.entity.OrderEntity;
import com.wheats.api.order.entity.OrderItemEntity;
import com.wheats.api.order.entity.OrderStatus;
import com.wheats.api.order.repository.OrderItemRepository;
import com.wheats.api.order.repository.OrderRepository;
import com.wheats.api.store.entity.MenuEntity;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.MenuRepository;
import com.wheats.api.store.repository.StoreRepository;
import jakarta.persistence.EntityNotFoundException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class MyPageService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public MyPageService(UserRepository userRepository,
                         OrderRepository orderRepository,
                         OrderItemRepository orderItemRepository,
                         StoreRepository storeRepository,
                         MenuRepository menuRepository) {
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.storeRepository = storeRepository;
        this.menuRepository = menuRepository;
    }

    /**
     * 현재 로그인 유저(임시로 userId 직접 전달)의 마이페이지 프로필 조회
     */
    public MyPageProfileResponse getMyProfile(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found. id=" + userId));

        return MyPageProfileResponse.from(user);
    }

    /**
     * 마이페이지 전체 정보 조회 (프로필 + 주문 내역)
     */
    @Transactional(readOnly = true)
    public MyPageResponse getMyPage(Long userId) {
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new EntityNotFoundException("User not found. id=" + userId));

        // 최근 주문 내역 조회 (최대 3개)
        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<OrderHistoryItemResponse> orderHistory = new ArrayList<>();

        int count = 0;
        for (OrderEntity order : orders) {
            if (count >= 3) break;

            // 가게 정보 조회
            StoreEntity store = storeRepository.findById(order.getStoreId())
                    .orElse(null);
            String storeName = (store != null) ? store.getName() : "(삭제된 가게)";

            // 주문 아이템 조회하여 상품 설명 생성
            List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(order.getId());
            String itemDescription = buildItemDescription(orderItems);

            // 주문 상태를 한글로 변환
            String status = convertOrderStatusToKorean(order.getStatus());

            orderHistory.add(new OrderHistoryItemResponse(
                    order.getId(),
                    storeName,
                    itemDescription,
                    order.getCreatedAt(),
                    order.getTotalPrice(),
                    status
            ));
            count++;
        }

        return new MyPageResponse(
                user.getName(),
                user.getEmail(),
                user.getPoint(),
                orderHistory
        );
    }

    /**
     * 주문 아이템 목록으로부터 상품 설명 문자열 생성
     * 예: "싸이버거 세트 외 1개"
     */
    private String buildItemDescription(List<OrderItemEntity> orderItems) {
        if (orderItems.isEmpty()) {
            return "주문 내역 없음";
        }

        // 첫 번째 메뉴 조회
        OrderItemEntity firstItem = orderItems.get(0);
        MenuEntity firstMenu = menuRepository.findById(firstItem.getMenuId())
                .orElse(null);
        String firstMenuName = (firstMenu != null) ? firstMenu.getName() : "(삭제된 메뉴)";

        if (orderItems.size() == 1) {
            return firstMenuName;
        } else {
            int otherCount = orderItems.size() - 1;
            return firstMenuName + " 외 " + otherCount + "개";
        }
    }

    /**
     * 전체 주문 내역 조회 (제한 없음)
     */
    @Transactional(readOnly = true)
    public List<OrderHistoryItemResponse> getAllOrderHistory(Long userId) {
        List<OrderEntity> orders = orderRepository.findByUserIdOrderByCreatedAtDesc(userId);
        List<OrderHistoryItemResponse> orderHistory = new ArrayList<>();

        for (OrderEntity order : orders) {
            // 가게 정보 조회
            StoreEntity store = storeRepository.findById(order.getStoreId())
                    .orElse(null);
            String storeName = (store != null) ? store.getName() : "(삭제된 가게)";

            // 주문 아이템 조회하여 상품 설명 생성
            List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(order.getId());
            String itemDescription = buildItemDescription(orderItems);

            // 주문 상태를 한글로 변환
            String status = convertOrderStatusToKorean(order.getStatus());

            orderHistory.add(new OrderHistoryItemResponse(
                    order.getId(),
                    storeName,
                    itemDescription,
                    order.getCreatedAt(),
                    order.getTotalPrice(),
                    status
            ));
        }

        return orderHistory;
    }

    /**
     * 주문 상태를 한글로 변환
     */
    private String convertOrderStatusToKorean(OrderStatus status) {
        return switch (status) {
            case PAID -> "배달완료";
            case PENDING -> "주문대기";
            case CANCELLED -> "주문취소";
        };
    }
}
