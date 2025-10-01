package com.back.domain.funding.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
@Builder
@Table(name = "funding_options")
public class FundingOption extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "funding_id", nullable = false)
    private Funding funding;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private long price;

    @Column
    private Integer stock;

    @Column
    @Builder.Default
    private int sortOrder = 0;

    public void attachTo(Funding funding) {
        this.funding = funding;
    }

    public void decreaseStock(int qty) {
        if (stock == null) return;
        this.stock = this.stock - qty;
    }

    public static FundingOption create(String name, long price, Integer stock, Integer sortOrder) {
        if (name == null || name.isBlank()) throw new IllegalArgumentException("옵션명은 필수입니다.");
        if (price <= 0) throw new IllegalArgumentException("옵션 가격은 0보다 커야 합니다.");
        if (stock == null || stock <= 0) throw new IllegalArgumentException("옵션 재고는 0보다 커야 합니다.");
        if (sortOrder == null) throw new IllegalArgumentException("옵션 정렬 순서는 필수입니다.");
        return FundingOption.builder()
                .name(name)
                .price(price)
                .stock(stock)
                .sortOrder(sortOrder)
                .build();
    }
}
