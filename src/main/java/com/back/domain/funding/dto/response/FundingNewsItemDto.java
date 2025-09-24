package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.FundingNews;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record FundingNewsItemDto(
        Long id,
        String actorNickname,
        String title,
        String content,
        String imageUrl,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createDate
) {
    public FundingNewsItemDto(FundingNews u) {
        this(
                u.getId(),
                u.getAuthor().getName(),
                u.getTitle(),
                u.getContent(),
                u.getImageUrl(),
                u.getCreateDate()
        );
    }
}