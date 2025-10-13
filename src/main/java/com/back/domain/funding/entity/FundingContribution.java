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
    private User buyer;

    @Column(nullable = false)
    private int quantity;

    @Column(nullable = false)
    private long price;

    @Column(nullable = false)
    private long totalAmount;

    @Column(nullable = false)
    private LocalDateTime paidAt;

    public static FundingContribution create(Funding funding, int qty, User buyer, long price, LocalDateTime paidAt) {

        return FundingContribution.builder()
                .funding(funding)
                .buyer(buyer)
                .quantity(qty)
                .price(price)
                .paidAt(paidAt)
                .build();
    }
}
