package com.back.domain.product.qna.repository;

import com.back.domain.product.product.entity.Product;
import com.back.domain.product.qna.entity.ProductQna;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface ProductQnaRepository extends JpaRepository<ProductQna, Long> {
    Page<ProductQna> findByProduct(Product product, Pageable pageable);
}

