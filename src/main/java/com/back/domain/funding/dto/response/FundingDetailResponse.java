package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.*;
import com.back.domain.user.entity.User;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FundingDetailResponse(
        Long id, // 펀딩 ID
        String title, // 펀딩 제목
        String description, // 펀딩 설명
        String imageUrl, // 펀딩 이미지 URL
//        String categoryName, // 카테고리 이름
        long targetAmount, // 목표 금액
        long currentAmount, // 현재 모인 금액
        int participants, // 참여자 수
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate, // 펀딩 시작일
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endDate, // 펀딩 종료일
        int remainingDays, // 남은 일수
        double progress, // 진행률 (currentAmount / targetAmount * 100)
        FundingStatus status, // 펀딩 상태
        AuthorDto author, // 작성자 정보
        List<FundingOptionDto> options, // 펀딩 옵션 목록
        List<FundingNewsDto> news, // 펀딩 새소식 목록
        List<FundingCommunityDto> communities // 펀딩 커뮤니티 목록
) {
    public FundingDetailResponse(Funding funding,
                                 long currentAmount,
                                 int participants,
                                 int remainingDays,
                                 double progress)
            {
        this(
                funding.getId(),
                funding.getTitle(),
                funding.getDescription(),
                funding.getImageUrl(),
//                funding.getCategory().getCategoryName(),
                funding.getTargetAmount(),
                currentAmount,
                participants,
                funding.getStartDate(),
                funding.getEndDate(),
                remainingDays,
                progress,
                funding.getStatus(),
                AuthorDto.from(funding.getUser()),
                funding.getOptions().stream().map(FundingOptionDto::new).collect(Collectors.toList()),
                funding.getNews().stream().map(FundingNewsDto::new).collect(Collectors.toList()),
                funding.getCommunities().stream().map(FundingCommunityDto::new).collect(Collectors.toList())
        );
    }

    // 작성자 DTO
    public record AuthorDto(Long id, String name, String profileImageUrl, String introduction) {
        public static AuthorDto from(User user) {
            return new AuthorDto(user.getId(), user.getName(), user.getProfileImageUrl(), null);
        }
    }

    // 옵션 DTO
    public record FundingOptionDto(Long id, String name, long price, int stock) {
        public FundingOptionDto(FundingOption option) {
            this(option.getId(), option.getName(), option.getPrice(), option.getStock());
        }
    }

    // 새소식 DTO
    public record FundingNewsDto(
            Long id,
            String actorNickname,
            String title,
            String content,
            String imageUrl,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createDate
    ) {
        public FundingNewsDto(FundingNews update) {
            this(update.getId(), update.getArtist().getName(), update.getTitle(), update.getContent(), update.getImageUrl(), update.getCreateDate());
        }
    }

    // 커뮤니티 DTO
    public record FundingCommunityDto(
            Long id,
            String writerName,
            String profileImageUrl,
            String content,
            @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
            LocalDateTime createDate
    ) {
        public FundingCommunityDto(FundingCommunity community) {
            this(community.getId(), community.getAuthor().getName(), community.getAuthor().getProfileImageUrl(), community.getContent(), community.getCreateDate());
        }
    }
}