package com.back.domain.dashboard.customer.dto.response;

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
    public record FormData(
            /** 주문 요약 정보 */
            Summary summary,
            /** 폼 데이터 */
            Form form,
            /** 권한 정보 */
            Permission permissions
    ) {}
    
    /**
     * 주문 요약 정보
     */
    public record Summary(
            /** 주문 번호 */
            String orderNo,
            /** 브랜드명 */
            String brandName,
            /** 상품명 */
            String title,
            /** 가격 */
            int price,
            /** 수량 */
            int quantity,
            /** 썸네일 URL */
            String thumbnailUrl
    ) {}
    
    /**
     * 폼 데이터
     */
    public record Form(
            /** 타입 (EXCHANGE | RETURN) */
            String type,
            /** 방법 (PICKUP | CONSIGN) */
            String method,
            /** 사유 코드 */
            String reasonCode,
            /** 상세 사유 */
            String detail,
            /** 첨부 이미지 */
            java.util.List<Image> images,
            /** 픽업 정보 (method=PICKUP일 때만) */
            Pickup pickup
    ) {}
    
    /**
     * 첨부 이미지 정보
     */
    public record Image(
            /** 파일 ID */
            String fileId,
            /** 파일명 */
            String fileName
    ) {}
    
    /**
     * 픽업 정보
     */
    public record Pickup(
            /** 우편번호 */
            String zip,
            /** 주소1 */
            String address1,
            /** 주소2 */
            String address2,
            /** 수령인명 */
            String name,
            /** 전화번호 */
            String phone
    ) {}
    
    /**
     * 권한 정보
     */
    public record Permission(
            /** 편집 가능 여부 */
            Boolean canEdit,
            /** 취소 가능 여부 */
            Boolean canCancel
    ) {}
}
