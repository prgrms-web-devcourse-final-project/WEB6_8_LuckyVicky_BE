package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.FundingCommunity;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record FundingCommunityItemDto(
        Long id,
        String writerName,
        String profileImageUrl,
        String content,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime createDate
) {
    public FundingCommunityItemDto(FundingCommunity c) {
        this(
                c.getId(),
                c.getAuthor().getName(),
                c.getAuthor().getProfileImageUrl(),
                c.getContent(),
                c.getCreateDate()
        );
    }
}