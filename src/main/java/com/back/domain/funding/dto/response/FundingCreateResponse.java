package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.Funding;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FundingCreateResponse(
        Long fundingId,
        String title,
        String description,
        String categoryName,
        List<FundingDetailResponse.FundingImageResponse> images,
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
                funding.getImages().stream()
                        .map(FundingDetailResponse.FundingImageResponse::fromEntity)
                        .collect(Collectors.toList()),                funding.getTargetAmount(),
                funding.getPrice(),
                funding.getStock(),
                funding.getStartDate(),
                funding.getEndDate()
        );
    }
}
