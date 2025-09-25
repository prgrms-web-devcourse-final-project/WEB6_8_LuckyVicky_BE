package com.back.domain.product.product.repository;

import com.back.domain.product.product.entity.AdditionalProduct;
import org.springframework.data.jpa.repository.JpaRepository;

public interface AdditionalProductRepository extends JpaRepository<AdditionalProduct, Long> {
}
