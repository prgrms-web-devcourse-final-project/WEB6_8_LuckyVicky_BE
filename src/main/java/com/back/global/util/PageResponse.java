package com.back.global.util;

import lombok.AllArgsConstructor;
import lombok.Getter;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

/**
 * 페이징 응답 공통 클래스
 * 
 * API 명세에 맞는 구조로 구현
 * 기본 페이지 사이즈: 10개 (원본 명세는 20개였지만 요청에 따라 10개로 설정)
 */
@Getter
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
     * 
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
     * 
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
     * 
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
     * 
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
     * 
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
}
