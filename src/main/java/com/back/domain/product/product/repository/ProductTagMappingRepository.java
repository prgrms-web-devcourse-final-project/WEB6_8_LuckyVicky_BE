package com.back.domain.product.product.repository;

import com.back.domain.product.product.entity.ProductTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.transaction.annotation.Transactional;
import com.back.domain.product.tag.entity.Tag;

public interface ProductTagMappingRepository extends JpaRepository<ProductTagMapping, Long> {
    boolean existsByTag(Tag tag); // 특정 태그가 매핑 테이블에 존재하는지 체크
    @Modifying
    @Query("DELETE FROM ProductTagMapping ptm WHERE ptm.product.id = :productId")
    @Transactional
    void deleteAllByProductId(@Param("productId") Long productId);
}


