package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.FundingNews;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record FundingNewsItemDto(
        Long id,
        String artistNickname,
        String title,
        String content,
        String imageUrl,
        String s3Key,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createDate
) {
    public FundingNewsItemDto(FundingNews u) {
        this(
                u.getId(),
                u.getArtist().getName(),
                u.getTitle(),
                u.getContent(),
                u.getImageUrl(),
                u.getS3Key(),
                u.getCreateDate()
        );
    }
}