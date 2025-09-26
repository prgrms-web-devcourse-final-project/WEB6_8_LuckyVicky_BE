package com.back.domain.product.product.repository;

import com.back.domain.product.product.entity.ProductTagMapping;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductTagMappingRepository extends JpaRepository<ProductTagMapping, Long> {
}
