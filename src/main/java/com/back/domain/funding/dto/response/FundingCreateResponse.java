package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.Funding;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;

public record FundingCreateResponse(
        Long fundingId,
        String title,
        String description,
        String categoryName,
        String imageUrl,
        long targetAmount,
        long price,
        Integer stock,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endDate
) {
    public static FundingCreateResponse from(Funding funding) {
        return new FundingCreateResponse(
                funding.getId(),
                funding.getTitle(),
                funding.getDescription(),
                funding.getCategory().getCategoryName(),
                funding.getImageUrl(),
                funding.getTargetAmount(),
                funding.getPrice(),
                funding.getStock(),
                funding.getStartDate(),
                funding.getEndDate()
        );
    }
}
