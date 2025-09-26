package com.back.domain.cart.service;

import com.back.domain.cart.calculator.CartCalculator;
import com.back.domain.cart.dto.request.CartRequestDto;
import com.back.domain.cart.dto.response.CartListResponseDto;
import com.back.domain.cart.dto.response.CartResponseDto;
import com.back.domain.cart.entity.Cart;
import com.back.domain.cart.repository.CartRepository;
import com.back.domain.product.product.entity.Product;
// import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartCalculator cartCalculator;
    // private final ProductRepository productRepository;

    /**
     * 장바구니에 상품 추가
     */
    @Transactional
    public CartResponseDto addToCart(User user, CartRequestDto requestDto) {
        // 1. 상품 존재 확인 (ProductRepository 구현 후 활성화)
        // Product product = productRepository.findById(requestDto.getProductId())
        //         .orElseThrow(() -> new ServiceException("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."));
        
        // 임시로 더미 Product 객체 생성 (id는 BaseEntity에서 자동 생성되므로 Builder에서 설정 불가)
        // TODO: ProductRepository 구현 후 실제 Product 조회로 대체
        Product product = new Product(); // 기본 생성자 사용
        // setter로 필수 필드들만 임시 설정
        product.setName("임시 상품명");
        product.setPrice(10000);

        // 2. 장바구니 타입 변환 (도메인에서 처리)
        Cart.CartType cartType = Cart.CartType.fromString(requestDto.cartType());

        // 3. 중복 상품 확인 및 처리
        Optional<Cart> existingCart = cartRepository.findByUserAndProductAndCartType(user, product, cartType);

        if (existingCart.isPresent()) {
            // 기존 상품이 있으면 수량 증가
            Cart cart = existingCart.get();
            cart.changeQuantity(cart.getQuantity() + requestDto.quantity());

            // 옵션 정보가 있으면 업데이트
            if (requestDto.optionInfo() != null) {
                cart.changeOptionInfo(requestDto.optionInfo());
            }

            Cart savedCart = cartRepository.save(cart);
            return CartResponseDto.from(savedCart);
        }

        // 4. 새로운 장바구니 아이템 생성 (중복이 없을 때만)
        Cart newCart = Cart.builder()
                .user(user)
                .product(product)
                .quantity(requestDto.quantity())
                .optionInfo(requestDto.optionInfo())
                .cartType(cartType)
                .isSelected(true)
                .build();

        Cart savedCart = cartRepository.save(newCart);
        return CartResponseDto.from(savedCart);
    }

    /**
     * 장바구니 목록 조회 (일반/펀딩 구분)
     * 성능 최적화: 한 번의 쿼리로 모든 데이터 조회 후 메모리에서 분리
     */
    public CartListResponseDto getCartItems(User user) {
        // 1. 모든 장바구니 아이템을 한 번에 조회 (N+1 문제 해결)
        List<Cart> allCarts = cartRepository.findByUserWithProduct(user);

        // 2. 메모리에서 타입별로 분리
        List<CartResponseDto> normalCartItems = allCarts.stream()
                .filter(cart -> cart.getCartType() == Cart.CartType.NORMAL)
                .map(CartResponseDto::from)
                .collect(Collectors.toList());

        List<CartResponseDto> fundingCartItems = allCarts.stream()
                .filter(cart -> cart.getCartType() == Cart.CartType.FUNDING)
                .map(CartResponseDto::from)
                .collect(Collectors.toList());

        // 3. 총합 계산
        Integer totalNormalQuantity = cartCalculator.calculateTotalQuantity(normalCartItems);
        Integer totalFundingQuantity = cartCalculator.calculateTotalQuantity(fundingCartItems);
        Integer totalNormalAmount = cartCalculator.calculateTotalAmount(normalCartItems);
        Integer totalFundingAmount = cartCalculator.calculateTotalAmount(fundingCartItems);


        return new CartListResponseDto(
                normalCartItems,
                fundingCartItems,
                totalNormalQuantity,
                totalFundingQuantity,
                totalNormalAmount,
                totalFundingAmount
        );
    }

    /**
     * 장바구니 수량 수정
     */
    @Transactional
    public CartResponseDto updateQuantity(User user, Long cartId, Integer quantity) {
        // 1. 장바구니 아이템 조회 및 권한 확인
        Cart cart = findCartByIdAndValidateOwnership(cartId, user);

        // 2. 수량 수정 (도메인 메서드에서 검증)
        cart.changeQuantity(quantity);
        Cart updatedCart = cartRepository.save(cart);

        return CartResponseDto.from(updatedCart);
    }

    /**
     * 장바구니에서 상품 삭제
     */
    @Transactional
    public void removeFromCart(User user, Long cartId) {
        // 1. 장바구니 아이템 조회 및 권한 확인
        Cart cart = findCartByIdAndValidateOwnership(cartId, user);

        // 2. 삭제
        cartRepository.delete(cart);
    }

    /**
     * 장바구니 전체 삭제
     */
    @Transactional
    public void clearCart(User user) {
        cartRepository.deleteByUser(user);
    }

    /**
     * 특정 타입 장바구니 삭제
     */
    @Transactional
    public void clearCartByType(User user, String cartType) {
        Cart.CartType type = Cart.CartType.fromString(cartType);
        cartRepository.deleteByUserAndCartType(user, type);
    }

    /**
     * 장바구니 선택 상태 토글
     */
    @Transactional
    public CartResponseDto toggleSelection(User user, Long cartId) {
        Cart cart = findCartByIdAndValidateOwnership(cartId, user);

        if (cart.getIsSelected()) {
            cart.unselect();
        } else {
            cart.select();
        }
        Cart updatedCart = cartRepository.save(cart);

        return CartResponseDto.from(updatedCart);
    }

    /**
     * 선택된 장바구니 아이템들만 조회
     */
    public List<CartResponseDto> getSelectedCartItems(User user) {
        List<Cart> selectedCarts = cartRepository.findByUserAndIsSelectedTrue(user);
        return selectedCarts.stream()
                .map(CartResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 장바구니 조회 및 권한 확인
     */
    private Cart findCartByIdAndValidateOwnership(Long cartId, User user) {
        Cart cart = cartRepository.findById(cartId)
                .orElseThrow(() -> new ServiceException("CART_NOT_FOUND", "존재하지 않는 장바구니 아이템입니다."));

        // Tell, don't ask 원칙 적용
        cart.validateOwnership(user);
        return cart;
    }

}
