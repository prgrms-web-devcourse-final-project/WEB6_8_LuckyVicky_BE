package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 팔로우 관련 응답 DTO
 * 
 * 사용자가 팔로우한 작가들의 정보를 포함합니다.
 *2025.09.20 수정
 */
public class FollowingResponse {
    
    /**
     * 팔로우한 작가 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<FollowingResponse.Artist> {
        /** 팔로우 현황 요약 정보 */
        private SummaryDto summary;
        
        public List() {
            super();
        }
        
        public List(SummaryDto summary, java.util.List<Artist> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.summary = summary;
        }
    }
    
    /**
     * 팔로우 현황 요약 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        /** 전체 팔로우 작가 수 */
        private int totalFollowing;
        /** 최근 7일간 새로 팔로우한 작가 수 */
        private int newFollowing7d;
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
        private String artistId;
        private String artistName;
        private String profileImageUrl;
        private int followerCount;
        private String artistPageUrl;
        private LocalDateTime followedAt;
        private LocalDateTime lastPublishedAt;
        private java.util.List<String> categories;
        private Badge badges;
        private Permission permissions;
    }
    
    /**
     * 작가 배지 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Badge {
        /** 인증 작가 여부 */
        private Boolean verified;
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
        /** 언팔로우 가능 여부 */
        private Boolean canUnfollow;
    }
}
