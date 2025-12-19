package com.wheats.api.order.service;

import com.wheats.api.mypage.entity.UserEntity;
import com.wheats.api.mypage.repository.UserRepository;
import com.wheats.api.order.dto.OrderDetailResponse;
import com.wheats.api.order.dto.OrderItemResponse;
import com.wheats.api.order.dto.OrderRequest;
import com.wheats.api.order.dto.OrderResponse;
import com.wheats.api.order.entity.CartEntity;
import com.wheats.api.order.entity.CartItemEntity;
import com.wheats.api.order.entity.CartStatus;
import com.wheats.api.order.entity.OrderEntity;
import com.wheats.api.order.entity.OrderItemEntity;
import com.wheats.api.order.entity.OrderStatus;
import com.wheats.api.order.repository.CartItemRepository;
import com.wheats.api.order.repository.CartRepository;
import com.wheats.api.order.repository.OrderItemRepository;
import com.wheats.api.order.repository.OrderRepository;
import com.wheats.api.store.entity.MenuEntity;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.MenuRepository;
import com.wheats.api.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final MenuRepository menuRepository;
    private final StoreRepository storeRepository;
    private final UserRepository userRepository;

    public OrderService(CartRepository cartRepository,
                        CartItemRepository cartItemRepository,
                        OrderRepository orderRepository,
                        OrderItemRepository orderItemRepository,
                        MenuRepository menuRepository,
                        StoreRepository storeRepository,
                        UserRepository userRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.orderItemRepository = orderItemRepository;
        this.menuRepository = menuRepository;
        this.storeRepository = storeRepository;
        this.userRepository = userRepository;
    }

    /**
     * 주문 생성 (+ 결제 완료 상태로 바로 전환)
     * - 지금은 userId = 1L 하드코딩 (마이페이지와 동일)
     * - 모바일에서 cartId만 보내준다고 가정
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        Long userId = 1L; // TODO: OAuth 붙이면 토큰에서 꺼내기
        Long cartId = request.getCartId();

        // 1) 카트 조회 + 소유자/상태 검증
        CartEntity cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new IllegalArgumentException("장바구니를 찾을 수 없습니다. id=" + cartId));

        if (!cart.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 장바구니가 아닙니다.");
        }
        if (cart.getStatus() != CartStatus.ACTIVE) {
            throw new IllegalStateException("이미 주문 처리된 장바구니입니다.");
        }

        // 2) 카트 아이템 조회
        List<CartItemEntity> cartItems = cartItemRepository.findByCartId(cartId);
        if (cartItems.isEmpty()) {
            throw new IllegalStateException("장바구니에 담긴 상품이 없습니다.");
        }

        // 3) 총 금액 계산 (각 메뉴 가격 * 수량)
        int orderAmount = 0;
        for (CartItemEntity item : cartItems) {
            Long menuId = item.getMenuId();
            MenuEntity menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. id=" + menuId));

            orderAmount += menu.getPrice() * item.getQuantity();
        }

        // 4) 배달료 조회
        StoreEntity store = storeRepository.findById(cart.getStoreId())
                .orElseThrow(() -> new IllegalArgumentException("가게를 찾을 수 없습니다. id=" + cart.getStoreId()));
        int deliveryFee = (store.getDeliveryTip() != null) ? store.getDeliveryTip() : 0;
        int totalPrice = orderAmount + deliveryFee;

        // 5) 사용자 포인트 확인 및 차감
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("사용자를 찾을 수 없습니다. id=" + userId));

        if (user.getPoint() < totalPrice) {
            throw new IllegalStateException(
                    String.format("포인트가 부족합니다. 잔여 포인트: %,d원, 필요 포인트: %,d원", 
                            user.getPoint(), totalPrice)
            );
        }

        // 포인트 차감
        user.setPoint(user.getPoint() - totalPrice);
        userRepository.save(user);

        // 6) 주문번호 생성 (간단 버전)
        String orderNumber = generateOrderNumber();

        // 7) 주문 엔티티 생성 (이미 정의된 생성자 시그니처에 맞춤)
        //    OrderEntity(Long userId, Long storeId, Long cartId,
        //                String orderNumber, OrderStatus status, int totalPrice)
        OrderEntity order = new OrderEntity(
                userId,
                cart.getStoreId(),
                cart.getId(),
                orderNumber,
                OrderStatus.PAID,   // 결제까지 완료된 상태
                totalPrice
        );
        order = orderRepository.save(order);
        
        // 결제 완료 시간 설정
        order.setPaidAt(LocalDateTime.now());
        order = orderRepository.save(order);

        // 8) 주문 아이템 엔티티 생성
        List<OrderItemEntity> orderItems = new ArrayList<>();
        for (CartItemEntity item : cartItems) {
            Long menuId = item.getMenuId();
            MenuEntity menu = menuRepository.findById(menuId)
                    .orElseThrow(() -> new IllegalArgumentException("메뉴를 찾을 수 없습니다. id=" + menuId));

            OrderItemEntity orderItem = new OrderItemEntity(
                    order.getId(),       // 🔥 여기: Long orderId 전달
                    menuId,
                    item.getQuantity(),
                    menu.getPrice()
            );
            orderItems.add(orderItem);
        }
        orderItemRepository.saveAll(orderItems);

        // 9) 장바구니 아이템들 삭제
        cartItemRepository.deleteAll(cartItems);

        // 10) 장바구니 삭제 (ID로 직접 삭제하여 상태 변경 방지)
        cartRepository.deleteById(cart.getId());

        // 11) 응답 DTO로 변환
        return new OrderResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getTotalPrice(),
                order.getStatus().name(),
                order.getCreatedAt(),
                order.getPaidAt()
        );
    }

    /**
     * 주문 상세 / 영수증 조회
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long orderId) {
        Long userId = 1L; // TODO: 인증 붙으면 토큰에서 꺼내기

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. id=" + orderId));

        // 본인 주문인지 확인 (나중에 어드민/점주는 별도 권한 체크)
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 주문이 아닙니다.");
        }

        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemResponse> itemResponses = new ArrayList<>();

        for (OrderItemEntity item : orderItems) {
            Long menuId = item.getMenuId();
            MenuEntity menu = menuRepository.findById(menuId)
                    .orElse(null); // 메뉴가 삭제되었을 수도 있으니, 없으면 이름은 null 처리

            String menuName = (menu != null) ? menu.getName() : "(삭제된 메뉴)";
            itemResponses.add(new OrderItemResponse(
                    menuId,
                    menuName,
                    item.getQuantity(),
                    item.getUnitPrice()
            ));
        }

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getPaidAt(),
                itemResponses
        );
    }

    /**
     * 아주 단순한 주문번호 생성 로직
     * - 실제 서비스라면 별도 시퀀스/규칙 사용
     */
    private String generateOrderNumber() {
        return "ORD-" + System.currentTimeMillis();
    }
}
