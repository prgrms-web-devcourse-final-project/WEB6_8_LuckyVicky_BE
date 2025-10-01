package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;

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
    public static class List extends PageResponse<FollowingResponse.Artist> {
        /** 조회 대상 사용자 프로필 */
        private final Profile profile;
        /** 팔로우 현황 요약 정보 */
        private final SummaryDto summary;
        
        public List() {
            super();
            this.profile = null;
            this.summary = null;
        }
        
        public List(Profile profile, SummaryDto summary, java.util.List<Artist> content,
                   int page, int size, long totalElements, int totalPages,
                   boolean hasNext, boolean hasPrevious) {
            super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
            this.profile = profile;
            this.summary = summary;
        }
        
        public Profile getProfile() {
            return profile;
        }
        
        public SummaryDto getSummary() {
            return summary;
        }
    }
    
    /**
     * 조회 대상 사용자 프로필 정보
     */
    public record Profile(
            /** 사용자 ID */
            String userId,
            /** 닉네임 */
            String nickname,
            /** 프로필 이미지 URL */
            String profileImageUrl
    ) {}
    
    /**
     * 팔로우 현황 요약 정보
     */
    public record SummaryDto(
            /** 전체 팔로우 작가 수 */
            int totalFollowing
    ) {}
    
    /**
     * 작가 정보
     */
    public record Artist(
            /** 작가 ID */
            String artistId,
            /** 작가명 */
            String artistName,
            /** 프로필 이미지 URL */
            String profileImageUrl,
            /** 팔로워 수 */
            int followerCount,
            /** 작가 페이지 URL */
            String artistPageUrl,
            /** 팔로우 관계 정보 */
            FollowRelation followRelation,
            /** 배지 정보 */
            Badge badges
    ) {}
    
    /**
     * 팔로우 관계 정보
     */
    public record FollowRelation(
            /** 관계 상태 (FOLLOWING) */
            String status,
            /** 팔로우한 날짜 */
            LocalDateTime followedAt
    ) {}
    
    /**
     * 작가 배지 정보
     */
    public record Badge(
            /** 인증 작가 여부 */
            Boolean verified
    ) {}
}
