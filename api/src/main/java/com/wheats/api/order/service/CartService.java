package com.wheats.api.order.service;

import com.wheats.api.order.dto.CartItemRequest;
import com.wheats.api.order.dto.CartItemResponse;
import com.wheats.api.order.dto.CartResponse;
import com.wheats.api.order.dto.UpdateCartItemQuantityRequest;
import com.wheats.api.order.entity.CartEntity;
import com.wheats.api.order.entity.CartItemEntity;
import com.wheats.api.order.entity.CartItemStatus;
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

        // 규칙:
        // - 사용자당 ACTIVE 카트는 1개만 허용(서비스 로직으로 보장)
        // - 다른 매장 담기 시 force=false면 409, force=true면 기존 ACTIVE를 CANCELLED로 전환 후 새 ACTIVE 생성
        Optional<CartEntity> activeCartOpt = cartRepository.findFirstByUserIdAndStatusOrderByCreatedAtDesc(userId, CartStatus.ACTIVE);

        CartEntity cart;
        if (activeCartOpt.isPresent()) {
            CartEntity activeCart = activeCartOpt.get();
            if (!activeCart.getStoreId().equals(storeId)) {
                if (!force) {
                    throw new CartConflictException(buildCartResponse(activeCart));
                }
                // 다른 매장 ACTIVE → CANCELLED 전환(삭제 금지)
                activeCart.setStatus(CartStatus.CANCELLED);
                cartRepository.save(activeCart);
                cartRepository.flush();
                // 기존 카트의 아이템도 CANCELLED 처리(조회에서 제외되도록 상태 동기화)
                cartItemRepository.updateStatusByCartId(activeCart.getId(), CartItemStatus.CANCELLED);

                // 새 ACTIVE 생성
                cart = cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE));
            } else {
                cart = activeCart;
            }
        } else {
            cart = cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE));
        }

        // 4. 메뉴 추가 로직
        Optional<CartItemEntity> existed = cartItemRepository.findByCartIdAndMenuIdAndStatus(
                cart.getId(), request.getMenuId(), CartItemStatus.ACTIVE
        );

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
            item.setStatus(CartItemStatus.CANCELLED);
            item.setQuantity(0);
            cartItemRepository.save(item);
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

        item.setStatus(CartItemStatus.CANCELLED);
        item.setQuantity(0);
        cartItemRepository.save(item);

        return buildCartResponse(cart);
    }

    private CartResponse buildCartResponse(CartEntity cart) {
        StoreEntity store = storeRepository.findById(cart.getStoreId())
                .orElseThrow(() -> new NoSuchElementException("Store not found. id=" + cart.getStoreId()));

        List<CartItemEntity> items = cartItemRepository.findByCartIdAndStatus(cart.getId(), CartItemStatus.ACTIVE);

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
