package com.back.domain.product.category.service;

import com.back.domain.product.category.dto.request.CategoryRequest;
import com.back.domain.product.category.dto.response.CategoryResponse;
import com.back.domain.product.category.entity.Category;
import com.back.domain.product.category.repository.CategoryRepository;
import com.back.domain.product.product.repository.ProductRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class CategoryService {
    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    /** 카테고리 조회 (상위+하위 전체)*/
    @Transactional(readOnly = true)
    public List<CategoryResponse> getAllCategories() {
        List<Category> topCategories = categoryRepository.findAllByParentIdIsNull(); // 상위 카테고리 조회

        return topCategories.stream()
                .map(this::convertToResponse)
                .toList(); // 하위 카테고리까지 포함된 DTO 반환
    }

    /** 카테고리 등록 */
    @Transactional
    public CategoryResponse createCategory(CategoryRequest request) {
        Category parent = null;
        if (request.parentId() != null) { // 상위 카테고리 지정이 있으면 DB에서 상위 카테고리 조회
            parent = categoryRepository.findById(request.parentId())
                    .orElseThrow(() -> {
                        log.error("상위 카테고리 없음: parentId={}", request.parentId());
                        return new ServiceException("404", "상위 카테고리를 찾을 수 없습니다.");
                    });
        }

        // 카테고리명 중복 체크
        boolean exists = categoryRepository.existsByCategoryNameAndParent(request.categoryName(), parent);
        if (exists) {
            log.error("중복 카테고리로 생성 실패: name={}, parentId={}", request.categoryName(), request.parentId());
            throw new ServiceException("400", "동일한 이름의 카테고리가 이미 존재합니다.");
        }

        Category category = Category.builder()
                .categoryName(request.categoryName())
                .parent(parent)
                .build();

        categoryRepository.save(category); // DB 저장
        return new CategoryResponse(category.getId(), category.getCategoryName(), List.of());
    }

    /** 카테고리 수정 */
    @Transactional
    public CategoryResponse updateCategory(Long id, CategoryRequest request) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("수정할 카테고리를 찾을 수  없음: id={}", id);
                    return new ServiceException("404", "카테고리를 찾을 수 없습니다.");
                });

        category.setCategoryName(request.categoryName());
        // 카테고리명 중복 체크
        if (categoryRepository.existsByCategoryNameAndParent(request.categoryName(), category.getParent())
                && !category.getCategoryName().equals(request.categoryName())) {
            log.warn("중복 카테고리로 수정 실패: name={}, parentId={}", request.categoryName(), category.getParent() == null ? null : category.getParent().getId());
            throw new ServiceException("400", "동일한 이름의 카테고리가 이미 존재합니다.");
        }

        categoryRepository.save(category);

        return new CategoryResponse(category.getId(), category.getCategoryName(), List.of());
    }

    /** 카테고리 삭제 (이때, 하위 카테고리나 속한 상품 존재 시 삭제 불가) */
    @Transactional
    public void deleteCategory(Long id) {
        Category category = categoryRepository.findById(id)
                .orElseThrow(() -> {
                    log.error("삭제할 카테고리 없음: id={}", id);
                    return new ServiceException("404", "카테고리를 찾을 수 없습니다.");
                });

        if (!category.getSubCategories().isEmpty()) {
            log.error("하위 카테고리 존재하여 삭제 불가: id={}", id);
            throw new ServiceException("400", "하위 카테고리가 존재하여 삭제할 수 없습니다.");
        }

        if (productRepository.existsByCategoryId(id)) {
            log.error("카테고리에 속한 상품 존재하여 삭제 불가: id={}", id);
            throw new ServiceException("400", "해당 카테고리에 속한 상품이 있어 삭제할 수 없습니다.");
        }

        categoryRepository.delete(category);
    }


    /** Entity → DTO 변환 (재귀적으로 하위 카테고리 포함) */
    private CategoryResponse convertToResponse(Category category) {
        List<CategoryResponse> subResponses = category.getSubCategories().stream()
                // 하위 카테고리로 재귀적으로 DTO로 변환 (카테고리 구조가 확장된다면 하위의 하위 카테고리까지도 포함될 수 있음)
                .map(this::convertToResponse)
                .toList();

        return new CategoryResponse(
                category.getId(),
                category.getCategoryName(),
                subResponses
        );
    }

}
