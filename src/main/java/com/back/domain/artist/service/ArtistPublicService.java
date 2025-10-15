package com.back.domain.artist.service;

import com.back.domain.artist.dto.response.ArtistListResponse;
import com.back.domain.artist.dto.response.ArtistProductResponse;
import com.back.domain.artist.dto.response.ArtistPublicProfileResponse;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.ProductImage;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.FileType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

/**
 * 작가 공개 정보 조회 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class ArtistPublicService {

    private final ArtistProfileRepository artistProfileRepository;
    private final ProductRepository productRepository;

    /**
     * 작가 목록 조회
     */
    public List<ArtistListResponse> getArtistList() {
        log.info("작가 목록 조회 시작 (전체)");

        List<ArtistProfile> artists = artistProfileRepository.findAllByOrderByArtistNameAsc();

        log.info("작가 목록 조회 완료 - 조회된 작가 수: {}", artists.size());

        return artists.stream()
                .map(ArtistListResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 작가 공개 프로필 상세 조회
     */
    public ArtistPublicProfileResponse getPublicProfile(Long artistId) {
        log.info("작가 공개 프로필 조회 시작 - artistId: {}", artistId);

        // 작가 프로필 조회
        ArtistProfile artistProfile = artistProfileRepository.findById(artistId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 작가입니다."));

        log.info("작가 공개 프로필 조회 완료 - artistId: {}, artistName: {}",
                artistId, artistProfile.getArtistName());

        return ArtistPublicProfileResponse.from(artistProfile);
    }

    /**
     * 작가 상품 목록 조회
     */
    public List<ArtistProductResponse> getArtistProducts(Long artistProfileId) {
        log.info("작가 상품 목록 조회 시작 - artistProfileId: {}", artistProfileId);

        // 작가 프로필 조회
        ArtistProfile artistProfile = artistProfileRepository.findById(artistProfileId)
                .orElseThrow(() -> new ServiceException("404", "존재하지 않는 작가입니다."));

        // 작가의 상품 목록 조회
        List<Product> products = productRepository.findByUserIdAndIsDeletedFalse(
                artistProfile.getUser().getId()
        );

        log.info("작가 상품 목록 조회 완료 - artistProfileId: {}, 조회된 상품 수: {}",
                artistProfileId, products.size());

        return products.stream()
                .map(this::convertToArtistProductResponse)
                .collect(Collectors.toList());
    }


    // ====== 헬퍼 메서드 ====== //
    /**
     * Product -> ArtistProductResponse 변환
     */
    private ArtistProductResponse convertToArtistProductResponse(Product product) {
        // 썸네일 이미지 추출
        String thumbnailUrl = product.getImages().stream()
                .filter(img -> FileType.THUMBNAIL.equals(img.getFileType()))
                .findFirst()
                .map(ProductImage::getFileUrl)
                .orElse(null);

        // 평균 평점과 리뷰 수 계산
        // TODO: 실제 리뷰 데이터를 기반으로 계산 로직 구현 필요
        Double rating = 0.0;
        Long reviewCount = 0L;

        return new ArtistProductResponse(
                product.getProductUuid(),
                product.getName(),
                product.getPrice(),
                product.getDiscountPrice(),
                product.getDiscountRate(),
                thumbnailUrl,
                rating,
                reviewCount,
                product.getStock(),
                product.getSellingStatus().name()
        );
    }


}
