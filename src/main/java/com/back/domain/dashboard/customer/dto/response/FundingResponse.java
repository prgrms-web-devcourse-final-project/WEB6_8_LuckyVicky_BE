package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 펀딩 참여 관련 응답 DTO
 * 
 * <p>사용자가 참여한 펀딩들의 정보를 포함합니다.
 *2025.09.20 수정
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
        /** 성공한 펀딩 수 */
        private int success;
        /** 실패한 펀딩 수 */
        private int failed;
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
        private String participationId;
        private Funding funding;
        private UserPledge userPledge;
        private String status;
        private String statusText;
        private Permission permissions;
        private Link links;
    }
    
    /**
     * 펀딩 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Funding {
        private Long fundingId;
        private String fundingNumber;
        private String title;
        private Artist artist;
        private String thumbnailUrl;
        private String endDate;
        private int achievementRate;
        private int currentAmount;
        private int targetAmount;
        private int supporterCount;
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
        private String id;
        private String name;
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
        /** 후원 금액 */
        private int pledgedAmount;
        /** 리워드 티어명 */
        private String rewardTierName;
        /** 수량 */
        private int quantity;
        /** 후원 일시 */
        private LocalDateTime pledgedAt;
        /** 후원 상태 */
        private String status;
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
}
