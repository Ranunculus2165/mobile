package com.wheats.api.order.service;

import com.wheats.api.order.dto.CartItemRequest;
import com.wheats.api.order.dto.CartItemResponse;
import com.wheats.api.order.dto.CartResponse;
import com.wheats.api.order.dto.UpdateCartItemQuantityRequest;
import com.wheats.api.order.entity.CartEntity;
import com.wheats.api.order.entity.CartItemEntity;
import com.wheats.api.order.entity.CartStatus;
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
    public CartResponse addItem(Long userId, CartItemRequest request) {
        Long storeId = request.getStoreId();

        // user + store 기준 ACTIVE 장바구니 조회 (없으면 새로 생성)
        CartEntity cart = cartRepository
                .findFirstByUserIdAndStoreIdAndStatusOrderByCreatedAtDesc(userId, storeId, CartStatus.ACTIVE)
                .orElseGet(() -> cartRepository.save(new CartEntity(userId, storeId, CartStatus.ACTIVE)));

        // 같은 메뉴가 이미 있으면 수량만 증가
        Optional<CartItemEntity> existed =
                cartItemRepository.findByCartIdAndMenuId(cart.getId(), request.getMenuId());

        if (existed.isPresent()) {
            CartItemEntity item = existed.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
            cartItemRepository.save(item);
        } else {
            CartItemEntity newItem =
                    new CartItemEntity(cart.getId(), request.getMenuId(), request.getQuantity());
            cartItemRepository.save(newItem);
        }

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
