package com.back.domain.review.controller;

import com.back.domain.review.dto.request.ReviewCreateRequestDto;
import com.back.domain.review.dto.request.ReviewWriteRequestDto;
import com.back.domain.review.dto.response.ReviewDetailResponseDto;
import com.back.domain.review.dto.response.ReviewResponseDto;
import com.back.domain.review.service.ReviewService;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.product.product.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.security.core.Authentication;
import org.springframework.test.util.ReflectionTestUtils;
import java.time.LocalDateTime;
import java.util.Arrays;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 Controller 테스트")
class ReviewControllerTest {

    @Mock
    private ReviewService reviewService;

    @Mock
    private ProductRepository productRepository;
    
    @Mock
    private com.back.global.s3.S3Service s3Service;

    @InjectMocks
    private ReviewController reviewController;

    private MockMvc mockMvc;
    private ObjectMapper objectMapper;
    private User user;
    
    private static final UUID TEST_PRODUCT_UUID = UUID.fromString("550e8400-e29b-41d4-a716-446655440000");

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(reviewController).build();
        objectMapper = new ObjectMapper();
        user = createTestUser();
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    void createReview_Success() throws Exception {
        // Given
        ReviewCreateRequestDto requestDto = ReviewCreateRequestDto.builder()
                .productUuid(TEST_PRODUCT_UUID)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .images(Arrays.asList())
                .build();

        ReviewResponseDto responseDto = ReviewResponseDto.builder()
                .reviewId(1L)
                .productUuid(TEST_PRODUCT_UUID)
                .productName("테스트 상품")
                .userId(user.getId())
                .userName(user.getName())
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .likeCount(0)
                .isPhotoReview(true)
                .isLiked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.createReview(any(ReviewCreateRequestDto.class), any(User.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.productUuid").value(TEST_PRODUCT_UUID.toString()))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("정말 좋은 상품입니다!"));
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공")
    void getReviewList_Success() throws Exception {
        // When & Then
        mockMvc.perform(get("/api/reviews")
                        .param("productUuid", TEST_PRODUCT_UUID.toString())
                        .param("reviewType", "ALL")
                        .param("page", "0")
                        .param("size", "10")
                        .requestAttr("user", user))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 상세 조회 성공")
    void getReviewDetail_Success() throws Exception {
        // Given
        ReviewResponseDto responseDto = ReviewResponseDto.builder()
                .reviewId(1L)
                .productUuid(TEST_PRODUCT_UUID)
                .productName("테스트 상품")
                .userId(user.getId())
                .userName(user.getName())
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .likeCount(0)
                .isPhotoReview(false)
                .isLiked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewDetail(eq(1L), any(User.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/reviews/1")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("정말 좋은 상품입니다!"));
    }

    @Test
    @DisplayName("리뷰 좋아요 토글 성공")
    void toggleReviewLike_Success() throws Exception {
        // Given
        when(reviewService.toggleReviewLike(eq(1L), any(User.class))).thenReturn(true);

        // When & Then
        mockMvc.perform(post("/api/reviews/1/like")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("리뷰 상세 팝업 조회 성공")
    void getReviewDetailPopup_Success() throws Exception {
        // Given
        ReviewDetailResponseDto responseDto = ReviewDetailResponseDto.builder()
                .reviewId(1L)
                .productUuid(TEST_PRODUCT_UUID)
                .productName("테스트 상품")
                .productOption("상품옵션1")
                .userId(user.getId())
                .userName(user.getName())
                .rating(5)
                .content("너무너무예뻐요......")
                .likeCount(10)
                .isPhotoReview(true)
                .isLiked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewDetailForPopup(eq(1L), any(User.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/reviews/1/popup")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.productOption").value("상품옵션1"))
                .andExpect(jsonPath("$.content").value("너무너무예뻐요......"));
    }

    @Test
    @DisplayName("리뷰 작성 팝업 성공")
    void writeReviewFromPopup_Success() throws Exception {
        // Given
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productUuid(TEST_PRODUCT_UUID)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .images(Arrays.asList())
                .hashtags(Arrays.asList("#예쁨", "#만족", "#추천"))
                .productOption("상품옵션1")
                .isPhotoReview(true)
                .build();

        ReviewDetailResponseDto responseDto = ReviewDetailResponseDto.builder()
                .reviewId(1L)
                .productUuid(TEST_PRODUCT_UUID)
                .productName("테스트 상품")
                .productOption("상품옵션1")
                .userId(user.getId())
                .userName(user.getName())
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .likeCount(0)
                .isPhotoReview(true)
                .isLiked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.writeReviewFromPopup(any(ReviewWriteRequestDto.class), any(User.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/reviews/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.productOption").value("상품옵션1"))
                .andExpect(jsonPath("$.isPhotoReview").value(true));
    }

    @Test
    @DisplayName("상품별 리뷰 통계 조회 성공")
    void getReviewStats_Success() throws Exception {
        // Given
        com.back.domain.product.product.entity.Product product = mock(com.back.domain.product.product.entity.Product.class);
        when(productRepository.findByProductUuid(TEST_PRODUCT_UUID)).thenReturn(java.util.Optional.of(product));
        
        com.back.domain.review.dto.response.ReviewStatsResponseDto statsDto = 
            com.back.domain.review.dto.response.ReviewStatsResponseDto.builder()
                .totalReviewCount(10)
                .photoReviewCount(3)
                .generalReviewCount(7)
                .averageRating(4.5)
                .ratingDistribution(java.util.Map.of(5, 6, 4, 3, 3, 1))
                .build();
        
        when(reviewService.getReviewStats(any(com.back.domain.product.product.entity.Product.class)))
            .thenReturn(statsDto);
        
        // When & Then
        mockMvc.perform(get("/api/reviews/stats")
                        .param("productUuid", TEST_PRODUCT_UUID.toString()))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("리뷰 작성 팝업 성공 - 피그마 디자인 기반 (별점, 텍스트, 이미지, 해시태그, 상품옵션)")
    void writeReviewFromPopup_FigmaDesign_Success() throws Exception {
        // Given - 피그마 디자인의 모든 요소 포함
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productUuid(TEST_PRODUCT_UUID)
                .rating(5)
                .content("정말 좋은 상품입니다! 추천해요!")
                .images(Arrays.asList())
                .hashtags(Arrays.asList("좋아요", "추천", "만족"))
                .productOption("상품옵션1") // 피그마 디자인: 상품옵션1 표시
                .reviewType("PHOTO")
                .build();

        ReviewDetailResponseDto responseDto = ReviewDetailResponseDto.builder()
                .reviewId(1L)
                .productUuid(TEST_PRODUCT_UUID)
                .productName("테스트 상품")
                .productOption("상품옵션1")
                .userId(user.getId())
                .userName(user.getName())
                .rating(5)
                .content("정말 좋은 상품입니다! 추천해요!")
                .likeCount(0)
                .isPhotoReview(true)
                .isLiked(false)
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.writeReviewFromPopup(any(ReviewWriteRequestDto.class), any(User.class)))
                .thenReturn(responseDto);

        // When & Then
        mockMvc.perform(post("/api/reviews/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.content").value("정말 좋은 상품입니다! 추천해요!"))
                .andExpect(jsonPath("$.productOption").value("상품옵션1"))
                .andExpect(jsonPath("$.isPhotoReview").value(true));
    }

    @Test
    @DisplayName("리뷰 작성 팝업 실패 - 필수 필드 누락")
    void writeReviewFromPopup_ValidationError() throws Exception {
        // Given - 필수 필드 누락된 요청
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productUuid(null) // 필수 필드 누락
                .rating(5)
                .content("") // 빈 내용
                .productOption("") // 빈 상품옵션
                .build();

        // When & Then
        mockMvc.perform(post("/api/reviews/write")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestDto))
                        .requestAttr("user", user))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("리뷰 상세 팝업 조회 성공 - 피그마 디자인 기반")
    void getReviewDetailPopup_FigmaDesign_Success() throws Exception {
        // Given - 피그마 디자인 팝업 응답
        ReviewDetailResponseDto responseDto = ReviewDetailResponseDto.builder()
                .reviewId(1L)
                .productUuid(TEST_PRODUCT_UUID)
                .productName("테스트 상품")
                .productOption("상품옵션1")
                .userId(user.getId())
                .userName(user.getName())
                .rating(5)
                .content("너무너무예뻐요......")
                .likeCount(10)
                .isPhotoReview(true)
                .isLiked(false)
                .images(Arrays.asList())
                .hashtags(Arrays.asList("예쁨", "만족"))
                .createdAt(LocalDateTime.now())
                .build();

        when(reviewService.getReviewDetailForPopup(eq(1L), any(User.class))).thenReturn(responseDto);

        // When & Then
        mockMvc.perform(get("/api/reviews/1/popup")
                        .requestAttr("user", user))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.reviewId").value(1L))
                .andExpect(jsonPath("$.productOption").value("상품옵션1"))
                .andExpect(jsonPath("$.content").value("너무너무예뻐요......"))
                .andExpect(jsonPath("$.rating").value(5))
                .andExpect(jsonPath("$.likeCount").value(10))
                .andExpect(jsonPath("$.isPhotoReview").value(true));
    }

    // Helper Methods
    private User createTestUser() {
        User user = User.createLocalUser(
                "test@example.com",
                "password123",
                "테스트유저",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(user, "id", 1L);
        ReflectionTestUtils.setField(user, "role", Role.USER);
        return user;
    }
}
