package com.back.domain.faq.service;

import com.back.domain.faq.dto.request.FaqCreateRequest;
import com.back.domain.faq.dto.request.FaqUpdateRequest;
import com.back.domain.faq.dto.response.FaqDetailResponse;
import com.back.domain.faq.dto.response.FaqListResponse;
import com.back.domain.faq.entity.Faq;
import com.back.domain.faq.entity.FaqCategory;
import com.back.domain.faq.repository.FaqRepository;
import com.back.global.exception.ServiceException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * FAQ 서비스
 */
@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FaqService {

    private final FaqRepository faqRepository;

    /**
     * FAQ 생성
     */
    @Transactional
    public Long createFaq(FaqCreateRequest request) {
        Faq faq = Faq.builder()
                .question(request.question())
                .answer(request.answer())
                .category(request.category())
                .build();

        Faq savedFaq = faqRepository.save(faq);

        log.info("FAQ 생성 완료 - faqId: {}", savedFaq.getId());

        return savedFaq.getId();
    }

    /**
     * FAQ 목록 조회
     */
    public FaqListResponse getFaqs(FaqCategory category, Pageable pageable) {
        Page<Faq> faqPage;

        if (category != null) {
            // 카테고리별 조회
            faqPage = faqRepository.findByCategoryOrderByCreateDateDesc(category, pageable);
            log.info("카테고리 필터링 조회 - category: {}", category);
        } else {
            // 전체 조회
            faqPage = faqRepository.findAllByOrderByCreateDateDesc(pageable);
            log.info("전체 FAQ 조회");
        }

        log.info("FAQ 목록 조회 완료 - 조회된 개수: {}, 전체 개수: {}",
                faqPage.getNumberOfElements(), faqPage.getTotalElements());

        return FaqListResponse.from(faqPage);
    }

    /**
     * FAQ 상세 조회
     */
    @Transactional
    public FaqDetailResponse getFaq(Long faqId) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> {
                    log.error("FAQ 조회 실패 - 존재하지 않는 FAQ ID: {}", faqId);
                    return new ServiceException("404", "존재하지 않는 FAQ입니다.");
                });

        // 조회수 증가
        faq.increaseViewCount();
        log.info("FAQ 조회수 증가 - faqId: {}, viewCount: {}", faqId, faq.getViewCount());

        return FaqDetailResponse.from(faq);
    }

    /**
     * FAQ 수정 (관리자 전용)
     */
    @Transactional
    public void updateFaq(Long faqId, FaqUpdateRequest request) {
        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> {
                    log.error("FAQ 수정 실패 - 존재하지 않는 FAQ ID: {}", faqId);
                    return new ServiceException("404", "존재하지 않는 FAQ입니다.");
                });

        // 수정 (null이 아닌 값만 업데이트)
        faq.update(
                request.question(),
                request.answer(),
                request.category()
        );

        log.info("FAQ 수정 완료 - faqId: {}", faqId);
    }

    /**
     * FAQ 삭제 (관리자 전용)
     */
    @Transactional
    public void deleteFaq(Long faqId) {
        log.info("FAQ 삭제 요청 - faqId: {}", faqId);

        Faq faq = faqRepository.findById(faqId)
                .orElseThrow(() -> {
                    log.error("FAQ 삭제 실패 - 존재하지 않는 FAQ ID: {}", faqId);
                    return new ServiceException("404", "존재하지 않는 FAQ입니다.");
                });

        faqRepository.delete(faq);
        log.info("FAQ 삭제 완료 - faqId: {}", faqId);
    }

}
