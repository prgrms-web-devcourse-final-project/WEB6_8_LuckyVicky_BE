package com.back.domain.funding.dto.response;

import com.back.domain.funding.entity.Funding;
import com.back.domain.funding.entity.FundingOption;
import com.fasterxml.jackson.annotation.JsonFormat;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

public record FundingCreateResponse(
        Long fundingId,
        String title,
        String description,
        String imageUrl,
        long targetAmount,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime startDate,
        @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd HH:mm:ss")
        LocalDateTime endDate,
        List<FundingOptionResponse> options
) {
    public static FundingCreateResponse from(Funding funding) {
        return new FundingCreateResponse(
                funding.getId(),
                funding.getTitle(),
                funding.getDescription(),
                funding.getImageUrl(),
                funding.getTargetAmount(),
                funding.getStartDate(),
                funding.getEndDate(),
                funding.getOptions().stream()
                        .map(FundingOptionResponse::from)
                        .collect(Collectors.toList())
        );
    }

    public record FundingOptionResponse(
            Long id,
            String name,
            long price,
            Integer stock,
            Integer sortOrder
    ) {
        public static FundingOptionResponse from(FundingOption option) {
            return new FundingOptionResponse(
                    option.getId(),
                    option.getName(),
                    option.getPrice(),
                    option.getStock(),
                    option.getSortOrder()
            );
        }
    }
}
