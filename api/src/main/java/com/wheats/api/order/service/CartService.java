package com.wheats.api.order.service;

import com.wheats.api.order.dto.CartItemRequest;
import com.wheats.api.order.dto.CartItemResponse;
import com.wheats.api.order.dto.CartResponse;
import com.wheats.api.order.dto.UpdateCartItemQuantityRequest;
import com.wheats.api.order.entity.CartEntity;
import com.wheats.api.order.entity.CartItemEntity;
import com.wheats.api.order.entity.CartStatus;
import com.wheats.api.order.exception.CartConflictException;
import com.wheats.api.order.repository.CartItemRepository;
import com.wheats.api.order.repository.CartRepository;
import com.wheats.api.order.repository.OrderRepository;
import com.wheats.api.store.entity.MenuEntity;
import com.wheats.api.store.entity.StoreEntity;
import com.wheats.api.store.repository.MenuRepository;
import com.wheats.api.store.repository.StoreRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final OrderRepository orderRepository;
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       OrderRepository orderRepository,
                       StoreRepository storeRepository,
                       MenuRepository menuRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.orderRepository = orderRepository;
        this.storeRepository = storeRepository;
        this.menuRepository = menuRepository;
    }

    @Transactional(readOnly = true)
    public Optional<CartResponse> getMyCart(Long userId) {
        Optional<CartEntity> cartOpt = cartRepository
                .findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, CartStatus.ACTIVE);

        if (cartOpt.isEmpty()) {
            return Optional.empty();
        }

        CartEntity cart = cartOpt.get();
        return Optional.of(buildCartResponse(cart));
    }

    @Transactional
    public CartResponse addItem(Long userId, CartItemRequest request, boolean force) {
        Long storeId = request.getStoreId();

        // 1. 현재 사용자의 모든 ACTIVE 장바구니 조회
        List<CartEntity> activeCarts = cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE);

        CartEntity cart;

        // 2. 다른 가게의 장바구니가 이미 있다면?
        Optional<CartEntity> otherStoreCart = activeCarts.stream()
                .filter(c -> !c.getStoreId().equals(storeId))
                .findFirst();

        if (otherStoreCart.isPresent()) {
            if (!force) {
                // force=false인 경우: 409 Conflict 예외 발생 (현재 장바구니 정보 포함)
                CartEntity existingCart = otherStoreCart.get();
                CartResponse existingCartResponse = buildCartResponse(existingCart);
                throw new CartConflictException(existingCartResponse);
            } else {
                // force=true인 경우: 다른 가게 ACTIVE 장바구니를 ABANDONED로 전환
                // (단, 해당 store에 ABANDONED가 이미 있으면 - 기존 버그/이력 - 안전하게 정리)
                CartEntity toAbandon = otherStoreCart.get();
                cleanupAbandonedConflictIfNeeded(userId, toAbandon.getStoreId(), toAbandon.getId());

                toAbandon.setStatus(CartStatus.ABANDONED);
                cartRepository.save(toAbandon);
                cartRepository.flush(); // 즉시 반영
                // NOTE:
                // carts 테이블은 orders.cart_id 에 의해 참조될 수 있으며(ON DELETE RESTRICT),
                // 카트를 삭제하면 FK 제약으로 500이 발생할 수 있다.
                // 따라서 ABANDONED 카트는 삭제하지 않는다. (필요 시 아이템만 정리하는 방향으로 처리)

                // 요청한 storeId의 ACTIVE 장바구니를 가져오거나(없으면 ABANDONED 재활용/생성)
                cart = getOrCreateActiveCart(userId, storeId);
            }
        } else {
            // 같은 가게 ACTIVE 장바구니가 없으면 ABANDONED를 재활용하여 ACTIVE로 전환(정규화)
            cart = getOrCreateActiveCart(userId, storeId);
        }

        // 4. 메뉴 추가 로직
        Optional<CartItemEntity> existed = cartItemRepository.findByCartIdAndMenuId(cart.getId(), request.getMenuId());

        if (existed.isPresent()) {
            CartItemEntity item = existed.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItemEntity newItem = new CartItemEntity(cart.getId(), request.getMenuId(), request.getQuantity());
            cartItemRepository.save(newItem);
        }

        // 5. 최종 응답 빌드 전 영속성 반영
        cartItemRepository.flush();

        return buildCartResponse(cart);
    }

    /**
     * 장바구니 라이프사이클 정규화 규칙:
     * - (user_id, store_id, status) 유니크 제약 때문에 같은 store에 ACTIVE/ABANDONED가 동시에 여러 개 생기면 안 됨
     * - 따라서 ACTIVE가 없고 ABANDONED가 있으면 ABANDONED를 "재활용"하여 ACTIVE로 전환한다.
     * - 주문이 참조하는 cart(orders.cart_id)는 삭제하면 안 됨.
     */
    private CartEntity getOrCreateActiveCart(Long userId, Long storeId) {
        Optional<CartEntity> existingActive = cartRepository
                .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ACTIVE);
        if (existingActive.isPresent()) {
            return existingActive.get();
        }

        Optional<CartEntity> abandoned = cartRepository
                .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ABANDONED);
        if (abandoned.isPresent()) {
            CartEntity abandonedCart = abandoned.get();

            // 과거 버그/이력으로 ABANDONED 상태인데 주문이 참조하는 경우가 있을 수 있음 → ORDERED로 정규화 시도
            if (orderRepository.existsByCartId(abandonedCart.getId())) {
                Optional<CartEntity> existingOrdered = cartRepository
                        .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ORDERED);
                if (existingOrdered.isEmpty()) {
                    abandonedCart.setStatus(CartStatus.ORDERED);
                    cartRepository.save(abandonedCart);
                    cartRepository.flush();
                }
                // 이 카트는 재활용 불가 → 새 ACTIVE 생성
                return cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE));
            }

            // 재활용: 아이템 비우고 ACTIVE로 전환
            cartItemRepository.deleteAllByCartId(abandonedCart.getId());
            cartItemRepository.flush();
            abandonedCart.setStatus(CartStatus.ACTIVE);
            cartRepository.save(abandonedCart);
            cartRepository.flush();
            return abandonedCart;
        }

        return cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE));
    }

    /**
     * force=true로 다른 가게 ACTIVE를 ABANDONED로 바꿀 때,
     * 해당 store에 이미 ABANDONED가 존재하면 유니크 제약 충돌이 날 수 있어 사전 정리한다.
     *
     * 정리 정책:
     * - 주문이 참조하지 않는 ABANDONED 카트는 "아이템만 비우고" 그대로 둔다(삭제 최소화)
     * - 단, 유니크 충돌을 피해야 하므로, 기존 ABANDONED가 다른 cartId라면 상태를 ORDERED로 정규화 시도(가능할 때만)
     */
    private void cleanupAbandonedConflictIfNeeded(Long userId, Long storeId, Long currentActiveCartId) {
        Optional<CartEntity> existingAbandoned = cartRepository
                .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ABANDONED);
        if (existingAbandoned.isEmpty()) return;

        CartEntity abandonedCart = existingAbandoned.get();
        if (abandonedCart.getId().equals(currentActiveCartId)) return;

        // 우선 아이템 정리
        cartItemRepository.deleteAllByCartId(abandonedCart.getId());
        cartItemRepository.flush();

        // 주문이 참조하는 ABANDONED라면(이력 오류) ORDERED로 정규화 시도
        if (orderRepository.existsByCartId(abandonedCart.getId())) {
            Optional<CartEntity> existingOrdered = cartRepository
                    .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ORDERED);
            if (existingOrdered.isEmpty()) {
                abandonedCart.setStatus(CartStatus.ORDERED);
                cartRepository.save(abandonedCart);
                cartRepository.flush();
            }
        }
    }

    @Transactional
    public CartResponse updateItemQuantity(Long userId,
                                           Long cartItemId,
                                           UpdateCartItemQuantityRequest request) {

        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found. id=" + cartItemId));

        CartEntity cart = cartRepository.findById(item.getCartId())
                .orElseThrow(() -> new NoSuchElementException("Cart not found. id=" + item.getCartId()));

        if (!cart.getUserId().equals(userId)) {
            throw new IllegalStateException("You cannot modify another user's cart.");
        }

        int newQuantity = request.getQuantity();
        if (newQuantity <= 0) {
            cartItemRepository.delete(item);
        } else {
            item.setQuantity(newQuantity);
            cartItemRepository.save(item);
        }

        return buildCartResponse(cart);
    }

    @Transactional
    public CartResponse removeItem(Long userId, Long cartItemId) {
        CartItemEntity item = cartItemRepository.findById(cartItemId)
                .orElseThrow(() -> new NoSuchElementException("Cart item not found. id=" + cartItemId));

        CartEntity cart = cartRepository.findById(item.getCartId())
                .orElseThrow(() -> new NoSuchElementException("Cart not found. id=" + item.getCartId()));

        if (!cart.getUserId().equals(userId)) {
            throw new IllegalStateException("You cannot modify another user's cart.");
        }

        cartItemRepository.delete(item);

        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(CartEntity cart) {
        StoreEntity store = storeRepository.findById(cart.getStoreId())
                .orElseThrow(() -> new NoSuchElementException("Store not found. id=" + cart.getStoreId()));

        List<CartItemEntity> items = cartItemRepository.findByCartId(cart.getId());

        List<CartItemResponse> itemResponses = items.stream()
                .map(it -> {
                    MenuEntity menu = menuRepository.findById(it.getMenuId())
                            .orElseThrow(() -> new NoSuchElementException("Menu not found. id=" + it.getMenuId()));

                    int unitPrice = menu.getPrice();
                    int linePrice = unitPrice * it.getQuantity();

                    return new CartItemResponse(
                            it.getId(),
                            menu.getId(),
                            menu.getName(),
                            it.getQuantity(),
                            unitPrice,
                            linePrice
                    );
                })
                .collect(Collectors.toList());

        int totalPrice = itemResponses.stream()
                .mapToInt(CartItemResponse::getLinePrice)
                .sum();

        return new CartResponse(
                cart.getId(),
                store.getId(),
                store.getName(),
                itemResponses,
                totalPrice
        );
    }
}
