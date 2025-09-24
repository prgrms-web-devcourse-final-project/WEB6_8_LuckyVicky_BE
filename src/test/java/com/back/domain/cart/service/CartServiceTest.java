package com.back.domain.cart.service;

import com.back.domain.cart.dto.request.CartRequestDto;
import com.back.domain.cart.dto.response.CartListResponseDto;
import com.back.domain.cart.dto.response.CartResponseDto;
import com.back.domain.cart.entity.Cart;
import com.back.domain.cart.repository.CartRepository;
import com.back.domain.product.product.entity.Product;
// import com.back.domain.product.product.repository.ProductRepository;
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

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("장바구니 서비스 테스트")
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;
    
    // @Mock
    // private ProductRepository productRepository;
    
    @InjectMocks
    private CartService cartService;

    private User testUser;
    private User anotherUser;
    private Product testProduct;
    private Cart testNormalCart;
    private Cart testFundingCart;
    private CartRequestDto normalCartRequestDto;
    private CartRequestDto fundingCartRequestDto;

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
        when(testProduct.getName()).thenReturn("테스트 상품");
        when(testProduct.getPrice()).thenReturn(10000);

        // 테스트용 장바구니 생성 (일반) - id는 BaseEntity에서 자동 생성되므로 Builder에서 설정 불가
        testNormalCart = Cart.builder()
                .user(testUser)
                .product(testProduct)
                .quantity(2)
                .cartType(Cart.CartType.NORMAL)
                .isSelected(true)
                .optionInfo("일반 상품 옵션")
                .build();

        // 테스트용 장바구니 생성 (펀딩) - id는 BaseEntity에서 자동 생성되므로 Builder에서 설정 불가
        testFundingCart = Cart.builder()
                .user(testUser)
                .product(testProduct)
                .quantity(1)
                .cartType(Cart.CartType.FUNDING)
                .isSelected(true)
                .optionInfo("펀딩 상품 옵션")
                .build();

        // 테스트용 요청 DTO 생성 (일반)
        normalCartRequestDto = CartRequestDto.builder()
                .productId(1L)
                .quantity(2)
                .cartType("NORMAL")
                .optionInfo("일반 상품 옵션")
                .build();

        // 테스트용 요청 DTO 생성 (펀딩)
        fundingCartRequestDto = CartRequestDto.builder()
                .productId(1L)
                .quantity(1)
                .cartType("FUNDING")
                .optionInfo("펀딩 상품 옵션")
                .build();
    }

    @Test
    @DisplayName("장바구니에 새 상품 추가 성공")
    void addToCart_NewProduct_Success() {
        // Given
        // TODO: ProductRepository 구현 후 활성화
        // given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
        given(cartRepository.findByUserAndProductAndCartType(eq(testUser), any(Product.class), eq(Cart.CartType.NORMAL)))
                .willReturn(Optional.empty());
        given(cartRepository.save(any(Cart.class))).willReturn(testNormalCart);

        // When
        CartResponseDto result = cartService.addToCart(testUser, normalCartRequestDto);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getQuantity()).isEqualTo(2);
        assertThat(result.getCartType()).isEqualTo("NORMAL");
        assertThat(result.getOptionInfo()).isEqualTo("일반 상품 옵션");

        // verify(productRepository).findById(1L); // ProductRepository 구현 후 활성화
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

        // given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));
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
        // TODO: ProductRepository 구현 후 활성화
        // given(productRepository.findById(1L)).willReturn(Optional.empty());

        // When & Then
        // assertThatThrownBy(() -> cartService.addToCart(testUser, normalCartRequestDto))
        //         .isInstanceOf(ServiceException.class)
        //         .hasMessage("PRODUCT_NOT_FOUND : 존재하지 않는 상품입니다.");

        // verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    @DisplayName("유효하지 않은 장바구니 타입으로 추가 실패")
    void addToCart_InvalidCartType_ThrowException() {
        // Given
        CartRequestDto invalidDto = CartRequestDto.builder()
                .productId(1L)
                .quantity(2)
                .cartType("INVALID")
                .build();

        // given(productRepository.findById(1L)).willReturn(Optional.of(testProduct));

        // When & Then
        assertThatThrownBy(() -> cartService.addToCart(testUser, invalidDto))
                .isInstanceOf(ServiceException.class)
                .hasMessage("INVALID_CART_TYPE : 유효하지 않은 장바구니 타입입니다: INVALID");
    }

    @Test
    @DisplayName("장바구니 목록 조회 성공")
    void getCartItems_Success() {
        // Given
        List<Cart> allCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(allCarts);

        // When
        CartListResponseDto result = cartService.getCartItems(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNormalCartItems()).hasSize(1);
        assertThat(result.getFundingCartItems()).hasSize(1);
        assertThat(result.getTotalNormalQuantity()).isEqualTo(2);
        assertThat(result.getTotalFundingQuantity()).isEqualTo(1);
        assertThat(result.getTotalNormalAmount()).isEqualTo(20000); // 10000 * 2
        assertThat(result.getTotalFundingAmount()).isEqualTo(10000); // 10000 * 1

        verify(cartRepository).findByUserWithProduct(testUser);
    }

    @Test
    @DisplayName("빈 장바구니 조회")
    void getCartItems_EmptyCart() {
        // Given
        given(cartRepository.findByUserWithProduct(testUser)).willReturn(Collections.emptyList());

        // When
        CartListResponseDto result = cartService.getCartItems(testUser);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getNormalCartItems()).isEmpty();
        assertThat(result.getFundingCartItems()).isEmpty();
        assertThat(result.getTotalNormalQuantity()).isEqualTo(0);
        assertThat(result.getTotalFundingQuantity()).isEqualTo(0);
        assertThat(result.getTotalNormalAmount()).isEqualTo(0);
        assertThat(result.getTotalFundingAmount()).isEqualTo(0);

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
        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(testUser, 1L, 0))
                .isInstanceOf(ServiceException.class)
                .hasMessage("INVALID_QUANTITY : 수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("유효하지 않은 수량으로 수정 실패 - null")
    void updateQuantity_InvalidQuantityNull_ThrowException() {
        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(testUser, 1L, null))
                .isInstanceOf(ServiceException.class)
                .hasMessage("INVALID_QUANTITY : 수량은 1개 이상이어야 합니다.");
    }

    @Test
    @DisplayName("권한 없는 사용자의 장바구니 수량 수정 실패")
    void updateQuantity_Unauthorized_ThrowException() {
        // Given
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));

        // When & Then
        assertThatThrownBy(() -> cartService.updateQuantity(anotherUser, 1L, 5))
                .isInstanceOf(ServiceException.class)
                .hasMessage("UNAUTHORIZED : 권한이 없습니다.");
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
                .isInstanceOf(ServiceException.class)
                .hasMessage("UNAUTHORIZED : 권한이 없습니다.");
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
                .isInstanceOf(ServiceException.class)
                .hasMessage("INVALID_CART_TYPE : 유효하지 않은 장바구니 타입입니다: INVALID");
    }

    @Test
    @DisplayName("장바구니 선택 상태 토글 성공 - true to false")
    void toggleSelection_TrueToFalse_Success() {
        // Given
        testNormalCart.setIsSelected(true);
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
        testNormalCart.setIsSelected(false);
        given(cartRepository.findById(1L)).willReturn(Optional.of(testNormalCart));
        given(cartRepository.save(testNormalCart)).willReturn(testNormalCart);

        // When
        CartResponseDto result = cartService.toggleSelection(testUser, 1L);

        // Then
        assertThat(testNormalCart.getIsSelected()).isTrue(); // false -> true로 변경
        verify(cartRepository).save(testNormalCart);
    }

    @Test
    @DisplayName("선택된 장바구니 아이템만 조회 성공")
    void getSelectedCartItems_Success() {
        // Given
        testNormalCart.setIsSelected(true);
        testFundingCart.setIsSelected(true);
        List<Cart> selectedCarts = Arrays.asList(testNormalCart, testFundingCart);
        given(cartRepository.findByUserAndIsSelectedTrue(testUser)).willReturn(selectedCarts);

        // When
        List<CartResponseDto> result = cartService.getSelectedCartItems(testUser);

        // Then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getIsSelected()).isTrue();
        assertThat(result.get(1).getIsSelected()).isTrue();
        verify(cartRepository).findByUserAndIsSelectedTrue(testUser);
    }

    @Test
    @DisplayName("선택된 장바구니 아이템이 없을 때")
    void getSelectedCartItems_EmptyResult() {
        // Given
        given(cartRepository.findByUserAndIsSelectedTrue(testUser)).willReturn(Collections.emptyList());

        // When
        List<CartResponseDto> result = cartService.getSelectedCartItems(testUser);

        // Then
        assertThat(result).isEmpty();
        verify(cartRepository).findByUserAndIsSelectedTrue(testUser);
    }
}
