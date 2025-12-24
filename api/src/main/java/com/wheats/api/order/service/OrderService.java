package com.wheats.api.order.service;

import com.wheats.api.mypage.entity.UserEntity;
import com.wheats.api.mypage.repository.UserRepository;
import com.wheats.api.order.dto.OrderDetailResponse;
import com.wheats.api.order.dto.OrderItemResponse;
import com.wheats.api.order.dto.OrderRequest;
import com.wheats.api.order.dto.OrderResponse;
import com.wheats.api.order.entity.CartEntity;
import com.wheats.api.order.entity.CartItemEntity;
import com.wheats.api.order.entity.CartItemStatus;
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
     * @param userId 사용자 ID (인증된 사용자)
     * @param request 주문 요청 (cartId 포함)
     */
    @Transactional
    public OrderResponse createOrder(Long userId, OrderRequest request) {
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
        List<CartItemEntity> cartItems = cartItemRepository.findByCartIdAndStatus(cartId, CartItemStatus.ACTIVE);
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

        // 9) 장바구니/아이템은 삭제하지 않는다.
        //    - CartItem은 ORDERED로 상태 전환하여 주문 이력 보존
        //    - Cart는 ORDERED로 상태 전환하여 주문 이력 보존
        cartItemRepository.updateStatusByCartId(cartId, CartItemStatus.ORDERED);
        cart.setStatus(CartStatus.ORDERED);
        cartRepository.save(cart);

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
     * @param userId 사용자 ID (인증된 사용자)
     * @param orderId 주문 ID
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetail(Long userId, Long orderId) {

        OrderEntity order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("주문을 찾을 수 없습니다. id=" + orderId));

        // 본인 주문인지 확인 (나중에 어드민/점주는 별도 권한 체크)
        if (!order.getUserId().equals(userId)) {
            throw new IllegalArgumentException("본인 주문이 아닙니다.");
        }

        // 주문 아이템 조회
        List<OrderItemEntity> orderItems = orderItemRepository.findByOrderId(orderId);
        List<OrderItemResponse> itemResponses = new ArrayList<>();
        int orderAmount = 0;

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
            orderAmount += item.getUnitPrice() * item.getQuantity();
        }

        // 매장 정보 조회
        StoreEntity store = storeRepository.findById(order.getStoreId())
                .orElse(null);
        String storeName = (store != null) ? store.getName() : "(삭제된 가게)";
        String storeAddress = (store != null && store.getDescription() != null) 
                ? store.getDescription() 
                : "서울시 강남구 테헤란로 123"; // 기본 주소 (실제로는 별도 주소 필드 필요)
        int deliveryFee = (store != null && store.getDeliveryTip() != null) 
                ? store.getDeliveryTip() 
                : 0;

        // 사용자 정보 조회
        UserEntity user = userRepository.findById(order.getUserId())
                .orElse(null);
        String userName = (user != null) ? user.getName() : "";
        String userEmail = (user != null) ? user.getEmail() : "";

        // 영수증 플래그 조회
        String receiptFlag = (order.getReceiptFlag() != null) ? order.getReceiptFlag() : "";

        return new OrderDetailResponse(
                order.getId(),
                order.getOrderNumber(),
                order.getStatus().name(),
                order.getTotalPrice(),
                order.getCreatedAt(),
                order.getPaidAt(),
                itemResponses,
                storeName,
                storeAddress,
                deliveryFee,
                orderAmount,
                userName,
                userEmail,
                receiptFlag
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
