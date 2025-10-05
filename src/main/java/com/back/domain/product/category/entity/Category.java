package com.back.domain.product.category.entity;

import com.back.domain.product.product.entity.Product;
import com.back.global.jpa.entity.BaseEntity;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "categories")
public class Category extends BaseEntity {

    @Column(nullable = false)
    private String categoryName; // 카테고리명

    // 상위 카테고리
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Category parent; // 상위 카테고리 id (상위 카테고리면 null. 자기참조)

    // 하위 카테고리
    @OneToMany(mappedBy = "parent", cascade = CascadeType.ALL, orphanRemoval = true) // 상위 카테고리 삭제 시 하위 카테고리들도 삭제
    private List<Category> subCategories = new ArrayList<>(); // 하위 카테고리 리스트

    @OneToMany(mappedBy = "category")
    private List<Product> products = new ArrayList<>(); // 해당 카테고리에 속한 상품들
}
