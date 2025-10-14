package com.back.domain.wishlist.controller;

import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.domain.wishlist.service.WishlistService;
import com.back.global.exception.ServiceException;
import com.back.global.security.auth.CustomUserDetails;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.doThrow;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("WishlistController 통합 테스트")
public class WishlistControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private WishlistService wishlistService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ProductRepository productRepository;

    private User testUser;
    private Product testProduct;
    private CustomUserDetails customUserDetails;

    @BeforeEach
    void setUp() {
        testUser = User.createLocalUser("test@example.com", "password", "TestUser", "010-1234-5678");
        testUser = userRepository.save(testUser); // Save to get an ID

        testProduct = Product.builder()
                .productUuid(UUID.randomUUID())
                .name("Test Product")
                .user(testUser) // 상품 등록자
                .build();
        productRepository.save(testProduct);

        customUserDetails = new CustomUserDetails(testUser, testUser.getRole());
    }

    @Nested
    @DisplayName("찜 등록")
    class AddWishlist {

        @Test
        @DisplayName("성공적으로 찜을 등록한다")
        void addWishlist_Success() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();
            given(wishlistService.addWishlist(any(UUID.class), any(CustomUserDetails.class)))
                    .willReturn(productUuid);

            // When
            ResultActions resultActions = mockMvc.perform(
                    post("/api/wishlist/{productUuid}", productUuid)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("상품이 위시리스트에 추가되었습니다."))
                    .andExpect(jsonPath("$.data").value(productUuid.toString()));
        }

        @Test
        @DisplayName("이미 찜한 상품인 경우 409 Conflict 에러를 반환한다")
        void addWishlist_Fail_AlreadyExists() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();
            doThrow(new ServiceException("409", "이미 위시리스트에 추가된 상품입니다."))
                    .when(wishlistService).addWishlist(any(UUID.class), any(CustomUserDetails.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    post("/api/wishlist/{productUuid}", productUuid)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isConflict())
                    .andExpect(jsonPath("$.resultCode").value("409"))
                    .andExpect(jsonPath("$.msg").value("이미 위시리스트에 추가된 상품입니다."));
        }

        @Test
        @DisplayName("존재하지 않는 상품인 경우 404 Not Found 에러를 반환한다")
        void addWishlist_Fail_ProductNotFound() throws Exception {
            // Given
            UUID nonExistentProductUuid = UUID.randomUUID();
            doThrow(new ServiceException("404", "존재하지 않는 상품입니다. UUID: " + nonExistentProductUuid))
                    .when(wishlistService).addWishlist(any(UUID.class), any(CustomUserDetails.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    post("/api/wishlist/{productUuid}", nonExistentProductUuid)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("404"))
                    .andExpect(jsonPath("$.msg").value("존재하지 않는 상품입니다. UUID: " + nonExistentProductUuid));
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 찜 등록 시도 시 401 Unauthorized 에러를 반환한다")
        void addWishlist_Fail_Unauthorized() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();

            // When
            ResultActions resultActions = mockMvc.perform(
                    post("/api/wishlist/{productUuid}", productUuid)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("찜 삭제")
    class RemoveWishlist {

        @Test
        @DisplayName("성공적으로 찜을 삭제한다")
        void removeWishlist_Success() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();
            given(wishlistService.removeWishlist(any(UUID.class), any(CustomUserDetails.class)))
                    .willReturn(productUuid);

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/wishlist/{productUuid}", productUuid)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("상품이 위시리스트에서 제거되었습니다."))
                    .andExpect(jsonPath("$.data").value(productUuid.toString()));
        }

        @Test
        @DisplayName("위시리스트 항목을 찾을 수 없는 경우 404 Not Found 에러를 반환한다")
        void removeWishlist_Fail_NotFound() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();
            doThrow(new ServiceException("404", "위시리스트 항목을 찾을 수 없습니다."))
                    .when(wishlistService).removeWishlist(any(UUID.class), any(CustomUserDetails.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/wishlist/{productUuid}", productUuid)
                            .with(user(customUserDetails))
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("404"))
                    .andExpect(jsonPath("$.msg").value("위시리스트 항목을 찾을 수 없습니다."));
        }

        @Test
        @DisplayName("인증되지 않은 사용자가 찜 삭제 시도 시 401 Unauthorized 에러를 반환한다")
        void removeWishlist_Fail_Unauthorized() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/wishlist/{productUuid}", productUuid)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isUnauthorized());
        }
    }

    @Nested
    @DisplayName("상품별 찜 개수 조회")
    class GetWishlistCount {

        @Test
        @DisplayName("성공적으로 상품별 찜 개수를 조회한다")
        void getWishlistCount_Success() throws Exception {
            // Given
            UUID productUuid = testProduct.getProductUuid();
            Long count = 5L;
            given(wishlistService.getWishlistCount(any(UUID.class)))
                    .willReturn(count);

            // When
            ResultActions resultActions = mockMvc.perform(
                    get("/api/wishlist/{productUuid}/count", productUuid)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("상품 찜 개수 조회 성공"))
                    .andExpect(jsonPath("$.data").value(count));
        }

        @Test
        @DisplayName("존재하지 않는 상품의 찜 개수 조회 시 404 Not Found 에러를 반환한다")
        void getWishlistCount_Fail_ProductNotFound() throws Exception {
            // Given
            UUID nonExistentProductUuid = UUID.randomUUID();
            doThrow(new ServiceException("404", "존재하지 않는 상품입니다. UUID: " + nonExistentProductUuid))
                    .when(wishlistService).getWishlistCount(any(UUID.class));

            // When
            ResultActions resultActions = mockMvc.perform(
                    get("/api/wishlist/{productUuid}/count", nonExistentProductUuid)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.resultCode").value("404"))
                    .andExpect(jsonPath("$.msg").value("존재하지 않는 상품입니다. UUID: " + nonExistentProductUuid));
        }
    }
}
