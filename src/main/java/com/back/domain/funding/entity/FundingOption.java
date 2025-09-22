package com.back.domain.funding.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
public class FundingOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funding_id", nullable = false)
    private Funding funding;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column
    private Integer availableQuantity;

    @Column
    @Builder.Default
    private int sortOrder = 0;

    public void attachTo(Funding funding) {
        this.funding = funding;
    }

    public void decreaseStock(int qty) {
        if (availableQuantity == null) return;
        this.availableQuantity = this.availableQuantity - qty;
    }
}
