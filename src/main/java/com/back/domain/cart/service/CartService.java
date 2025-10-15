package com.back.domain.cart.service;

import com.back.domain.cart.calculator.CartCalculator;
import com.back.domain.cart.dto.request.CartRequestDto;
import com.back.domain.cart.dto.response.CartListResponseDto;
import com.back.domain.cart.dto.response.CartResponseDto;
import com.back.domain.cart.entity.Cart;
import com.back.domain.cart.repository.CartRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CartService {

    private final CartRepository cartRepository;
    private final CartCalculator cartCalculator;
    private final ProductRepository productRepository;
    private final com.back.domain.funding.repository.FundingRepository fundingRepository;

    /**
     * 장바구니에 상품 추가
     */
    @Transactional
    public CartResponseDto addToCart(User user, CartRequestDto requestDto) {
        // 1. 유효성 검증
        requestDto.validate();
        
        // 2. 장바구니 타입 변환
        Cart.CartType cartType = Cart.CartType.fromString(requestDto.cartType());
        
        if (cartType == Cart.CartType.NORMAL) {
            // 일반 장바구니 처리
            return addNormalCart(user, requestDto, cartType);
        } else {
            // 펀딩 장바구니 처리
            return addFundingCart(user, requestDto, cartType);
        }
    }
    
    /**
     * 일반 장바구니 추가
     */
    private CartResponseDto addNormalCart(User user, CartRequestDto requestDto, Cart.CartType cartType) {
        // 상품 존재 확인
        Product product = productRepository.findByProductUuid(requestDto.productUuid())
                .orElseThrow(() -> new ServiceException("PRODUCT_NOT_FOUND", "존재하지 않는 상품입니다."));
        
        // 중복 확인
        Optional<Cart> existingCart = cartRepository.findByUserAndProductAndCartType(user, product, cartType);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.changeQuantity(cart.getQuantity() + requestDto.quantity());
            
            if (requestDto.optionInfo() != null) {
                cart.changeOptionInfo(requestDto.optionInfo());
            }
            
            return CartResponseDto.from(cartRepository.save(cart));
        }
        
        // 새로운 장바구니 생성
        Cart newCart = Cart.builder()
                .user(user)
                .product(product)
                .funding(null)
                .quantity(requestDto.quantity())
                .optionInfo(requestDto.optionInfo())
                .cartType(cartType)
                .isSelected(true)
                .fundingId(null)
                .fundingPrice(null)
                .fundingStock(null)
                .build();
        
        return CartResponseDto.from(cartRepository.save(newCart));
    }
    
    /**
     * 펀딩 장바구니 추가
     */
    private CartResponseDto addFundingCart(User user, CartRequestDto requestDto, Cart.CartType cartType) {
        // 펀딩 존재 확인
        com.back.domain.funding.entity.Funding funding = fundingRepository.findById(requestDto.fundingId())
                .orElseThrow(() -> new ServiceException("FUNDING_NOT_FOUND", "존재하지 않는 펀딩입니다."));
        
        // 중복 확인
        Optional<Cart> existingCart = cartRepository.findByUserAndFundingAndCartType(user, funding, cartType);
        
        if (existingCart.isPresent()) {
            Cart cart = existingCart.get();
            cart.changeQuantity(cart.getQuantity() + requestDto.quantity());
            return CartResponseDto.from(cartRepository.save(cart));
        }
        
        // 새로운 펀딩 장바구니 생성
        Cart newCart = Cart.builder()
                .user(user)
                .product(null)
                .funding(funding)
                .quantity(requestDto.quantity())
                .optionInfo(null)
                .cartType(cartType)
                .isSelected(true)
                .fundingId(requestDto.fundingId().toString())
                .fundingPrice(requestDto.fundingPrice() != null ? requestDto.fundingPrice() : (int) funding.getPrice())
                .fundingStock(requestDto.fundingStock() != null ? requestDto.fundingStock() : funding.getStock())
                .build();
        
        return CartResponseDto.from(cartRepository.save(newCart));
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
     * 장바구니 전체 선택 토글
     */
    @Transactional
    public List<CartResponseDto> toggleAllSelection(User user, boolean isSelected) {
        // 1. 사용자의 모든 장바구니 아이템 조회 (상품/펀딩 fetch join)
        List<Cart> userCarts = cartRepository.findByUserWithProduct(user);

        // 2. 모든 아이템의 선택 상태를 일괄 변경
        for (Cart cart : userCarts) {
            if (isSelected) {
                cart.select();
            } else {
                cart.unselect();
            }
        }

        // 3. 일괄 저장
        List<Cart> updatedCarts = cartRepository.saveAll(userCarts);

        // 4. 응답 DTO 변환
        return updatedCarts.stream()
                .map(CartResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 선택된 장바구니 아이템 조회
     * @param user 사용자
     * @param validateForOrder 주문용 유효성 검증 여부 (true: 유효한 것만, false: 모두)
     */
    public List<CartResponseDto> getSelectedCartItems(User user, boolean validateForOrder) {
        // N+1 방지를 위해 Fetch Join 사용
        List<Cart> selectedCarts = cartRepository.findByUserAndIsSelectedTrueWithProduct(user);
        
        Stream<Cart> stream = selectedCarts.stream();
        
        // 주문용일 때만 유효성 검증
        if (validateForOrder) {
            stream = stream.filter(Cart::isValid);
        }
        
        return stream
                .map(CartResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 전체 장바구니 아이템 조회
     * @param user 사용자
     * @param validateForOrder 주문용 유효성 검증 여부 (true: 유효한 것만, false: 모두)
     */
    public List<CartResponseDto> getAllCartItems(User user, boolean validateForOrder) {
        List<Cart> allCarts = cartRepository.findByUserWithProduct(user);
        
        Stream<Cart> stream = allCarts.stream();
        
        // 주문용일 때만 유효성 검증
        if (validateForOrder) {
            stream = stream.filter(Cart::isValid);
        }
        
        return stream
                .map(CartResponseDto::from)
                .collect(Collectors.toList());
    }

    /**
     * 주문 가능한 장바구니 아이템들 검증
     */
    public void validateCartItemsForOrder(User user, boolean isFullOrder) {
        List<Cart> cartItems = isFullOrder ? 
            cartRepository.findByUserWithProduct(user) :
            cartRepository.findByUserAndIsSelectedTrueWithProduct(user);

        if (cartItems.isEmpty()) {
            throw new ServiceException("CART_EMPTY", "주문할 장바구니 아이템이 없습니다.");
        }

        // 레거시 데이터 보정 (펀딩 price/stock 누락 대비)
        cartItems.forEach(this::normalizeCartFields);

        // 각 아이템의 유효성 검증
        for (Cart cart : cartItems) {
            if (!cart.isValid()) {
                String productName = cart.getProduct() != null ? cart.getProduct().getName() : "알 수 없는 상품";
                throw new ServiceException("CART_INVALID", 
                    String.format("장바구니 아이템 '%s'이(가) 주문 불가능한 상태입니다.", productName));
            }
        }

        // 펀딩 상품과 일반 상품이 섞여있는지 확인
        boolean hasNormalProducts = cartItems.stream()
            .anyMatch(cart -> cart.getCartType() == Cart.CartType.NORMAL);
        boolean hasFundingProducts = cartItems.stream()
            .anyMatch(cart -> cart.getCartType() == Cart.CartType.FUNDING);

        if (hasNormalProducts && hasFundingProducts) {
            throw new ServiceException("CART_MIXED_TYPES", 
                "일반 상품과 펀딩 상품은 함께 주문할 수 없습니다.");
        }
    }

    private void normalizeCartFields(Cart cart) {
        if (cart.isFundingCart() && cart.getFunding() != null) {
            if (cart.getFundingPrice() == null) {
                cart.updateFundingInfo(
                        cart.getFundingId(),
                        (int) cart.getFunding().getPrice(),
                        cart.getFundingStock()
                );
            }
            if (cart.getFundingStock() == null) {
                cart.updateFundingInfo(
                        cart.getFundingId(),
                        cart.getFundingPrice(),
                        cart.getFunding().getStock()
                );
            }
        }
    }

    /**
     * 장바구니 총 금액 계산 (전체/선택)
     */
    public Integer calculateTotalAmount(User user, boolean isFullOrder) {
        List<Cart> cartItems = isFullOrder ? 
            cartRepository.findByUserWithProduct(user) :
            cartRepository.findByUserAndIsSelectedTrueWithProduct(user);

        // 레거시 보정(펀딩 price/stock 누락 대비)
        cartItems.forEach(this::normalizeCartFields);

        return cartItems.stream()
            .filter(Cart::isValid)
            .mapToInt(Cart::getTotalPrice)
            .sum();
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
