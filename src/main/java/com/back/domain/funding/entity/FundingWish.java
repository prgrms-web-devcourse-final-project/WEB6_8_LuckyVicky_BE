package com.back.domain.funding.entity;

import com.back.domain.user.entity.User;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Table(name = "funding_wishes", uniqueConstraints = {@UniqueConstraint(name = "uk_funding_wish_user_funding", columnNames = {"user_id", "funding_id"})},
        indexes = {@Index(name = "idx_funding_wish_user", columnList = "user_id"), @Index(name = "idx_funding_wish_funding", columnList = "funding_id")})
public class FundingWish extends BaseEntity {
    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funding_id", nullable = false)
    private Funding funding;

    public static FundingWish create(User user, Funding funding) {
        return FundingWish.builder()
                .user(user)
                .funding(funding)
                .build();
    }
}
