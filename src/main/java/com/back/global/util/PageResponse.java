package com.back.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

/**
 * 페이징 응답 공통 클래스
 * API 명세에 맞는 구조로 구현
 * 기본 페이지 사이즈: 10개 (1페이지당 10개 데이터)
 * 기본 정렬: 최근 날짜순 (createdAt DESC)
 * 
 * 사용 예시:
 * 1. 기본 페이징: PageResponse.createPageable(page, size)
 * 2. 기본 정렬: PageResponse.createPageableWithDefaultSort(page, size)
 * 3. 커스텀 정렬: PageResponse.createPageableWithSort(page, size, "endDate", "DESC")
 * 4. 안전한 정렬: PageResponse.validateSortField(sort, "endDate", "title", "createdAt")
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class PageResponse<T> {
    
    /** 기본 페이지 사이즈 (요청에 따라 10개로 설정) */
    public static final int DEFAULT_PAGE_SIZE = 10;
    
    /** 최대 페이지 사이즈 */
    public static final int MAX_PAGE_SIZE = 100;
    
    // API 명세에 맞는 필드명 사용
    private List<T> content;           // "content" 필드명 유지
    private int page;                  // "page" 필드명 유지  
    private int size;                  // "size" 필드명 유지
    private long totalElements;        // "totalElements" 필드명 유지
    private int totalPages;            // "totalPages" 필드명 유지
    private boolean hasNext;           // "hasNext" 필드명 유지
    private boolean hasPrevious;       // "hasPrevious" 필드명 유지

    /**
     * Spring Data Page 객체로부터 PageResponse 생성
     * @param page Spring Data Page 객체
     * @return PageResponse 객체
     */
    public static <T> PageResponse<T> from(Page<T> page) {
        return new PageResponse<>(
                page.getContent(),          // content
                page.getNumber(),           // page (0-based)
                page.getSize(),            // size
                page.getTotalElements(),    // totalElements
                page.getTotalPages(),      // totalPages  
                page.hasNext(),            // hasNext
                page.hasPrevious()         // hasPrevious
        );
    }

    /**
     * 안전한 페이지 사이즈 검증 및 조정
     * @param size 요청된 페이지 사이즈
     * @return 검증된 페이지 사이즈 (기본: 10, 최대: 100)
     */
    public static int validatePageSize(Integer size) {
        if (size == null || size <= 0) {
            return DEFAULT_PAGE_SIZE;
        }
        return Math.min(size, MAX_PAGE_SIZE);
    }

    /**
     * 안전한 페이지 번호 검증 및 조정
     * @param page 요청된 페이지 번호
     * @return 검증된 페이지 번호 (최소: 0)
     */
    public static int validatePageNumber(Integer page) {
        if (page == null || page < 0) {
            return 0;
        }
        return page;
    }

    /**
     * 기본 설정(10개씩)으로 Pageable 생성
     * @param page 페이지 번호
     * @return Pageable 객체
     */
    public static Pageable createDefaultPageable(Integer page) {
        return PageRequest.of(
                validatePageNumber(page), 
                DEFAULT_PAGE_SIZE
        );
    }

    /**
     * 커스텀 설정으로 Pageable 생성
     * @param page 페이지 번호
     * @param size 페이지 사이즈
     * @return Pageable 객체
     */
    public static Pageable createPageable(Integer page, Integer size) {
        return PageRequest.of(
                validatePageNumber(page), 
                validatePageSize(size)
        );
    }

    /**
     * 정렬을 포함한 Pageable 생성 (대시보드용)
     * 기본 정렬: 최근 날짜순 (createdAt DESC)
     * @param page 페이지 번호
     * @param size 페이지 사이즈  
     * @param sort 정렬 필드명 (null이면 기본 정렬 사용)
     * @param order 정렬 방향 ("ASC" 또는 "DESC", null이면 DESC)
     * @return Pageable 객체
     */
    public static Pageable createPageableWithSort(Integer page, Integer size, 
                                                  String sort, String order) {
        // 기본 정렬: 최근 날짜순
        String sortField = (sort != null && !sort.trim().isEmpty()) ? sort : "createdAt";
        Sort.Direction direction = "ASC".equalsIgnoreCase(order) ? 
            Sort.Direction.ASC : Sort.Direction.DESC;
        
        return PageRequest.of(
                validatePageNumber(page), 
                validatePageSize(size),
                Sort.by(direction, sortField)
        );
    }

    /**
     * 기본 정렬(최근 날짜순)로 Pageable 생성
     * @param page 페이지 번호
     * @param size 페이지 사이즈
     * @return Pageable 객체 (createdAt DESC 정렬)
     */
    public static Pageable createPageableWithDefaultSort(Integer page, Integer size) {
        return PageRequest.of(
                validatePageNumber(page), 
                validatePageSize(size),
                Sort.by(Sort.Direction.DESC, "createdAt")
        );
    }

    /**
     * 정렬 방향 검증
     * @param order 요청된 정렬 방향
     * @return 검증된 정렬 방향 ("ASC" 또는 "DESC")
     */
    public static String validateSortOrder(String order) {
        if ("ASC".equalsIgnoreCase(order)) {
            return "ASC";
        }
        return "DESC"; // 기본값: 최근순
    }

    /**
     * 정렬 필드 검증 (화이트리스트 방식)
     * @param sort 요청된 정렬 필드
     * @param allowedFields 허용된 정렬 필드 목록
     * @return 검증된 정렬 필드 (허용되지 않으면 "createdAt" 반환)
     */
    public static String validateSortField(String sort, String... allowedFields) {
        if (sort == null || sort.trim().isEmpty()) {
            return "createdAt"; // 기본값
        }
        
        for (String allowedField : allowedFields) {
            if (allowedField.equals(sort)) {
                return sort;
            }
        }
        
        return "createdAt"; // 허용되지 않은 필드면 기본값 반환
    }
}
