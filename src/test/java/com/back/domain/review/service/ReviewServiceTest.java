package com.back.domain.review.service;

import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.review.dto.request.ReviewCreateRequestDto;
import com.back.domain.review.dto.request.ReviewListRequestDto;
import com.back.domain.review.dto.request.ReviewUpdateRequestDto;
import com.back.domain.review.dto.request.ReviewWriteRequestDto;
import com.back.domain.review.dto.response.ReviewDetailResponseDto;
import com.back.domain.review.entity.Review;
import com.back.domain.review.entity.ReviewLike;
import com.back.domain.review.repository.ReviewLikeRepository;
import com.back.domain.review.repository.ReviewRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import org.springframework.test.util.ReflectionTestUtils;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("리뷰 서비스 테스트")
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private ReviewLikeRepository reviewLikeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private UserRepository userRepository;

    @InjectMocks
    private ReviewService reviewService;

    private User user;
    private Product product;
    private Review review;

    @BeforeEach
    void setUp() {
        user = createTestUser();
        product = createTestProduct();
        review = createTestReview();
    }

    @Test
    @DisplayName("리뷰 작성 성공")
    void createReview_Success() {
        // Given
        ReviewCreateRequestDto requestDto = ReviewCreateRequestDto.builder()
                .productId(1L)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .images(Arrays.asList())
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndUserAndNotDeleted(product, user)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        var result = reviewService.createReview(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("정말 좋은 상품입니다! 추천해요!");

        verify(productRepository).findById(1L);
        verify(reviewRepository).findByProductAndUserAndNotDeleted(product, user);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 상품을 찾을 수 없음")
    void createReview_ProductNotFound() {
        // Given
        ReviewCreateRequestDto requestDto = ReviewCreateRequestDto.builder()
                .productId(999L)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .build();

        when(productRepository.findById(999L)).thenReturn(Optional.empty());

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(requestDto, user))
                .isInstanceOf(ServiceException.class)
                .hasMessage("E-1 : 상품을 찾을 수 없습니다.");
    }

    @Test
    @DisplayName("리뷰 작성 실패 - 이미 리뷰 작성함")
    void createReview_AlreadyExists() {
        // Given
        ReviewCreateRequestDto requestDto = ReviewCreateRequestDto.builder()
                .productId(1L)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndUserAndNotDeleted(product, user)).thenReturn(Optional.of(review));

        // When & Then
        assertThatThrownBy(() -> reviewService.createReview(requestDto, user))
                .isInstanceOf(ServiceException.class)
                .hasMessage("E-2 : 이미 해당 상품에 리뷰를 작성하셨습니다.");
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공")
    void getReviewList_Success() {
        // Given
        ReviewListRequestDto requestDto = ReviewListRequestDto.builder()
                .productId(1L)
                .reviewType(ReviewListRequestDto.ReviewType.ALL)
                .page(0)
                .size(10)
                .build();

        List<Review> reviews = Arrays.asList(review);
        Page<Review> reviewPage = new PageImpl<>(reviews, PageRequest.of(0, 10), 1);

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndNotDeleted(any(Product.class), any(Pageable.class))).thenReturn(reviewPage);
        when(reviewRepository.countByProductAndNotDeleted(product)).thenReturn(1L);
        when(reviewRepository.countPhotoReviewsByProduct(product)).thenReturn(0L);
        when(reviewRepository.countGeneralReviewsByProduct(product)).thenReturn(1L);
        when(reviewRepository.findAverageRatingByProduct(product)).thenReturn(Optional.of(5.0));
        when(reviewRepository.findRatingDistributionByProduct(product)).thenReturn(new Object[0][]);
        
        // N+1 방지를 위해 Fetch Join 메서드 Mock 설정 (로그인 사용자용)
        when(reviewRepository.findByProductWithUserAndImagesAndLikes(eq(product), eq(user.getId()), any(Pageable.class)))
                .thenReturn(List.of(review));

        // When
        var result = reviewService.getReviewList(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviews()).hasSize(1);
        assertThat(result.getTotalCount()).isEqualTo(1);
        assertThat(result.getAverageRating()).isEqualTo(5.0);

        verify(productRepository).findById(1L);
        verify(reviewRepository).findByProductWithUserAndImagesAndLikes(eq(product), eq(user.getId()), any(Pageable.class));
    }

    @Test
    @DisplayName("리뷰 수정 성공")
    void updateReview_Success() {
        // Given
        ReviewUpdateRequestDto requestDto = ReviewUpdateRequestDto.builder()
                .rating(4)
                .content("수정된 리뷰 내용입니다.")
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        var result = reviewService.updateReview(1L, requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(4);
        assertThat(result.getContent()).isEqualTo("수정된 리뷰 내용입니다.");

        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 수정 실패 - 작성자가 아님")
    void updateReview_NotAuthor() {
        // Given
        User otherUser = createTestUser();
        ReflectionTestUtils.setField(otherUser, "id", 2L);
        
        ReviewUpdateRequestDto requestDto = ReviewUpdateRequestDto.builder()
                .rating(4)
                .content("수정된 리뷰 내용입니다.")
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));

        // When & Then
        assertThatThrownBy(() -> reviewService.updateReview(1L, requestDto, otherUser))
                .isInstanceOf(ServiceException.class)
                .hasMessage("E-7 : 본인이 작성한 리뷰만 수정할 수 있습니다.");
    }

    @Test
    @DisplayName("리뷰 삭제 성공")
    void deleteReview_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        reviewService.deleteReview(1L, user);

        // Then
        verify(reviewRepository).findById(1L);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 좋아요 토글 성공 - 좋아요 추가")
    void toggleReviewLike_Success_AddLike() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewAndUserAndNotDeleted(review, user)).thenReturn(Optional.empty());
        when(reviewLikeRepository.save(any(ReviewLike.class))).thenReturn(ReviewLike.builder().build());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        boolean result = reviewService.toggleReviewLike(1L, user);

        // Then
        assertThat(result).isTrue();
        verify(reviewRepository).findById(1L);
        verify(reviewLikeRepository).findByReviewAndUserAndNotDeleted(review, user);
        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 좋아요 토글 성공 - 좋아요 취소")
    void toggleReviewLike_Success_RemoveLike() {
        // Given
        ReviewLike reviewLike = ReviewLike.builder()
                .review(review)
                .user(user)
                .build();

        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewAndUserAndNotDeleted(review, user)).thenReturn(Optional.of(reviewLike));
        when(reviewLikeRepository.save(any(ReviewLike.class))).thenReturn(reviewLike);
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        boolean result = reviewService.toggleReviewLike(1L, user);

        // Then
        assertThat(result).isFalse();
        verify(reviewRepository).findById(1L);
        verify(reviewLikeRepository).findByReviewAndUserAndNotDeleted(review, user);
        verify(reviewLikeRepository).save(any(ReviewLike.class));
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 팝업 성공 - 피그마 디자인 기반")
    void writeReviewFromPopup_Success() {
        // Given - 피그마 디자인 요소들 포함
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productId(1L)
                .rating(5)
                .content("정말 좋은 상품입니다! 추천해요!")
                .images(Arrays.asList())
                .hashtags(Arrays.asList("좋아요", "추천", "만족"))
                .productOption("상품옵션1") // 피그마 디자인: 상품옵션1 표시
                .reviewType("PHOTO")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndUserAndNotDeleted(product, user)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenReturn(review);

        // When
        ReviewDetailResponseDto result = reviewService.writeReviewFromPopup(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("정말 좋은 상품입니다! 추천해요!");
        assertThat(result.getProductOption()).isEqualTo("상품옵션1");

        verify(productRepository).findById(1L);
        verify(reviewRepository).findByProductAndUserAndNotDeleted(product, user);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 작성 팝업 실패 - 해시태그 유효성 검사")
    void writeReviewFromPopup_InvalidHashtags() {
        // Given - 유효하지 않은 해시태그 (20자 초과)
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productId(1L)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .hashtags(Arrays.asList("이것은매우긴해시태그입니다20자를초과합니다"))
                .productOption("상품옵션1")
                .build();

        // When & Then - 유효성 검사에서 실패하므로 repository stubbing 불필요
        assertThatThrownBy(() -> reviewService.writeReviewFromPopup(requestDto, user))
                .isInstanceOf(ServiceException.class)
                .hasMessage("E-5 : 해시태그는 20자 이하로 입력해주세요.");
    }

    @Test
    @DisplayName("리뷰 작성 팝업 실패 - 이미지 개수 제한 초과")
    void writeReviewFromPopup_TooManyImages() {
        // Given - 이미지 6개 (최대 5개 제한 초과)
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productId(1L)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .images(Arrays.asList(
                    new com.back.global.s3.S3FileRequest("url1", com.back.global.s3.FileType.MAIN, "key1", "img1.jpg"),
                    new com.back.global.s3.S3FileRequest("url2", com.back.global.s3.FileType.ADDITIONAL, "key2", "img2.jpg"),
                    new com.back.global.s3.S3FileRequest("url3", com.back.global.s3.FileType.ADDITIONAL, "key3", "img3.jpg"),
                    new com.back.global.s3.S3FileRequest("url4", com.back.global.s3.FileType.ADDITIONAL, "key4", "img4.jpg"),
                    new com.back.global.s3.S3FileRequest("url5", com.back.global.s3.FileType.ADDITIONAL, "key5", "img5.jpg"),
                    new com.back.global.s3.S3FileRequest("url6", com.back.global.s3.FileType.ADDITIONAL, "key6", "img6.jpg")
                ))
                .productOption("상품옵션1")
                .build();

        // When & Then - 유효성 검사에서 실패하므로 repository stubbing 불필요
        assertThatThrownBy(() -> reviewService.writeReviewFromPopup(requestDto, user))
                .isInstanceOf(ServiceException.class)
                .hasMessage("E-6 : 이미지는 최대 5개까지 업로드 가능합니다.");
    }

    @Test
    @DisplayName("리뷰 작성 팝업 성공 - 포토리뷰 자동 설정")
    void writeReviewFromPopup_AutoPhotoReview() {
        // Given - 이미지가 있으면 자동으로 포토리뷰로 설정
        ReviewWriteRequestDto requestDto = ReviewWriteRequestDto.builder()
                .productId(1L)
                .rating(5)
                .content("정말 좋은 상품입니다!")
                .images(Arrays.asList(
                    new com.back.global.s3.S3FileRequest("url1", com.back.global.s3.FileType.MAIN, "key1", "image1.jpg")
                ))
                .productOption("상품옵션1")
                .build();

        when(productRepository.findById(1L)).thenReturn(Optional.of(product));
        when(reviewRepository.findByProductAndUserAndNotDeleted(product, user)).thenReturn(Optional.empty());
        when(reviewRepository.save(any(Review.class))).thenAnswer(invocation -> {
            Review savedReview = invocation.getArgument(0);
            ReflectionTestUtils.setField(savedReview, "id", 1L);
            return savedReview;
        });

        // When
        ReviewDetailResponseDto result = reviewService.writeReviewFromPopup(requestDto, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getIsPhotoReview()).isTrue(); // 이미지가 있으면 포토리뷰로 자동 설정

        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 상세 팝업 조회 성공 - 피그마 디자인 기반")
    void getReviewDetailForPopup_Success() {
        // Given
        when(reviewRepository.findById(1L)).thenReturn(Optional.of(review));
        when(reviewLikeRepository.findByReviewAndUserAndNotDeleted(review, user)).thenReturn(Optional.empty());

        // When
        ReviewDetailResponseDto result = reviewService.getReviewDetailForPopup(1L, user);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.getReviewId()).isEqualTo(1L);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("정말 좋은 상품입니다! 추천해요!");

        verify(reviewRepository).findById(1L);
        verify(reviewLikeRepository).findByReviewAndUserAndNotDeleted(review, user);
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

    private Product createTestProduct() {
        Product product = Product.builder()
                .name("테스트 상품")
                .price(10000)
                .discountRate(10)
                .build();
        ReflectionTestUtils.setField(product, "id", 1L);
        return product;
    }

    private Review createTestReview() {
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(5)
                .content("정말 좋은 상품입니다! 추천해요!")
                .likeCount(0)
                .isPhotoReview(false)
                .isDeleted(false)
                .build();
        ReflectionTestUtils.setField(review, "id", 1L);
        return review;
    }
}
