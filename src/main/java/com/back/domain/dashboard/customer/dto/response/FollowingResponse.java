package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;

/**
 * 팔로우 관련 응답 DTO
 */
public class FollowingResponse {
    
    @Getter
    @Setter
    public static class List extends PageResponse<FollowingResponse.Artist> {
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SummaryDto {
        private int totalFollowing;
        private int newFollowing7d;
    }
    
    @Getter
    @Setter
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
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Badge {
        private Boolean verified;
    }
    
    @Getter
    @Setter
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Permission {
        private Boolean canUnfollow;
    }
}
