package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 펀딩 참여 관련 응답 DTO
 * 
 * 사용자가 참여한 펀딩들의 정보를 포함
 * 2025.09.22 수정 - API 명세 변경에 따른 구조 개편
 */
public class FundingResponse {
    
    /**
     * 참여한 펀딩 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<FundingResponse.Participation> {
        /** 펀딩 참여 현황 요약 정보 */
        private SummaryDto summary;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Participation> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
    }
    
    /**
     * 펀딩 참여 현황 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        /** 전체 참여 펀딩 수 */
        private int totalParticipations;
        /** 진행중인 펀딩 수 */
        private int active;
        /** 종료된 펀딩 수 (성공/실패 합) */
        private int ended;
        /** 발송준비중인 펀딩 수 */
        private int fulfilling;
        /** 발송완료된 펀딩 수 */
        private int fulfilled;
    }
    
    /**
     * 펀딩 참여 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Participation {
        /** 참여번호 (리스트 표기용 짧은 번호) */
        private String participationNumber;
        /** 내부 식별자 */
        private String participationId;
        /** 이미지 URL */
        private String imageUrl;
        /** 펀딩 제목 */
        private String title;
        /** 작가 정보 */
        private Artist artist;
        /** 수량 */
        private int quantity;
        /** 후원 금액 */
        private int pledgedAmount;
        /** 상태 (ACTIVE | ENDED | FULFILLING | FULFILLED) */
        private String status;
        /** 상태 텍스트 */
        private String statusText;
        /** 후원 일자 (YYYY-MM-DD) */
        private String pledgedDate;
        /** 링크 정보 */
        private Link links;
        /** 메타 정보 (선택적) */
        private Meta meta;
    }
    
    /**
     * 작가 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        /** 작가 ID */
        private String id;
        /** 작가명 */
        private String name;
    }
    
    /**
     * 링크 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        /** 상세 페이지 URL */
        private String detail;
    }
    
    /**
     * 메타 정보 (선택적)
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Meta {
        /** 펀딩 ID */
        private Long fundingId;
        /** 펀딩 번호 */
        private String fundingNumber;
        /** 달성률 */
        private int achievementRate;
        /** 현재 모금액 */
        private int currentAmount;
        /** 목표 금액 */
        private int targetAmount;
        /** 서포터 수 */
        private int supporterCount;
        /** 사용자 후원 정보 */
        private UserPledge userPledge;
        /** 권한 정보 */
        private Permission permissions;
    }
    
    /**
     * 사용자 후원 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPledge {
        /** 리워드 티어명 */
        private String rewardTierName;
        /** 후원 일시 */
        private LocalDateTime pledgedAt;
        /** 원시 상태 (내부용) */
        private String rawStatus;
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
        /** 후원 취소 가능 여부 */
        private Boolean canCancelPledge;
        /** 배송 정보 수정 가능 여부 */
        private Boolean canEditShippingInfo;
    }
}
