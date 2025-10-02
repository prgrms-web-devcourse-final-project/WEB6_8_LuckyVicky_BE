package com.back.domain.product.product.repository;

import com.back.domain.product.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductCustomRepository {
    Optional<Product> findByProductUuid(UUID productUuid);
}
