package com.back.domain.product.product.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.domain.product.tag.entity.Tag;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_tag_mapping", // 상품-태그 중간 테이블(N:M 관계라서 중간테이블이 필요함)
        uniqueConstraints = {@UniqueConstraint(columnNames = {"product_id", "tag_id"})}) // 하나의 상품에 같은 태그 중복 방지를 위해.
public class ProductTagMapping extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 FK

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "tag_id", nullable = false)
    private Tag tag; // 태그 FK
}
