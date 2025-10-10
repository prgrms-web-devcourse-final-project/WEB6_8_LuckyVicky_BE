package com.back.domain.support.faq.service;


import com.back.domain.support.faq.dto.request.FaqCreateRequest;
import com.back.domain.support.faq.dto.request.FaqUpdateRequest;
import com.back.domain.support.faq.dto.response.FaqDetailResponse;
import com.back.domain.support.faq.dto.response.FaqListResponse;
import com.back.domain.support.faq.entity.Faq;
import com.back.domain.support.faq.entity.FaqCategory;
import com.back.domain.support.faq.repository.FaqRepository;
import com.back.global.exception.ServiceException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("FaqService 단위 테스트")
public class FaqServiceTest {

    @Mock
    private FaqRepository faqRepository;

    @InjectMocks
    private FaqService faqService;

    private Faq testFaq;
    private FaqCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 Faq 생성
        testFaq = Faq.builder()
                .question("회원가입은 어떻게 하나요?")
                .answer("회원가입 버튼을 클릭하여 이메일 또는 소셜 계정으로 가입할 수 있습니다.")
                .category(FaqCategory.ACCOUNT)
                .build();
        ReflectionTestUtils.setField(testFaq, "id", 1L);

        // 테스트용 CreateRequest 생성
        validCreateRequest = new FaqCreateRequest(
                "배송은 얼마나 걸리나요?",
                "주문 후 2-3일 내 배송됩니다.",
                FaqCategory.DELIVERY
        );
    }

    @Nested
    @DisplayName("FAQ 생성 테스트")
    class CreateFaqTest {

        @Test
        @DisplayName("FAQ 생성 성공")
        void createFaq_Success() {
            // given
            Faq savedFaq = Faq.builder()
                    .question(validCreateRequest.question())
                    .answer(validCreateRequest.answer())
                    .category(validCreateRequest.category())
                    .build();
            ReflectionTestUtils.setField(savedFaq, "id", 1L);

            given(faqRepository.save(any(Faq.class))).willReturn(savedFaq);

            // when
            Long faqId = faqService.createFaq(validCreateRequest);

            // then
            assertThat(faqId).isEqualTo(1L);
            verify(faqRepository).save(any(Faq.class));
        }
    }

    @Nested
    @DisplayName("FAQ 목록 조회 테스트")
    class GetFaqsTest {

        @Test
        @DisplayName("FAQ 목록 조회 성공 (전체)")
        void getFaqs_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Faq> faqs = List.of(testFaq);
            Page<Faq> faqPage = new PageImpl<>(faqs, pageable, 1);

            given(faqRepository.findAllByOrderByCreateDateDesc(pageable))
                    .willReturn(faqPage);

            // when
            FaqListResponse response = faqService.getFaqs(null, pageable);

            // then
            assertThat(response.faqs()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
            assertThat(response.currentPage()).isEqualTo(1);
            verify(faqRepository).findAllByOrderByCreateDateDesc(pageable);
        }

        @Test
        @DisplayName("FAQ 목록 조회 성공 (카테고리 필터)")
        void getFaqs_CategoryFilter_Success() {
            // given
            FaqCategory category = FaqCategory.ACCOUNT;
            Pageable pageable = PageRequest.of(0, 10);
            List<Faq> faqs = List.of(testFaq);
            Page<Faq> faqPage = new PageImpl<>(faqs, pageable, 1);

            given(faqRepository.findByCategoryOrderByCreateDateDesc(category, pageable))
                    .willReturn(faqPage);

            // when
            FaqListResponse response = faqService.getFaqs(category, pageable);

            // then
            assertThat(response.faqs()).hasSize(1);
            verify(faqRepository).findByCategoryOrderByCreateDateDesc(category, pageable);
        }

        @Test
        @DisplayName("FAQ 목록 조회 - 빈 목록")
        void getFaqs_EmptyList() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Faq> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

            given(faqRepository.findAllByOrderByCreateDateDesc(pageable))
                    .willReturn(emptyPage);

            // when
            FaqListResponse response = faqService.getFaqs(null, pageable);

            // then
            assertThat(response.faqs()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("FAQ 상세 조회 테스트")
    class GetFaqTest {

        @Test
        @DisplayName("FAQ 상세 조회 성공")
        void getFaq_Success() {
            // given
            given(faqRepository.findById(1L))
                    .willReturn(Optional.of(testFaq));

            Long initialViewCount = testFaq.getViewCount();

            // when
            FaqDetailResponse response = faqService.getFaq(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.question()).isEqualTo("회원가입은 어떻게 하나요?");
            assertThat(response.viewCount()).isEqualTo(initialViewCount + 1);
            verify(faqRepository).findById(1L);
        }

        @Test
        @DisplayName("FAQ 상세 조회 실패 - 존재하지 않는 FAQ")
        void getFaq_NotFound() {
            // given
            given(faqRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> faqService.getFaq(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 FAQ입니다.");
                    });
        }
    }

    @Nested
    @DisplayName("FAQ 수정 테스트")
    class UpdateFaqTest {

        @Test
        @DisplayName("FAQ 수정 성공")
        void updateFaq_Success() {
            // given
            FaqUpdateRequest updateRequest = new FaqUpdateRequest(
                    "수정된 질문",
                    "수정된 답변",
                    FaqCategory.ORDER_PAYMENT
            );

            given(faqRepository.findById(1L))
                    .willReturn(Optional.of(testFaq));

            // when
            faqService.updateFaq(1L, updateRequest);

            // then
            assertThat(testFaq.getQuestion()).isEqualTo("수정된 질문");
            assertThat(testFaq.getAnswer()).isEqualTo("수정된 답변");
            assertThat(testFaq.getCategory()).isEqualTo(FaqCategory.ORDER_PAYMENT);
            verify(faqRepository).findById(1L);
        }

        @Test
        @DisplayName("FAQ 수정 실패 - 존재하지 않는 FAQ")
        void updateFaq_NotFound() {
            // given
            FaqUpdateRequest updateRequest = new FaqUpdateRequest(
                    "수정된 질문",
                    "수정된 답변",
                    FaqCategory.ACCOUNT
            );

            given(faqRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> faqService.updateFaq(999L, updateRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 FAQ입니다.");
                    });
        }
    }

    @Nested
    @DisplayName("FAQ 삭제 테스트")
    class DeleteFaqTest {

        @Test
        @DisplayName("FAQ 삭제 성공")
        void deleteFaq_Success() {
            // given
            given(faqRepository.findById(1L))
                    .willReturn(Optional.of(testFaq));

            // when
            faqService.deleteFaq(1L);

            // then
            verify(faqRepository).findById(1L);
            verify(faqRepository).delete(testFaq);
        }

        @Test
        @DisplayName("FAQ 삭제 실패 - 존재하지 않는 FAQ")
        void deleteFaq_NotFound() {
            // given
            given(faqRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> faqService.deleteFaq(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 FAQ입니다.");
                    });
        }
    }
}
