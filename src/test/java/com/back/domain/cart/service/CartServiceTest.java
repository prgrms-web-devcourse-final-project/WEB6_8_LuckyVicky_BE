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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("장바구니 서비스 테스트")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    
    @Mock
    private CartCalculator cartCalculator;
    
    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private com.back.domain.funding.repository.FundingRepository fundingRepository;
    
    @InjectMocks
    private CartService cartService;

    private User testUser;
    private User anotherUser;
    private Product testProduct;
    private com.back.domain.funding.entity.Funding testFunding;
    private Cart testNormalCart;
    private Cart testFundingCart;
    private CartRequestDto normalCartRequestDto;
    private CartRequestDto fundingCartRequestDto;
    
    private static final UUID TEST_PRODUCT_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        // 테스트용 사용자 생성 (User 엔티티에 @Setter 없으므로 Mock 사용)
        testUser = mock(User.class);
        when(testUser.getId()).thenReturn(1L);
        when(testUser.getEmail()).thenReturn("test@example.com");
        
        anotherUser = mock(User.class);
        when(anotherUser.getId()).thenReturn(2L);
        when(anotherUser.getEmail()).thenReturn("another@example.com");

        // 테스트용 상품 생성 (Mock 사용)
        testProduct = mock(Product.class);
        when(testProduct.getId()).thenReturn(1L);
        when(testProduct.getProductUuid()).thenReturn(TEST_PRODUCT_UUID);
        when(testProduct.getName()).thenReturn("테스트 상품");
        when(testProduct.getPrice()).thenReturn(10000);
        when(testProduct.getDiscountPrice()).thenReturn(10000);
        when(testProduct.isDeleted()).thenReturn(false);
        when(testProduct.getStock()).thenReturn(100);

        // 테스트용 펀딩 생성 (Mock 사용)
        testFunding = mock(com.back.domain.funding.entity.Funding.class);
        when(testFunding.getId()).thenReturn(2L);
        when(testFunding.getTitle()).thenReturn("테스트 펀딩");
        when(testFunding.getPrice()).thenReturn(50000L);
        when(testFunding.getStock()).thenReturn(100);
        when(testFunding.getImageUrl()).thenReturn("https://test.com/funding.jpg");

        // 테스트용 장바구니 생성 (일반) - id는 BaseEntity에서 자동 생성되므로 Builder에서 설정 불가
        testNormalCart = Cart.builder()
                .user(testUser)
                .product(testProduct)
                .funding(null)
                .quantity(2)
                .cartType(Cart.CartType.NORMAL)
                .isSelected(true)
                .optionInfo("일반 상품 옵션")
                .build();

        // 테스트용 장바구니 생성 (펀딩) - id는 BaseEntity에서 자동 생성되므로 Builder에서 설정 불가
        testFundingCart = Cart.builder()
                .user(testUser)
                .product(null)
                .funding(testFunding)
                .quantity(1)
                .cartType(Cart.CartType.FUNDING)
                .isSelected(true)
                .fundingId("2")
                .fundingPrice(50000)
                .fundingStock(100)
                .build();

        // 테스트용 요청 DTO 생성 (일반)
        normalCartRequestDto = new CartRequestDto(
                TEST_PRODUCT_UUID, // productUuid
                2,                 // quantity
                "일반 상품 옵션",    // optionInfo
                "NORMAL",          // cartType
                null,              // fundingId
                null,              // fundingPrice
                null               // fundingStock
        );

        // 테스트용 요청 DTO 생성 (펀딩)
        fundingCartRequestDto = new CartRequestDto(
                null,              // productUuid (펀딩은 필요 없음)
                1,                 // quantity
                null,              // optionInfo (펀딩은 옵션 안씀)
                "FUNDING",         // cartType
                2L,                // fundingId (Long 타입)
                50000,             // fundingPrice
                100                // fundingStock
        );

        // CartCalculator Mock 설정
        given(cartCalculator.calculateTotalQuantity(anyList())).willReturn(0);
        given(cartCalculator.calculateTotalAmount(anyList())).willReturn(0);
    }

    @Test
    @DisplayName("장바구니에 새 상품 추가 성공")
    void addToCart_NewProduct_Success() {
        // Given
        given(productRepository.findByProductUuid(TEST_PRODUCT_UUID)).willReturn(Optional.of(testProduct));
        given(cartRepository.findByUserAndProductAndCartType(eq(testUser), any(Product.class), eq(Cart.CartType.NORMAL)))
                .willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(testNormalCart);

        // When
        CartResponseDto result = cartService.addToCart(testUser, normalCartRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.quantity()).isEqualTo(2);
        assertThat(result.cartType()).isEqualTo("NORMAL");
        assertThat(result.optionInfo()).isEqualTo("일반 상품 옵션");

        verify(productRepository).findByProductUuid(TEST_PRODUCT_UUID);
        verify(cartRepository).findByUserAndProductAndCartType(eq(testUser), any(Product.class), eq(Cart.CartType.NORMAL));
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("장바구니에 기존 상품 추가 - 수량 증가")
    void addToCart_ExistingProduct_QuantityIncrease() {
        // Given
        Cart existingCart = Cart.builder()
                .user(testUser)
                .product(testProduct)
                .quantity(3)
                .cartType(Cart.CartType.NORMAL)
                .isSelected(true)
                .optionInfo("기존 옵션")
                .build();

        given(productRepository.findByProductUuid(TEST_PRODUCT_UUID)).willReturn(Optional.of(testProduct));
        given(cartRepository.findByUserAndProductAndCartType(eq(testUser), any(Product.class), eq(Cart.CartType.NORMAL)))
                .willReturn(Optional.of(existingCart));
        given(cartRepository.save(existingCart)).willReturn(existingCart);

        // When
        CartResponseDto result = cartService.addToCart(testUser, normalCartRequestDto);

        // Then
        assertThat(existingCart.getQuantity()).isEqualTo(5); // 3 + 2 = 5
        assertThat(existingCart.getOptionInfo()).isEqualTo("일반 상품 옵션"); // 옵션 업데이트 확인
        verify(cartRepository).save(existingCart);
    }

    @Test
    @DisplayName("존재하지 않는 상품 추가 실패")
    void addToCart_ProductNotFound_ThrowException() {
        // Given
        given(productRepository.findByProductUuid(TEST_PRODUCT_UUID)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(testUser, normalCartRequestDto))
                .isInstanceOf(ServiceException.class)
                .hasMessage("PRODUCT_NOT_FOUND : 존재하지 않는 상품입니다.");

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("유효하지 않은 장바구니 타입으로 추가 실패")
    void addToCart_InvalidCartType_ThrowException() {
        // Given
        CartRequestDto invalidDto = new CartRequestDto(
                TEST_PRODUCT_UUID, // productUuid
                2,                 // quantity
                null,              // optionInfo
                "INVALID",         // cartType
                null,              // fundingId
                null,              // fundingPrice
                null               // fundingStock
        );

        given(productRepository.findByProductUuid(TEST_PRODUCT_UUID)).willReturn(Optional.of(testProduct));

        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(testUser, invalidDto))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 장바구니 타입입니다: INVALID");
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCartItems_Success() {
        // Given
        List<Cart> allCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(allCarts);
        
        // CartCalculator Mock 설정
        given(cartCalculator.calculateTotalQuantity(anyList())).willReturn(2, 1);
        given(cartCalculator.calculateTotalAmount(anyList())).willReturn(20000, 10000);

        // When
        CartListResponseDto result = cartService.getCartItems(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.normalCartItems()).hasSize(1);
        assertThat(result.fundingCartItems()).hasSize(1);
        assertThat(result.totalNormalQuantity()).isEqualTo(2);
        assertThat(result.totalFundingQuantity()).isEqualTo(1);
        assertThat(result.totalNormalAmount()).isEqualTo(20000); // 10000 * 2
        assertThat(result.totalFundingAmount()).isEqualTo(10000); // 10000 * 1

        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("빈 장바구니 조회")
    void getCartItems_EmptyCart() {
        // Given
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(Collections.emptyList());
        
        // CartCalculator Mock 설정 (빈 리스트일 때 0 반환)
        given(cartCalculator.calculateTotalQuantity(anyList())).willReturn(0);
        given(cartCalculator.calculateTotalAmount(anyList())).willReturn(0);

        // When
        CartListResponseDto result = cartService.getCartItems(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.normalCartItems()).isEmpty();
        assertThat(result.fundingCartItems()).isEmpty();
        assertThat(result.totalNormalQuantity()).isEqualTo(0);
        assertThat(result.totalFundingQuantity()).isEqualTo(0);
        assertThat(result.totalNormalAmount()).isEqualTo(0);
        assertThat(result.totalFundingAmount()).isEqualTo(0);

        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("장바구니 수량 수정 성공")
    void updateQuantity_Success() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        given(cartRepository.save(testNormalCart)).willReturn(testNormalCart);

        // When
        CartResponseDto result = cartService.updateQuantity(testUser, 1L, 5);

        // Then
        assertThat(testNormalCart.getQuantity()).isEqualTo(5);
        assertThat(result).isNotNull();
        verify(cartRepository).save(testNormalCart);
    }

    @Test
    @DisplayName("유효하지 않은 수량으로 수정 실패 - 0")
    void updateQuantity_InvalidQuantityZero_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        
        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(testUser, 1L, 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 수량으로 수정 실패 - null")
    void updateQuantity_InvalidQuantityNull_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        
        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(testUser, 1L, null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("권한 없는 사용자의 장바구니 수량 수정 실패")
    void updateQuantity_Unauthorized_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));

        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(anotherUser, 1L, 5))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 장바구니 아이템에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("존재하지 않는 장바구니 수량 수정 실패")
    void updateQuantity_NotFound_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(testUser, 1L, 5))
                .isInstanceOf(ServiceException.class)
                .hasMessage("CART_NOT_FOUND : 존재하지 않는 장바구니 아이템입니다.");
    }

    @Test
    @DisplayName("장바구니에서 상품 삭제 성공")
    void removeFromCart_Success() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        willDoNothing().given(cartRepository).delete(testNormalCart);

        // When
        cartService.removeFromCart(testUser, 1L);

        // Then
        verify(cartRepository).delete(testNormalCart);
    }

    @Test
    @DisplayName("존재하지 않는 장바구니 아이템 삭제 실패")
    void removeFromCart_NotFound_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> cartService.removeFromCart(testUser, 1L))
                .isInstanceOf(ServiceException.class)
                .hasMessage("CART_NOT_FOUND : 존재하지 않는 장바구니 아이템입니다.");
    }

    @Test
    @DisplayName("권한 없는 사용자의 장바구니 삭제 실패")
    void removeFromCart_Unauthorized_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));

        // When & Then
        assertThatThrownBy(() -> cartService.removeFromCart(anotherUser, 1L))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("해당 장바구니 아이템에 대한 권한이 없습니다.");
    }

    @Test
    @DisplayName("장바구니 전체 삭제 성공")
    void clearCart_Success() {
        // Given
        given(cartRepository.deleteByUser(testUser)).willReturn(3);

        // When
        cartService.clearCart(testUser);

        // Then
        verify(cartRepository).deleteByUser(testUser);
    }

    @Test
    @DisplayName("일반 타입 장바구니 삭제 성공")
    void clearCartByType_Normal_Success() {
        // Given
        given(cartRepository.deleteByUserAndCartType(testUser, Cart.CartType.NORMAL)).willReturn(2);

        // When
        cartService.clearCartByType(testUser, "NORMAL");

        // Then
        verify(cartRepository).deleteByUserAndCartType(testUser, Cart.CartType.NORMAL);
    }

    @Test
    @DisplayName("펀딩 타입 장바구니 삭제 성공")
    void clearCartByType_Funding_Success() {
        // Given
        given(cartRepository.deleteByUserAndCartType(testUser, Cart.CartType.FUNDING)).willReturn(1);

        // When
        cartService.clearCartByType(testUser, "FUNDING");

        // Then
        verify(cartRepository).deleteByUserAndCartType(testUser, Cart.CartType.FUNDING);
    }

    @Test
    @DisplayName("유효하지 않은 타입으로 장바구니 삭제 실패")
    void clearCartByType_InvalidType_ThrowException() {
        // When & Then
        assertThatThrownBy(() -> cartService.clearCartByType(testUser, "INVALID"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("유효하지 않은 장바구니 타입입니다: INVALID");
    }

    @Test
    @DisplayName("장바구니 선택 상태 토글 성공 - true to false")
    void toggleSelection_TrueToFalse_Success() {
        // Given
        testNormalCart.select();
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        given(cartRepository.save(testNormalCart)).willReturn(testNormalCart);

        // When
        CartResponseDto result = cartService.toggleSelection(testUser, 1L);

        // Then
        assertThat(testNormalCart.getIsSelected()).isFalse(); // true -> false로 변경
        verify(cartRepository).save(testNormalCart);
    }

    @Test
    @DisplayName("장바구니 선택 상태 토글 성공 - false to true")
    void toggleSelection_FalseToTrue_Success() {
        // Given
        testNormalCart.unselect();
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        given(cartRepository.save(testNormalCart)).willReturn(testNormalCart);

        // When
        CartResponseDto result = cartService.toggleSelection(testUser, 1L);

        // Then
        assertThat(testNormalCart.getIsSelected()).isTrue(); // false -> true로 변경
        verify(cartRepository).save(testNormalCart);
    }

    @Test
    @DisplayName("선택된 장바구니 아이템만 조회 성공 - 검증 없음")
    void getSelectedCartItems_WithoutValidation_Success() {
        // Given
        testNormalCart.select();
        testFundingCart.select();
        List<Cart> selectedCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserAndIsSelectedTrueWithProduct(testUser)).willReturn(selectedCarts);

        // When
        List<CartResponseDto> result = cartService.getSelectedCartItems(testUser, false);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).isSelected()).isTrue();
        assertThat(result.get(1).isSelected()).isTrue();
        verify(cartRepository).findByUserAndIsSelectedTrueWithProduct(testUser);
    }

    @Test
    @DisplayName("선택된 장바구니 아이템만 조회 성공 - 유효성 검증 포함")
    void getSelectedCartItems_WithValidation_Success() {
        // Given
        testNormalCart.select();
        testFundingCart.select();
        List<Cart> selectedCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserAndIsSelectedTrueWithProduct(testUser)).willReturn(selectedCarts);

        // When
        List<CartResponseDto> result = cartService.getSelectedCartItems(testUser, true);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).findByUserAndIsSelectedTrueWithProduct(testUser);
    }

    @Test
    @DisplayName("선택된 장바구니 아이템이 없을 때")
    void getSelectedCartItems_EmptyResult() {
        // Given
        given(cartRepository.findByUserAndIsSelectedTrueWithProduct(testUser)).willReturn(Collections.emptyList());

        // When
        List<CartResponseDto> result = cartService.getSelectedCartItems(testUser, false);

        // Then
        assertThat(result).isEmpty();
        verify(cartRepository).findByUserAndIsSelectedTrueWithProduct(testUser);
    }

    @Test
    @DisplayName("전체 장바구니 아이템 조회 성공 - 검증 없음")
    void getAllCartItems_WithoutValidation_Success() {
        // Given
        List<Cart> allCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(allCarts);

        // When
        List<CartResponseDto> result = cartService.getAllCartItems(testUser, false);

        // Then
        assertThat(result).hasSize(2);
        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("전체 장바구니 아이템 조회 성공 - 유효성 검증 포함")
    void getAllCartItems_WithValidation_Success() {
        // Given
        List<Cart> allCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(allCarts);

        // When
        List<CartResponseDto> result = cartService.getAllCartItems(testUser, true);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("주문 가능한 장바구니 검증 성공 - 전체주문")
    void validateCartItemsForOrder_FullOrder_Success() {
        // Given
        testNormalCart.select();
        List<Cart> allCarts = Arrays.asList(testNormalCart);
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(allCarts);

        // When & Then
        cartService.validateCartItemsForOrder(testUser, true);
        
        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("주문 가능한 장바구니 검증 성공 - 선택주문")
    void validateCartItemsForOrder_SelectedOrder_Success() {
        // Given
        testNormalCart.select();
        List<Cart> selectedCarts = Arrays.asList(testNormalCart);
        given(cartRepository.findByUserAndIsSelectedTrue(testUser)).willReturn(selectedCarts);

        // When & Then
        cartService.validateCartItemsForOrder(testUser, false);
        
        verify(cartRepository).findByUserAndIsSelectedTrue(testUser);
    }

    @Test
    @DisplayName("빈 장바구니 주문 검증 실패")
    void validateCartItemsForOrder_EmptyCart_ThrowException() {
        // Given
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(Collections.emptyList());

        // When & Then
        assertThatThrownBy(() -> cartService.validateCartItemsForOrder(testUser, true))
                .isInstanceOf(ServiceException.class)
                .hasMessage("CART_EMPTY : 주문할 장바구니 아이템이 없습니다.");
    }

    @Test
    @DisplayName("장바구니 총 금액 계산 성공 - 전체주문")
    void calculateTotalAmount_FullOrder_Success() {
        // Given
        List<Cart> allCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(allCarts);

        // When
        Integer result = cartService.calculateTotalAmount(testUser, true);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("장바구니 총 금액 계산 성공 - 선택주문")
    void calculateTotalAmount_SelectedOrder_Success() {
        // Given
        testNormalCart.select();
        List<Cart> selectedCarts = Arrays.asList(testNormalCart);
        given(cartRepository.findByUserAndIsSelectedTrue(testUser)).willReturn(selectedCarts);

        // When
        Integer result = cartService.calculateTotalAmount(testUser, false);

        // Then
        assertThat(result).isNotNull();
        verify(cartRepository).findByUserAndIsSelectedTrue(testUser);
    }

    @Test
    @DisplayName("펀딩 장바구니에 상품 추가 성공")
    void addToCart_FundingCart_Success() {
        // Given
        Cart fundingCartWithFields = Cart.builder()
                .user(testUser)
                .product(null)
                .funding(testFunding)
                .quantity(1)
                .cartType(Cart.CartType.FUNDING)
                .isSelected(true)
                .fundingId("2")
                .fundingPrice(50000)
                .fundingStock(100)
                .build();
        
        given(fundingRepository.findById(2L)).willReturn(Optional.of(testFunding));
        given(cartRepository.findByUserAndFundingAndCartType(eq(testUser), any(com.back.domain.funding.entity.Funding.class), eq(Cart.CartType.FUNDING)))
                .willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(fundingCartWithFields);

        // When
        CartResponseDto result = cartService.addToCart(testUser, fundingCartRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.cartType()).isEqualTo("FUNDING");
        assertThat(result.optionInfo()).isNull(); // 펀딩은 옵션 안씀
        assertThat(result.fundingId()).isEqualTo("2");
        assertThat(result.fundingPrice()).isEqualTo(50000);
        assertThat(result.fundingStock()).isEqualTo(100);

        verify(fundingRepository).findById(2L);
        verify(cartRepository).findByUserAndFundingAndCartType(eq(testUser), any(com.back.domain.funding.entity.Funding.class), eq(Cart.CartType.FUNDING));
        verify(cartRepository).save(any(Cart.class));
    }
}
