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
@Table(name = "funding_communities")
public class FundingCommunity extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private Funding funding;

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    private User author;

    @Column(nullable = false)
    private String content;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    private LocalDateTime deletedAt;

    public void attachTo(Funding funding) { this.funding = funding; }

    public void markAsDeleted() {
        this.deleted = true;
        this.deletedAt = LocalDateTime.now();
    }
}
