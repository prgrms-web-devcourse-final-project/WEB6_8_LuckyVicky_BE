package com.back.domain.product.product.entity;

import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@Table(name = "additional_product")
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class AdditionalProduct extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 FK

    @Column(nullable = false)
    private String additionalName; // 추가상품명

    @Column(nullable = false)
    private int additionalPrice; // 추가상품 가격

    @Column(nullable = false)
    private int additionalStock; // 추가상품 재고
}
