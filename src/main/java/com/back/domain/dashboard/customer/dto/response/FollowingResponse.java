package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

import lombok.*;

import java.time.LocalDateTime;

/**
 * 팔로우 관련 응답 DTO
 * 
 * 사용자가 팔로우한 작가들의 정보를 포함
 * 2025.09.22 수정 - API 명세 변경에 따른 구조 개편
 */
public class FollowingResponse {
    
    /**
     * 팔로우한 작가 목록 응답
     */
    @Getter
    @Setter
    public static class List extends PageResponse<FollowingResponse.Artist> {
        /** 조회 대상 사용자 프로필 */
        private Profile profile;
        /** 팔로우 현황 요약 정보 */
        private SummaryDto summary;
        
        public List() {
            super();
        }
        
        public List(Profile profile, SummaryDto summary, java.util.List<Artist> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.profile = profile;
            this.summary = summary;
        }
    }
    
    /**
     * 조회 대상 사용자 프로필 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Profile {
        /** 사용자 ID */
        private String userId;
        /** 닉네임 */
        private String nickname;
        /** 프로필 이미지 URL */
        private String profileImageUrl;
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
        private String artistId;
        /** 작가명 */
        private String artistName;
        /** 프로필 이미지 URL */
        private String profileImageUrl;
        /** 팔로워 수 */
        private int followerCount;
        /** 작가 페이지 URL */
        private String artistPageUrl;
        /** 팔로우 관계 정보 */
        private FollowRelation followRelation;
        /** 배지 정보 */
        private Badge badges;
    }
    
    /**
     * 팔로우 관계 정보
     */
    @Getter
    @Setter
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class FollowRelation {
        /** 관계 상태 (FOLLOWING) */
        private String status;
        /** 팔로우한 날짜 */
        private LocalDateTime followedAt;
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
}
