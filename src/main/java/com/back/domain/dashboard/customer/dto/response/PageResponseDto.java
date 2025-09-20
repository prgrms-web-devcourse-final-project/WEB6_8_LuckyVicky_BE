package com.back.domain.dashboard.customer.dto.response;

import com.back.global.util.PageResponse;
import lombok.NoArgsConstructor;

/**
 * 대시보드 전용 페이징 응답 DTO
 * 공용 PageResponse를 상속받아 사용
 */
@NoArgsConstructor
public class PageResponseDto<T> extends PageResponse<T> {
    
    public PageResponseDto(java.util.List<T> content, int page, int size, 
                          long totalElements, int totalPages, boolean hasNext, boolean hasPrevious) {
        super(content, page, size, totalElements, totalPages, hasNext, hasPrevious);
    }
}
