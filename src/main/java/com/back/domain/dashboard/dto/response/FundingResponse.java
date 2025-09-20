package com.back.domain.dashboard.dto.response;

import com.back.global.util.PageResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 펀딩 참여 관련 응답 DTO
 */
public class FundingResponse {
    
    @Getter
    @Setter
    public static class List extends PageResponse<FundingResponse.Participation> {
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private int totalParticipations;
        private int active;
        private int success;
        private int failed;
    }
    
    @Getter
    @Setter
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
    
    @Getter
    @Setter
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Artist {
        private String id;
        private String name;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UserPledge {
        private int pledgedAmount;
        private String rewardTierName;
        private int quantity;
        private LocalDateTime pledgedAt;
        private String status;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private Boolean canCancelPledge;
        private Boolean canEditShippingInfo;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Link {
        private String detail;
    }
}
