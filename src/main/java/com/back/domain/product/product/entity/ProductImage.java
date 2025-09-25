package com.back.domain.product.product.entity;

import com.back.global.jpa.entity.BaseEntity;
import com.back.global.s3.FileType;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Table(name = "product_image")
public class ProductImage extends BaseEntity {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private Product product; // 상품 FK

    @Column(nullable = false)
    private String productImageUrl; // S3에 업로드된 이미지 URL

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType imageType; // 이미지 종류 (MAIN, ADDITIONAL, THUMBNAIL)

    //테스트용
    @Column(nullable = false)
    private String key;

    //테스트용
    @Column(nullable = false)
    private String originalFilename;

}
