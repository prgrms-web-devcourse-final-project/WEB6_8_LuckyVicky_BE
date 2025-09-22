package com.back.domain.dashboard.customer.dto.response;

import lombok.*;

/**
 * 교환/반품 관련 응답 DTO
 * 
 * 고객의 교환/반품 신청 관련 정보를 포함합니다.
 * 2025.09.22 생성
 */
public class ReturnResponse {
    
    /**
     * 교환/반품 폼 데이터 응답
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FormData {
        /** 주문 요약 정보 */
        private Summary summary;
        /** 폼 데이터 */
        private Form form;
        /** 권한 정보 */
        private Permission permissions;
    }
    
    /**
     * 주문 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Summary {
        /** 주문 번호 */
        private String orderNo;
        /** 브랜드명 */
        private String brandName;
        /** 상품명 */
        private String title;
        /** 가격 */
        private int price;
        /** 수량 */
        private int quantity;
        /** 썸네일 URL */
        private String thumbnailUrl;
    }
    
    /**
     * 폼 데이터
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Form {
        /** 타입 (EXCHANGE | RETURN) */
        private String type;
        /** 방법 (PICKUP | CONSIGN) */
        private String method;
        /** 사유 코드 */
        private String reasonCode;
        /** 상세 사유 */
        private String detail;
        /** 첨부 이미지 */
        private java.util.List<Image> images;
        /** 픽업 정보 (method=PICKUP일 때만) */
        private Pickup pickup;
    }
    
    /**
     * 첨부 이미지 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Image {
        /** 파일 ID */
        private String fileId;
        /** 파일명 */
        private String fileName;
    }
    
    /**
     * 픽업 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Pickup {
        /** 우편번호 */
        private String zip;
        /** 주소1 */
        private String address1;
        /** 주소2 */
        private String address2;
        /** 수령인명 */
        private String name;
        /** 전화번호 */
        private String phone;
    }
    
    /**
     * 권한 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        /** 편집 가능 여부 */
        private Boolean canEdit;
        /** 취소 가능 여부 */
        private Boolean canCancel;
    }
}
