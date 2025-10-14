package com.back.domain.search.service;

import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.funding.dto.response.FundingCardDto;
import com.back.domain.funding.repository.FundingRepository;
import com.back.domain.funding.service.FundingService;
import com.back.domain.product.product.dto.response.ProductListResponse;
import com.back.domain.product.product.entity.Product;
import com.back.domain.product.product.entity.ProductImage;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.domain.search.dto.ArtistSearchResultDto;
import com.back.domain.search.dto.SearchResponseDto;
import com.back.global.s3.FileType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class SearchService {

    private final ProductRepository productRepository;
    private final ArtistProfileRepository artistProfileRepository;
    private final FundingRepository fundingRepository;
    private final FundingService fundingService;

    public SearchResponseDto search(String keyword) {
        if (!StringUtils.hasText(keyword)) { // keyword가 없으면 빈 리스트 반환
            return SearchResponseDto.builder()
                    .products(Collections.emptyList())
                    .artists(Collections.emptyList())
                    .fundings(Collections.emptyList())
                    .build();
        }

        List<ProductListResponse.ProductInfo> products = searchProducts(keyword);
        List<ArtistSearchResultDto> artists = searchArtists(keyword);
        List<FundingCardDto> fundings = searchFundings(keyword);

        return SearchResponseDto.builder()
                .products(products)
                .artists(artists)
                .fundings(fundings)
                .build();
    }

    /**
     * 검색 키워드(상품명)에 해당하는 상품 조회
     */
    private List<ProductListResponse.ProductInfo> searchProducts(String keyword) {
        return productRepository.searchByProductNameOrBrandName(keyword).stream()
                .map(this::mapToProductInfo)
                .collect(Collectors.toList());
    }

    /**
     * 검색 키워드(작가 이름)에 해당하는 작가 조회
     */
    private List<ArtistSearchResultDto> searchArtists(String keyword) {
        return artistProfileRepository.searchByArtistName(keyword).stream()
                .map(this::mapToArtistSearchResultDto)
                .collect(Collectors.toList());
    }

    /**
     * 검색 키워드(펀딩 제목)에 해당하는 펀딩 조회
     */
    private List<FundingCardDto> searchFundings(String keyword) {
        return fundingRepository.searchByTitle(keyword).stream()
                .map(fundingService::toCardDto)
                .collect(Collectors.toList());
    }


    /**
     * 매핑 헬퍼 메서드들
     */
    private ProductListResponse.ProductInfo mapToProductInfo(Product product) {

        String imageUrl = product.getImages().stream() // 썸네일 이미지
                .filter(image -> image.getFileType() == FileType.THUMBNAIL)
                .map(ProductImage::getFileUrl)
                .findFirst()
                .orElse("");

        return new ProductListResponse.ProductInfo(
                product.getProductUuid(),
                imageUrl,
                product.getBrandName(),
                product.getName(),
                product.getPrice(),
                product.getDiscountRate(),
                product.getDiscountPrice(),
                product.getAverageRating()
        );
    }

    private ArtistSearchResultDto mapToArtistSearchResultDto(ArtistProfile artistProfile) {
        return ArtistSearchResultDto.builder()
                .artistId(artistProfile.getId())
                .artistName(artistProfile.getArtistName())
                .profileImageUrl(artistProfile.getProfileImageUrl())
                .build();
    }

}
