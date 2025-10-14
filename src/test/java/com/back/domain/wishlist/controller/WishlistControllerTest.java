package com.back.domain.wishlist.controller;

import com.back.domain.wishlist.service.WishlistService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
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

    private final UUID productUuid = UUID.randomUUID();

    @Nested
    @DisplayName("찜 등록")
    class AddWishlist {

        @Test
        @WithMockUser
        @DisplayName("찜 등록 성공")
        void addWishlist_Success() throws Exception {
            // Given
            given(wishlistService.addWishlist(any(), any())).willReturn(productUuid);

            // When
            ResultActions resultActions = mockMvc.perform(
                    post("/api/wishlist/{productUuid}", productUuid)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("상품이 위시리스트에 추가되었습니다."))
                    .andExpect(jsonPath("$.data").value(productUuid.toString()));
        }
    }

    @Nested
    @DisplayName("찜 삭제")
    class RemoveWishlist {

        @Test
        @WithMockUser
        @DisplayName("찜 삭제 성공")
        void removeWishlist_Success() throws Exception {
            // Given
            given(wishlistService.removeWishlist(any(), any())).willReturn(productUuid);

            // When
            ResultActions resultActions = mockMvc.perform(
                    delete("/api/wishlist/{productUuid}", productUuid)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("상품이 위시리스트에서 제거되었습니다."))
                    .andExpect(jsonPath("$.data").value(productUuid.toString()));
        }
    }

    @Nested
    @DisplayName("상품별 찜 개수 조회")
    class GetWishlistCount {

        @Test
        @DisplayName("상품별 찜 개수 조회 성공")
        void getWishlistCount_Success() throws Exception {
            // Given
            long count = 10L;
            given(wishlistService.getWishlistCount(any())).willReturn(count);

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
    }
}