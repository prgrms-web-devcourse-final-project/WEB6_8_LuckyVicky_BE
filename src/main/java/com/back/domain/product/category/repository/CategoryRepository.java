package com.back.domain.product.category.repository;

import com.back.domain.product.category.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface CategoryRepository extends JpaRepository<Category, Long> {
    /**
     * parend_id(상위 카테고리)가 parentId인 하위 카테고리들 조회
     * @param parentId 상위 카테고리 ID
     * @return 하위 카테고리 리스트
     */
    List<Category> findAllByParentId(Long parentId);

    /** 상위 카테고리들 조회 */
    List<Category> findAllByParentIdIsNull();
    /** 카테고리 생성/수정 시 중복 방지용 */
    boolean existsByCategoryNameAndParent(String categoryName, Category parent);
}
