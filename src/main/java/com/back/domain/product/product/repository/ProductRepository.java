package com.back.domain.product.product.repository;

import com.back.domain.product.product.entity.Product;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.Optional;
import java.util.UUID;

<<<<<<< HEAD
public interface ProductRepository extends JpaRepository<Product, Long>, JpaSpecificationExecutor<Product>, ProductCustomRepository {
=======

public interface ProductRepository extends JpaRepository<Product, Long>, ProductCustomRepository, JpaSpecificationExecutor<Product> {
>>>>>>> 2f4795372b442dd5b55cfd8b8cfe7ba547b36a98
    Optional<Product> findByProductUuid(UUID productUuid);
}
