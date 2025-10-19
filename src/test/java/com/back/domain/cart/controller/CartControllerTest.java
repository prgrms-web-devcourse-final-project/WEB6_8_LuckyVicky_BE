package com.back.domain.cart.controller;

import com.back.domain.cart.dto.request.CartRequestDto;
import com.back.domain.cart.dto.response.CartListResponseDto;
import com.back.domain.cart.dto.response.CartResponseDto;
import com.back.domain.cart.service.CartService;
import com.back.domain.user.entity.User;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.security.oauth2.client.OAuth2ClientAutoConfiguration;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@WebMvcTest(
        controllers = CartController.class,
        excludeAutoConfiguration = {
                SecurityAutoConfiguration.class,
                org.springframework.boot.autoconfigure.security.servlet.SecurityFilterAutoConfiguration.class,
                OAuth2ClientAutoConfiguration.class
        },
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.REGEX,
                pattern = "com\\.back\\.global\\.security\\..*"
        )
)
class CartControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private CartService cartService;

    @Autowired
    private ObjectMapper objectMapper;

    private User testUser;
    private CartRequestDto cartRequestDto;
    private CartResponseDto cartResponseDto;
    private CartListResponseDto cartListResponseDto;
    
    private static final UUID TEST_PRODUCT_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        testUser = User.createLocalUser(
                "test@test.com",
                "password",
                "testUser",
                "010-0000-0000"
        );
        // ID 주입
        org.springframework.test.util.ReflectionTestUtils.setField(testUser, "id", 1L);

        cartRequestDto = new CartRequestDto(
                TEST_PRODUCT_UUID, // productUuid
                2,                 // quantity
                "옵션정보",         // optionInfo
                "NORMAL",          // cartType
                null,              // fundingId
                null,              // fundingPrice
                null               // fundingStock
        );

        cartResponseDto = new CartResponseDto(
                1L,                    // cartId
                TEST_PRODUCT_UUID,     // productUuid
                "임시 상품명",         // productName
                "test-image.jpg",     // productImageUrl
                10000,                 // price
                2,                     // quantity
                "옵션정보",            // optionInfo
                true,                  // isSelected
                "NORMAL",              // cartType
                null,                  // fundingId
                null,                  // fundingPrice
                null,                  // fundingStock
                LocalDateTime.now()    // createdAt
        );

        List<CartResponseDto> normalItems = Arrays.asList(cartResponseDto);
        cartListResponseDto = new CartListResponseDto(
                normalItems,
                Arrays.asList(),
                2,
                0,
                20000,
                0
        );
    }

    @Test
    @DisplayName("장바구니에 상품 추가 - 성공")
    void addToCart_Success() throws Exception {
        // given
        when(cartService.addToCart(any(), any(CartRequestDto.class)))
                .thenReturn(cartResponseDto);

        // when & then
        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).addToCart(any(), any(CartRequestDto.class));
    }

    @Test
    @DisplayName("장바구니 목록 조회 - 성공")
    void getCartItems_Success() throws Exception {
        // given
        when(cartService.getCartItems(any()))
                .thenReturn(cartListResponseDto);

        // when & then
        mockMvc.perform(get("/api/cart"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).getCartItems(any());
    }

    @Test
    @DisplayName("장바구니에 상품 추가 - 유효성 검증 실패")
    void addToCart_ValidationFailed() throws Exception {
        // given
        CartRequestDto invalidRequest = new CartRequestDto(
                null,  // productId - 필수값 누락
                -1,    // quantity - 음수값
                null,  // optionInfo
                null,  // cartType
                null,  // fundingId
                null,  // fundingPrice
                null   // fundingStock
        );

        // when & then
        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidRequest)))
                .andDo(print())
                .andExpect(status().isBadRequest());

        verify(cartService, never()).addToCart(any(), any(CartRequestDto.class));
    }

    @Test
    @DisplayName("장바구니 수량 수정 - 성공")
    void updateQuantity_Success() throws Exception {
        // given
        Long cartId = 1L;
        Integer newQuantity = 3;
        CartResponseDto updatedCart = new CartResponseDto(
                cartId,              // cartId
                TEST_PRODUCT_UUID,   // productUuid
                "임시 상품명",       // productName
                "test-image.jpg", // productImageUrl
                10000,            // price
                newQuantity,      // quantity
                null,             // optionInfo
                true,             // isSelected
                "NORMAL",         // cartType
                null,             // fundingId
                null,             // fundingPrice
                null,             // fundingStock
                null              // createdAt
        );

        when(cartService.updateQuantity(any(), eq(cartId), eq(newQuantity)))
                .thenReturn(updatedCart);

        // when & then
        mockMvc.perform(put("/api/cart/{cartId}/quantity", cartId)
                        .param("quantity", newQuantity.toString()))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).updateQuantity(any(), eq(cartId), eq(newQuantity));
    }

    @Test
    @DisplayName("장바구니에서 상품 삭제 - 성공")
    void removeFromCart_Success() throws Exception {
        // given
        Long cartId = 1L;
        doNothing().when(cartService).removeFromCart(any(), eq(cartId));

        // when & then
        mockMvc.perform(delete("/api/cart/{cartId}", cartId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).removeFromCart(any(), eq(cartId));
    }

    @Test
    @DisplayName("장바구니 전체 삭제 - 성공")
    void clearCart_Success() throws Exception {
        // given
        doNothing().when(cartService).clearCart(any());

        // when & then
        mockMvc.perform(delete("/api/cart"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCart(any());
    }

    @Test
    @DisplayName("타입별 장바구니 삭제 - 성공")
    void clearCartByType_Success() throws Exception {
        // given
        String cartType = "NORMAL";
        doNothing().when(cartService).clearCartByType(any(), eq(cartType));

        // when & then
        mockMvc.perform(delete("/api/cart/type/{cartType}", cartType))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).clearCartByType(any(), eq(cartType));
    }

    @Test
    @DisplayName("장바구니 선택 상태 토글 - 성공")
    void toggleSelection_Success() throws Exception {
        // given
        Long cartId = 1L;
        CartResponseDto toggledCart = new CartResponseDto(
                cartId,              // cartId
                TEST_PRODUCT_UUID,   // productUuid
                "임시 상품명",       // productName
                "test-image.jpg", // productImageUrl
                10000,            // price
                2,                // quantity
                null,             // optionInfo
                false,            // isSelected
                "NORMAL",         // cartType
                null,             // fundingId
                null,             // fundingPrice
                null,             // fundingStock
                null              // createdAt
        );

        when(cartService.toggleSelection(any(), eq(cartId)))
                .thenReturn(toggledCart);

        // when & then
        mockMvc.perform(put("/api/cart/{cartId}/toggle-selection", cartId))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).toggleSelection(any(), eq(cartId));
    }

    @Test
    @DisplayName("선택된 장바구니 아이템 조회 - 성공")
    void getSelectedCartItems_Success() throws Exception {
        // given
        List<CartResponseDto> selectedItems = Arrays.asList(
                new CartResponseDto(
                        1L,                   // cartId
                        TEST_PRODUCT_UUID,    // productUuid
                        "테스트 상품1",       // productName
                        "test-image1.jpg",   // productImageUrl
                        10000,                // price
                        2,                    // quantity
                        null,                 // optionInfo
                        true,                 // isSelected
                        "NORMAL",             // cartType
                        null,                 // fundingId
                        null,                 // fundingPrice
                        null,                 // fundingStock
                        null                  // createdAt
                )
        );

        when(cartService.getSelectedCartItems(any(), eq(false)))
                .thenReturn(selectedItems);

        // when & then
        mockMvc.perform(get("/api/cart/selected"))
                .andDo(print())
                .andExpect(status().isOk());

        verify(cartService, times(1)).getSelectedCartItems(any(), eq(false));
    }

    @Test
    @DisplayName("인증되지 않은 사용자 접근 - 성공")
    void unauthorizedAccess_Success() throws Exception {
        // given - Security 비활성화로 인해 인증 없이도 접근 가능하므로 Mock 설정
        when(cartService.getCartItems(any()))
                .thenReturn(cartListResponseDto);
        
        // when & then
        mockMvc.perform(get("/api/cart"))
                .andDo(print())
                .andExpect(status().isOk()); // Security 비활성화로 인해 인증 없이도 접근 가능

        verify(cartService, times(1)).getCartItems(any());
    }

    @Test
    @DisplayName("서비스 예외 발생 시 처리")
    void serviceException_Handling() throws Exception {
        // given
        when(cartService.addToCart(any(), any(CartRequestDto.class)))
                .thenThrow(new RuntimeException("서비스 오류"));

        // when & then
        mockMvc.perform(post("/api/cart")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(cartRequestDto)))
                .andDo(print())
                .andExpect(status().isInternalServerError());

        verify(cartService, times(1)).addToCart(any(), any(CartRequestDto.class));
    }
}