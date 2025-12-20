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
    private final StoreRepository storeRepository;
    private final MenuRepository menuRepository;

    public CartService(CartRepository cartRepository,
                       CartItemRepository cartItemRepository,
                       StoreRepository storeRepository,
                       MenuRepository menuRepository) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
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
                // force=true인 경우: 기존 장바구니를 ABANDONED로 변경 (아이템 유지)
                CartEntity toAbandon = otherStoreCart.get();
                toAbandon.setStatus(CartStatus.ABANDONED);
                cartRepository.save(toAbandon);
                cartRepository.flush(); // 즉시 반영

                // 같은 가게의 ABANDONED 장바구니가 있으면 삭제 (유니크 제약조건 문제 방지)
                Optional<CartEntity> abandonedCartOpt = cartRepository
                        .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ABANDONED);
                if (abandonedCartOpt.isPresent()) {
                    CartEntity abandonedCart = abandonedCartOpt.get();
                    cartItemRepository.deleteAllByCartId(abandonedCart.getId());
                    cartRepository.deleteById(abandonedCart.getId());
                    cartRepository.flush();
                }

                // 같은 가게의 ACTIVE 장바구니가 이미 있는지 먼저 확인
                Optional<CartEntity> existingActiveCart = cartRepository
                        .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ACTIVE);
                
                if (existingActiveCart.isPresent()) {
                    // 이미 존재하면 기존 장바구니 사용
                    cart = existingActiveCart.get();
                } else {
                    // 없을 때만 새로 생성
                    cart = cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE));
                }
            }
        } else {
            // 3. 같은 가게의 ACTIVE 장바구니가 있는지 먼저 확인
            Optional<CartEntity> existingActiveCart = cartRepository
                    .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ACTIVE);
            
            if (existingActiveCart.isPresent()) {
                // 이미 존재하면 기존 장바구니 사용
                cart = existingActiveCart.get();
            } else {
                // 같은 가게의 ABANDONED 장바구니가 있으면 먼저 삭제 (유니크 제약조건 문제 방지)
                Optional<CartEntity> abandonedCartOpt = cartRepository
                        .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ABANDONED);
                if (abandonedCartOpt.isPresent()) {
                    CartEntity abandonedCart = abandonedCartOpt.get();
                    cartItemRepository.deleteAllByCartId(abandonedCart.getId());
                    cartRepository.deleteById(abandonedCart.getId());
                    cartRepository.flush();
                }
                // 없을 때만 새로 생성
                cart = cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE));
            }
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
