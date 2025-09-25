package com.back.domain.product.product.entity;

import com.back.global.jpa.entity.FileBaseEntity;
import com.back.global.s3.FileType;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "product_images")
public class ProductImage extends FileBaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 FK

    public ProductImage(Product product, String fileUrl, FileType fileType, String key, String originalFilename) {
        super(fileUrl, fileType, key, originalFilename);
        this.product = product;
    }

}
