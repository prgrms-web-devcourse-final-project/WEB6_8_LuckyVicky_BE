package com.back.domain.funding.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "funding_contributions")
public class FundingContribution extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Funding funding;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private FundingOption option;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User buyer;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long unitPrice;

    @Column(nullable = false)
    private long totalAmount;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    public static FundingContribution create(Funding funding, FundingOption option, int qty, User buyer, LocalDateTime paidAt) {
        long unit = option.getPrice();
        long total = unit * qty;
        return FundingContribution.builder()
                .funding(funding)
                .option(option)
                .buyer(buyer)
                .quantity(qty)
                .unitPrice(unit)
                .totalAmount(total)
                .paidAt(paidAt)
                .build();
    }
}
