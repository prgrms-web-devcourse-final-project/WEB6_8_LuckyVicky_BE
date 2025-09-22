package com.back.domain.dashboard.artist.dto.response;

import com.back.global.util.PageResponse;
import lombok.*;

/**
 * 작가 상품 관련 응답 DTO
 * 
 * 작가의 상품 목록과 관련된 정보를 포함합니다.
 * 2025.09.22 생성
 */
public class ArtistProductResponse {
    
    /**
     * 상품 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<ArtistProductResponse.Product> {
        
        public List() {
            super();
        }
        
        public List(java.util.List<Product> content, int page, int size, 
                   long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
        }
    }
    
    /**
     * 상품 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Product {
        /** 상품 번호 */
        private String productNumber;
        /** 상품명 */
        private String productName;
        /** 가격 */
        private int price;
        /** 판매 상태 */
        private String sellingStatus;
        /** 상태 텍스트 */
        private String statusText;
        /** 등록일 */
        private String registrationDate;
    }
}
