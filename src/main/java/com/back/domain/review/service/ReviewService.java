package com.back.domain.review.service;

import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.review.dto.request.ReviewCreateRequestDto;
import com.back.domain.review.dto.request.ReviewListRequestDto;
import com.back.domain.review.dto.request.ReviewUpdateRequestDto;
import com.back.domain.review.dto.request.ReviewWriteRequestDto;
import com.back.domain.review.dto.response.ReviewDetailResponseDto;
import com.back.domain.review.dto.response.ReviewListResponseDto;
import com.back.domain.review.dto.response.ReviewResponseDto;
import com.back.domain.review.dto.response.ReviewStatsResponseDto;
import com.back.domain.review.entity.Review;
import com.back.domain.review.entity.ReviewImage;
import com.back.domain.review.entity.ReviewLike;
import com.back.domain.review.repository.ReviewLikeRepository;
import com.back.domain.review.repository.ReviewRepository;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * 리뷰 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final ReviewLikeRepository reviewLikeRepository;
    private final ProductRepository productRepository;
    private final UserRepository userRepository;

    /**
     * 리뷰 작성
     */
    @Transactional
    public ReviewResponseDto createReview(ReviewCreateRequestDto requestDto, User user) {
        log.info("리뷰 작성 요청 - 상품UUID: {}, 사용자: {}, 평점: {}", 
                requestDto.getProductUuid(), user.getId(), requestDto.getRating());

        // 상품 존재 확인
        Product product = productRepository.findByProductUuid(requestDto.getProductUuid())
                .orElseThrow(() -> new ServiceException("E-1", "상품을 찾을 수 없습니다."));

        // 이미 리뷰를 작성했는지 확인
        Optional<Review> existingReview = reviewRepository.findByProductAndUserAndNotDeleted(product, user);
        if (existingReview.isPresent()) {
            throw new ServiceException("E-2", "이미 해당 상품에 리뷰를 작성하셨습니다.");
        }

        // 리뷰 생성
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .build();

        // 이미지 추가 (S3FileRequest에서 데이터 가져옴)
        if (requestDto.getImages() != null && !requestDto.getImages().isEmpty()) {
            for (int i = 0; i < requestDto.getImages().size(); i++) {
                var imageData = requestDto.getImages().get(i);
                ReviewImage image = ReviewImage.builder()
                        .imageUrl(imageData.url())
                        .originalFileName(imageData.originalFileName())
                        .s3Key(imageData.s3Key())
                        .fileType(imageData.type())
                        .sortOrder(i)
                        .build();
                review.addImage(image);
            }
        }
        
        // 상품 옵션 설정
        if (requestDto.getProductOption() != null) {
            review.setProductOption(requestDto.getProductOption());
        }
        
        // 해시태그 설정
        if (requestDto.getHashtags() != null && !requestDto.getHashtags().isEmpty()) {
            review.setHashtagsList(requestDto.getHashtags());
        }

        Review savedReview = reviewRepository.save(review);

        log.info("리뷰 작성 완료 - 리뷰ID: {}", savedReview.getId());

        // 상품 평점 및 리뷰 개수 업데이트
        updateProductReviewStats(product);

        return ReviewResponseDto.from(savedReview, false); // 작성자는 좋아요 안 누름
    }

    /**
     * 리뷰 목록 조회
     */
    public ReviewListResponseDto getReviewList(ReviewListRequestDto requestDto, User currentUser) {
        log.info("리뷰 목록 조회 요청 - 상품UUID: {}, 리뷰타입: {}", 
                requestDto.getProductUuid(), requestDto.getReviewType());

        Product product = productRepository.findByProductUuid(requestDto.getProductUuid())
                .orElseThrow(() -> new ServiceException("E-1", "상품을 찾을 수 없습니다."));

        // 페이지 설정
        Pageable pageable = createPageable(requestDto);

        // 리뷰 목록 조회
        Page<Review> reviewPage = getReviewsByType(product, requestDto.getReviewType(), pageable);

        // 리뷰 통계 조회
        ReviewStatsResponseDto stats = getReviewStats(product);

        // 리뷰 응답 DTO 변환 (N+1 방지)
        List<ReviewResponseDto> reviewDtos;
        
        if (currentUser != null) {
            // 로그인 사용자: Fetch Join으로 좋아요 정보 포함 조회
            List<Review> reviewsWithLikes = reviewRepository.findByProductWithUserAndImagesAndLikes(
                    product, currentUser.getId(), pageable);
            reviewDtos = reviewsWithLikes.stream()
                    .map(review -> {
                        boolean isLiked = review.getLikes().stream()
                                .anyMatch(like -> like.getUser().getId().equals(currentUser.getId()));
                        return ReviewResponseDto.from(review, isLiked);
                    })
                    .collect(Collectors.toList());
        } else {
            // 비로그인 사용자: 기본 조회
            reviewDtos = reviewPage.getContent().stream()
                    .map(review -> ReviewResponseDto.from(review, false))
                    .collect(Collectors.toList());
        }

        return ReviewListResponseDto.builder()
                .reviews(reviewDtos)
                .totalCount(stats.getTotalReviewCount())
                .photoReviewCount(stats.getPhotoReviewCount())
                .generalReviewCount(stats.getGeneralReviewCount())
                .averageRating(stats.getAverageRating())
                .hasNext(reviewPage.hasNext())
                .currentPage(reviewPage.getNumber())
                .totalPages(reviewPage.getTotalPages())
                .build();
    }

    /**
     * 리뷰 상세 조회
     */
    public ReviewResponseDto getReviewDetail(Long reviewId, User currentUser) {
        log.info("리뷰 상세 조회 요청 - 리뷰ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("E-3", "리뷰를 찾을 수 없습니다."));

        if (review.getIsDeleted()) {
            throw new ServiceException("E-4", "삭제된 리뷰입니다.");
        }

        // 현재 사용자의 좋아요 여부 확인
        boolean isLiked = false;
        if (currentUser != null) {
            isLiked = reviewLikeRepository.findByReviewAndUserAndNotDeleted(review, currentUser).isPresent();
        }

        return ReviewResponseDto.from(review, isLiked);
    }

    /**
     * 리뷰 상세 팝업용 조회 (이미지 확대보기 등)
     */
    public ReviewDetailResponseDto getReviewDetailForPopup(Long reviewId, User currentUser) {
        log.info("리뷰 상세 팝업 조회 요청 - 리뷰ID: {}", reviewId);

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("E-3", "리뷰를 찾을 수 없습니다."));

        if (review.getIsDeleted()) {
            throw new ServiceException("E-4", "삭제된 리뷰입니다.");
        }

        ReviewDetailResponseDto dto = ReviewDetailResponseDto.from(review);
        
        // 현재 사용자의 좋아요 여부 설정
        if (currentUser != null) {
            boolean isLiked = reviewLikeRepository.findByReviewAndUserAndNotDeleted(review, currentUser).isPresent();
            dto = dto.toBuilder().isLiked(isLiked).build();
        }

        return dto;
    }

    /**
     * 리뷰 작성 팝업용 작성
     */
    @Transactional
    public ReviewDetailResponseDto writeReviewFromPopup(ReviewWriteRequestDto requestDto, User user) {
        log.info("리뷰 작성 팝업 요청 - 상품UUID: {}, 사용자: {}, 평점: {}, 상품옵션: {}", 
                requestDto.getProductUuid(), user.getId(), requestDto.getRating(), requestDto.getProductOption());

        // 입력 데이터 유효성 검사
        validateReviewWriteRequest(requestDto);

        // 상품 존재 확인
        Product product = productRepository.findByProductUuid(requestDto.getProductUuid())
                .orElseThrow(() -> new ServiceException("E-1", "상품을 찾을 수 없습니다."));

        // 이미 리뷰를 작성했는지 확인
        Optional<Review> existingReview = reviewRepository.findByProductAndUserAndNotDeleted(product, user);
        if (existingReview.isPresent()) {
            throw new ServiceException("E-2", "이미 해당 상품에 리뷰를 작성하셨습니다.");
        }

        // 포토리뷰 여부 자동 설정 (이미지가 있으면 포토리뷰)
        boolean isPhotoReview = requestDto.isPhotoReview();

        // 리뷰 생성
        Review review = Review.builder()
                .product(product)
                .user(user)
                .rating(requestDto.getRating())
                .content(requestDto.getContent())
                .isPhotoReview(isPhotoReview)
                .build();

        // 해시태그와 상품옵션 설정
        if (requestDto.getHashtags() != null && !requestDto.getHashtags().isEmpty()) {
            review.setHashtagsList(requestDto.getHashtags());
        }
        if (requestDto.getProductOption() != null) {
            review.setProductOption(requestDto.getProductOption());
        }

        // 이미지 추가
        addReviewImagesFromS3(review, requestDto.getImages());

        Review savedReview = reviewRepository.save(review);

        log.info("리뷰 작성 팝업 완료 - 리뷰ID: {}, 포토리뷰여부: {}, 이미지개수: {}", 
                savedReview.getId(), isPhotoReview, 
                savedReview.getImages().size());

        return ReviewDetailResponseDto.from(savedReview);
    }

    /**
     * 리뷰 작성 요청 데이터 유효성 검사
     */
    private void validateReviewWriteRequest(ReviewWriteRequestDto requestDto) {
        // 해시태그 유효성 검사
        if (!requestDto.hasValidHashtags()) {
            throw new ServiceException("E-5", "해시태그는 20자 이하로 입력해주세요.");
        }

        // 이미지 개수 제한 (최대 5개)
        if (requestDto.getImages() != null && requestDto.getImages().size() > 5) {
            throw new ServiceException("E-6", "이미지는 최대 5개까지 업로드 가능합니다.");
        }
    }

    /**
     * S3FileRequest로부터 리뷰 이미지 추가
     */
    private void addReviewImagesFromS3(Review review, List<com.back.global.s3.S3FileRequest> images) {
        if (images == null || images.isEmpty()) {
            return;
        }

        for (int i = 0; i < images.size(); i++) {
            var imageData = images.get(i);
            ReviewImage image = ReviewImage.builder()
                    .imageUrl(imageData.url())
                    .originalFileName(imageData.originalFileName())
                    .s3Key(imageData.s3Key())
                    .fileType(imageData.type())
                    .sortOrder(i)
                    .build();
            review.addImage(image);
        }
    }

    /**
     * 리뷰 수정
     */
    @Transactional
    public ReviewResponseDto updateReview(Long reviewId, ReviewUpdateRequestDto requestDto, User user) {
        log.info("리뷰 수정 요청 - 리뷰ID: {}, 사용자: {}", reviewId, user.getId());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("E-3", "리뷰를 찾을 수 없습니다."));

        if (review.getIsDeleted()) {
            throw new ServiceException("E-4", "삭제된 리뷰입니다.");
        }

        // 작성자 확인
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ServiceException("E-7", "본인이 작성한 리뷰만 수정할 수 있습니다.");
        }

        // 리뷰 수정
        review.updateReview(requestDto.getRating(), requestDto.getContent());

        // 기존 이미지 삭제 후 새 이미지 추가
        review.getImages().clear();
        addReviewImagesFromS3(review, requestDto.getImages());
        
        // 상품 옵션 업데이트
        if (requestDto.getProductOption() != null) {
            review.setProductOption(requestDto.getProductOption());
        }
        
        // 해시태그 업데이트
        if (requestDto.getHashtags() != null) {
            review.setHashtagsList(requestDto.getHashtags());
        }

        Review savedReview = reviewRepository.save(review);

        // 상품 평점 및 리뷰 개수 업데이트
        updateProductReviewStats(savedReview.getProduct());

        // 사용자가 좋아요 눌렀는지 확인
        boolean isLiked = reviewLikeRepository.findByReviewAndUserAndNotDeleted(savedReview, user).isPresent();

        log.info("리뷰 수정 완료 - 리뷰ID: {}", savedReview.getId());
        return ReviewResponseDto.from(savedReview, isLiked);
    }

    /**
     * 리뷰 삭제
     */
    @Transactional
    public void deleteReview(Long reviewId, User user) {
        log.info("리뷰 삭제 요청 - 리뷰ID: {}, 사용자: {}", reviewId, user.getId());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("E-3", "리뷰를 찾을 수 없습니다."));

        if (review.getIsDeleted()) {
            throw new ServiceException("E-8", "이미 삭제된 리뷰입니다.");
        }

        // 작성자 확인
        if (!review.getUser().getId().equals(user.getId())) {
            throw new ServiceException("E-9", "본인이 작성한 리뷰만 삭제할 수 있습니다.");
        }

        review.deleteReview();
        reviewRepository.save(review);

        // 상품 평점 및 리뷰 개수 업데이트
        updateProductReviewStats(review.getProduct());

        log.info("리뷰 삭제 완료 - 리뷰ID: {}", reviewId);
    }

    /**
     * 리뷰 좋아요 토글
     */
    @Transactional
    public boolean toggleReviewLike(Long reviewId, User user) {
        log.info("리뷰 좋아요 토글 요청 - 리뷰ID: {}, 사용자: {}", reviewId, user.getId());

        Review review = reviewRepository.findById(reviewId)
                .orElseThrow(() -> new ServiceException("E-3", "리뷰를 찾을 수 없습니다."));

        if (review.getIsDeleted()) {
            throw new ServiceException("E-4", "삭제된 리뷰입니다.");
        }

        Optional<ReviewLike> existingLike = reviewLikeRepository.findByReviewAndUserAndNotDeleted(review, user);

        if (existingLike.isPresent()) {
            // 좋아요 취소
            ReviewLike reviewLike = existingLike.get();
            reviewLike.cancelLike();
            reviewLikeRepository.save(reviewLike);
            
            // DB 쿼리로 직접 감소 (동시성 안전)
            reviewRepository.decreaseLikeCount(reviewId);
            
            log.info("리뷰 좋아요 취소 - 리뷰ID: {}", reviewId);
            return false;
        } else {
            // 좋아요 추가
            ReviewLike reviewLike = ReviewLike.builder()
                    .review(review)
                    .user(user)
                    .build();
            reviewLikeRepository.save(reviewLike);
            
            // DB 쿼리로 직접 증가 (동시성 안전)
            reviewRepository.increaseLikeCount(reviewId);
            
            log.info("리뷰 좋아요 추가 - 리뷰ID: {}", reviewId);
            return true;
        }
    }

    /**
     * 리뷰 통계 조회
     */
    public ReviewStatsResponseDto getReviewStats(Product product) {
        Integer totalCount = Math.toIntExact(reviewRepository.countByProductAndNotDeleted(product));
        Integer photoReviewCount = Math.toIntExact(reviewRepository.countPhotoReviewsByProduct(product));
        Integer generalReviewCount = Math.toIntExact(reviewRepository.countGeneralReviewsByProduct(product));
        
        Double averageRating = reviewRepository.findAverageRatingByProduct(product).orElse(0.0);
        
        Object[][] ratingDistributionArray = reviewRepository.findRatingDistributionByProduct(product);
        Map<Integer, Integer> ratingDistribution = new HashMap<>();
        for (Object[] row : ratingDistributionArray) {
            ratingDistribution.put((Integer) row[0], ((Number) row[1]).intValue());
        }

        return ReviewStatsResponseDto.builder()
                .totalReviewCount(totalCount)
                .photoReviewCount(photoReviewCount)
                .generalReviewCount(generalReviewCount)
                .averageRating(averageRating)
                .ratingDistribution(ratingDistribution)
                .build();
    }

    /**
     * 페이지 설정 생성 (기본 최신순 정렬)
     */
    private Pageable createPageable(ReviewListRequestDto requestDto) {
        int page = requestDto.getPage() != null ? requestDto.getPage() : 0;
        int size = requestDto.getSize() != null ? requestDto.getSize() : 10;

        // 기본 최신순 정렬
        Sort sort = Sort.by(Sort.Direction.DESC, "createDate");

        return PageRequest.of(page, size, sort);
    }

    /**
     * 리뷰 타입별 조회
     */
    private Page<Review> getReviewsByType(Product product, ReviewListRequestDto.ReviewType reviewType, Pageable pageable) {
        switch (reviewType) {
            case PHOTO:
                return reviewRepository.findPhotoReviewsByProduct(product, pageable);
            case GENERAL:
                return reviewRepository.findGeneralReviewsByProduct(product, pageable);
            case ALL:
            default:
                return reviewRepository.findByProductAndNotDeleted(product, pageable);
        }
    }

    /**
     * 상품의 평균 평점(averageRating)과 리뷰 개수(reviewCount) 업데이트
     */
    private void updateProductReviewStats(Product product) {
        Long reviewCount = reviewRepository.countByProductAndNotDeleted(product);
        Double averageRating = reviewRepository.findAverageRatingByProduct(product).orElse(0.0);

        product.setReviewCount(reviewCount.intValue()); // 상태 업데이트
        product.setAverageRating(averageRating); // 상태 업데이트
        productRepository.save(product); // DB 저장
        log.info("상품 리뷰 통계 업데이트 완료 - 상품ID: {}, 리뷰개수: {}, 평균평점: {}",
                product.getId(), reviewCount, averageRating);
    }
}
