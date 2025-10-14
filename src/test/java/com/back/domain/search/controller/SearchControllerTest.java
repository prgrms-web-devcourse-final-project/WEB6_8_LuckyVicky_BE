package com.back.domain.search.controller;

import com.back.domain.search.dto.SearchResponseDto;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.search.dto.ArtistSearchResultDto;
import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.search.service.SearchService;
import com.fasterxml.jackson.databind.ObjectMapper;
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

import java.util.List;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@ActiveProfiles("test")
@DisplayName("SearchController 통합 테스트")
public class SearchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private SearchService searchService;

    @Nested
    @DisplayName("통합 검색")
    class IntegratedSearch {

        @Test
        @DisplayName("키워드로 통합 검색 성공")
        void search_Success() throws Exception {
            // Given
            String keyword = "테스트";
            SearchResponseDto mockResponse = createMockSearchResponseDto();
            given(searchService.search(anyString())).willReturn(mockResponse);

            // When
            ResultActions resultActions = mockMvc.perform(
                    get("/api/search")
                            .param("keyword", keyword)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("검색 성공"))
                    .andExpect(jsonPath("$.data.products", hasSize(1)))
                    .andExpect(jsonPath("$.data.products[0].name").value("테스트 상품"))
                    .andExpect(jsonPath("$.data.artists", hasSize(1)))
                    .andExpect(jsonPath("$.data.artists[0].artistName").value("테스트 작가"))
                    .andExpect(jsonPath("$.data.fundings", hasSize(1)))
                    .andExpect(jsonPath("$.data.fundings[0].title").value("테스트 펀딩"));
        }

        @Test
        @DisplayName("키워드 없이 검색 시 빈 결과 반환")
        void search_NoKeyword_ReturnsEmpty() throws Exception {
            // Given
            SearchResponseDto emptyResponse = new SearchResponseDto(List.of(), List.of(), List.of());
            given(searchService.search(anyString())).willReturn(emptyResponse);

            // When
            ResultActions resultActions = mockMvc.perform(
                    get("/api/search")
                            .param("keyword", "")
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("검색 성공"))
                    .andExpect(jsonPath("$.data.products", hasSize(0)))
                    .andExpect(jsonPath("$.data.artists", hasSize(0)))
                    .andExpect(jsonPath("$.data.fundings", hasSize(0)));
        }

        @Test
        @DisplayName("검색 결과가 없는 경우 빈 목록 반환")
        void search_NoResults_ReturnsEmpty() throws Exception {
            // Given
            String keyword = "없는키워드";
            SearchResponseDto emptyResponse = new SearchResponseDto(List.of(), List.of(), List.of());
            given(searchService.search(anyString())).willReturn(emptyResponse);

            // When
            ResultActions resultActions = mockMvc.perform(
                    get("/api/search")
                            .param("keyword", keyword)
                            .contentType(MediaType.APPLICATION_JSON)
            ).andDo(print());

            // Then
            resultActions
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.resultCode").value("200"))
                    .andExpect(jsonPath("$.msg").value("검색 성공"))
                    .andExpect(jsonPath("$.data.products", hasSize(0)))
                    .andExpect(jsonPath("$.data.artists", hasSize(0)))
                    .andExpect(jsonPath("$.data.fundings", hasSize(0)));
        }

        @Test
        @DisplayName("다양한 키워드로 검색 성공")
        void search_VariousKeywords_Success() throws Exception {
            // Given
            String[] keywords = {"상품", "작가", "펀딩", "도자기", "그림"};

            for (String keyword : keywords) {
                SearchResponseDto mockResponse = createMockSearchResponseDtoForKeyword(keyword);
                given(searchService.search(keyword)).willReturn(mockResponse);

                // When
                ResultActions resultActions = mockMvc.perform(
                        get("/api/search")
                                .param("keyword", keyword)
                                .contentType(MediaType.APPLICATION_JSON)
                ).andDo(print());

                // Then
                resultActions
                        .andExpect(status().isOk())
                        .andExpect(jsonPath("$.resultCode").value("200"))
                        .andExpect(jsonPath("$.msg").value("검색 성공"));
                // 추가적인 jsonPath 검증은 mockResponseForKeyword에 따라 달라짐
            }
        }
    }

    private SearchResponseDto createMockSearchResponseDto() {
        ProductListResponse.ProductInfo product = new ProductListResponse.ProductInfo(
                UUID.randomUUID(),
                "https://bucket.s3.amazonaws.com/product-images/uuid1.png",
                "브랜드1",
                "테스트 상품",
                10000,
                10,
                9000,
                4.6
        );
        ArtistSearchResultDto artist = new ArtistSearchResultDto(
                1L,
                "테스트 작가",
                "https://example.com/profile.jpg"
        );
        FundingCardDto funding = new FundingCardDto(
                101L,
                "테스트 펀딩",
                "https://bucket.s3.amazonaws.com/funding-images/uuid1.png",
                "스티커",
                "김작가",
                3000,
                600000L,
                500000L,
                50.0,
                10
        );
        return new SearchResponseDto(List.of(product), List.of(artist), List.of(funding));
    }

    private SearchResponseDto createMockSearchResponseDtoForKeyword(String keyword) {
        ProductListResponse.ProductInfo product = new ProductListResponse.ProductInfo(
                UUID.randomUUID(),
                "https://bucket.s3.amazonaws.com/product-images/uuid1.png",
                "브랜드1",
                keyword + " 상품",
                10000,
                10,
                9000,
                4.6
        );
        ArtistSearchResultDto artist = new ArtistSearchResultDto(
                1L,
                keyword + " 작가",
                "https://example.com/profile.jpg"
        );
        FundingCardDto funding = new FundingCardDto(
                101L,
                keyword + " 펀딩",
                "https://bucket.s3.amazonaws.com/funding-images/uuid1.png",
                "스티커",
                "김작가",
                3000,
                600000L,
                500000L,
                50.0,
                10
        );
        return new SearchResponseDto(List.of(product), List.of(artist), List.of(funding));
    }
}