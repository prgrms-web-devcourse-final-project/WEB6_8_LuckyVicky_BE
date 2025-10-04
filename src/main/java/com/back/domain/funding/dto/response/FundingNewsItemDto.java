package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.FundingNews;
import com.fasterxml.jackson.annotation.JsonFormat;
import io.swagger.v3.oas.annotations.media.Schema;

import java.time.LocalDateTime;

@Schema(description = "펀딩 새소식 응답")
public record FundingNewsItemDto(
        Long id,
        String artistNickname,
        String title,
        String content,
        String imageUrl,
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
                u.getCreateDate()
        );
    }
}