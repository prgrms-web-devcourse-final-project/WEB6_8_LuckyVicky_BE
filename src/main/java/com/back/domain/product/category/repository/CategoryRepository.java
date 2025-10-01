package com.back.domain.product.category.repository;

import com.back.domain.product.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * parend_id가 상위 카테고리인(해당 상위 카테고리를 부모로 가진) 하위 카테고리들을 찾는 메서드
     * @param parentId 상위 카테고리 ID
     * @return 하위 카테고리 리스트
     */
    List<Category> findAllByParentId(Long parentId);
}
