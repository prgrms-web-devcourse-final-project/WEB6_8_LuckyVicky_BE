package com.back.domain.support.inquiry.service;

import com.back.domain.support.inquiry.dto.request.InquiryCreateRequest;
import com.back.domain.support.inquiry.dto.request.InquiryReplyRequest;
import com.back.domain.support.inquiry.dto.request.InquiryUpdateRequest;
import com.back.domain.support.inquiry.dto.response.InquiryDetailResponse;
import com.back.domain.support.inquiry.dto.response.InquiryListResponse;
import com.back.domain.support.inquiry.entity.*;
import com.back.domain.support.inquiry.repository.InquiryDocumentRepository;
import com.back.domain.support.inquiry.repository.InquiryReplyRepository;
import com.back.domain.support.inquiry.repository.InquiryRepository;
import com.back.domain.user.entity.User;
import com.back.global.exception.ServiceException;
import com.back.global.s3.S3Service;
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
import static org.mockito.Mockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("InquiryService 단위 테스트")
public class InquiryServiceTest {

    @Mock
    private InquiryRepository inquiryRepository;
    @Mock
    private InquiryDocumentRepository inquiryDocumentRepository;
    @Mock
    private InquiryReplyRepository inquiryReplyRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private InquiryService inquiryService;

    private User testUser;
    private Inquiry testInquiry;
    private InquiryCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = User.createLocalUser("test@test.com", "encodedPassword", "테스트유저", "010-1234-5678");
        ReflectionTestUtils.setField(testUser, "id", 1L);

        // 테스트용 Inquiry 생성
        testInquiry = Inquiry.builder()
                .user(testUser)
                .title("배송 문의")
                .content("언제 배송되나요?")
                .category(InquiryCategory.DELIVERY)
                .isSecret(false)
                .build();
        ReflectionTestUtils.setField(testInquiry, "id", 1L);

        // 테스트용 CreateRequest 생성
        validCreateRequest = new InquiryCreateRequest(
                InquiryCategory.PRODUCT,
                "상품 문의",
                "상품 재고 문의드립니다.",
                false,
                null
        );
    }

    @Nested
    @DisplayName("문의 생성 테스트")
    class CreateInquiryTest {

        @Test
        @DisplayName("문의 생성 성공")
        void createInquiry_Success() {
            // given
            Inquiry savedInquiry = Inquiry.builder()
                    .user(testUser)
                    .title(validCreateRequest.title())
                    .content(validCreateRequest.content())
                    .category(validCreateRequest.category())
                    .isSecret(validCreateRequest.isSecret())
                    .build();
            ReflectionTestUtils.setField(savedInquiry, "id", 1L);

            given(inquiryRepository.save(any(Inquiry.class))).willReturn(savedInquiry);

            // when
            Long inquiryId = inquiryService.createInquiry(validCreateRequest, testUser);

            // then
            assertThat(inquiryId).isEqualTo(1L);
            verify(inquiryRepository).save(any(Inquiry.class));
        }
    }

    @Nested
    @DisplayName("문의 목록 조회 테스트")
    class GetInquiriesTest {

        @Test
        @DisplayName("내 문의 목록 조회 성공")
        void getMyInquiries_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Inquiry> inquiries = List.of(testInquiry);
            Page<Inquiry> inquiryPage = new PageImpl<>(inquiries, pageable, 1);

            given(inquiryRepository.findByUserOrderByCreateDateDesc(testUser, pageable))
                    .willReturn(inquiryPage);

            // when
            InquiryListResponse response = inquiryService.getMyInquiries(testUser, pageable);

            // then
            assertThat(response.inquiries()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
            verify(inquiryRepository).findByUserOrderByCreateDateDesc(testUser, pageable);
        }

        @Test
        @DisplayName("공개 문의 목록 조회 성공")
        void getPublicInquiries_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Inquiry> inquiries = List.of(testInquiry);
            Page<Inquiry> inquiryPage = new PageImpl<>(inquiries, pageable, 1);

            given(inquiryRepository.findPublicInquiries(pageable))
                    .willReturn(inquiryPage);

            // when
            InquiryListResponse response = inquiryService.getPublicInquiries(pageable);

            // then
            assertThat(response.inquiries()).hasSize(1);
            verify(inquiryRepository).findPublicInquiries(pageable);
        }

        @Test
        @DisplayName("문의 목록 조회 - 빈 목록")
        void getInquiries_EmptyList() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Inquiry> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

            given(inquiryRepository.findPublicInquiries(pageable))
                    .willReturn(emptyPage);

            // when
            InquiryListResponse response = inquiryService.getPublicInquiries(pageable);

            // then
            assertThat(response.inquiries()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("문의 상세 조회 테스트")
    class GetInquiryTest {

        @Test
        @DisplayName("문의 상세 조회 성공")
        void getInquiry_Success() {
            // given
            given(inquiryRepository.findByIdWithDetails(1L))
                    .willReturn(Optional.of(testInquiry));

            Long initialViewCount = testInquiry.getViewCount();

            // when
            InquiryDetailResponse response = inquiryService.getInquiry(1L, testUser.getId(), false);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("배송 문의");
            assertThat(response.viewCount()).isEqualTo(initialViewCount + 1);
            verify(inquiryRepository).findByIdWithDetails(1L);
        }

        @Test
        @DisplayName("문의 상세 조회 실패 - 존재하지 않는 문의")
        void getInquiry_NotFound() {
            // given
            given(inquiryRepository.findByIdWithDetails(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inquiryService.getInquiry(999L, testUser.getId(), false))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 문의입니다.");
                    });
        }

        @Test
        @DisplayName("비밀문의 조회 실패 - 권한 없음")
        void getInquiry_Forbidden() {
            // given
            Inquiry secretInquiry = Inquiry.builder()
                    .user(testUser)
                    .title("비밀 문의")
                    .content("비밀 내용")
                    .category(InquiryCategory.PAYMENT)
                    .isSecret(true)
                    .build();
            ReflectionTestUtils.setField(secretInquiry, "id", 2L);

            given(inquiryRepository.findByIdWithDetails(2L))
                    .willReturn(Optional.of(secretInquiry));

            // when & then - 다른 사용자가 조회 시도
            assertThatThrownBy(() -> inquiryService.getInquiry(2L, 999L, false))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("403");
                    });
        }
    }

    @Nested
    @DisplayName("문의 수정 테스트")
    class UpdateInquiryTest {

        @Test
        @DisplayName("문의 수정 성공")
        void updateInquiry_Success() {
            // given
            InquiryUpdateRequest updateRequest = new InquiryUpdateRequest(
                    InquiryCategory.EXCHANGE_REFUND,
                    "수정된 제목",
                    "수정된 내용",
                    true,
                    null,
                    null
            );

            given(inquiryRepository.findByIdWithDetails(1L))
                    .willReturn(Optional.of(testInquiry));

            // when
            inquiryService.updateInquiry(1L, updateRequest, testUser.getId());

            // then
            assertThat(testInquiry.getTitle()).isEqualTo("수정된 제목");
            assertThat(testInquiry.getContent()).isEqualTo("수정된 내용");
            assertThat(testInquiry.getCategory()).isEqualTo(InquiryCategory.EXCHANGE_REFUND);
            verify(inquiryRepository).findByIdWithDetails(1L);
        }

        @Test
        @DisplayName("문의 수정 실패 - 권한 없음")
        void updateInquiry_Forbidden() {
            // given
            InquiryUpdateRequest updateRequest = new InquiryUpdateRequest(
                    InquiryCategory.EXCHANGE_REFUND,
                    "수정된 제목",
                    "수정된 내용",
                    false,
                    null,
                    null
            );

            given(inquiryRepository.findByIdWithDetails(1L))
                    .willReturn(Optional.of(testInquiry));

            // when & then - 다른 사용자가 수정 시도
            assertThatThrownBy(() -> inquiryService.updateInquiry(1L, updateRequest, 999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("403");
                    });
        }
    }

    @Nested
    @DisplayName("문의 삭제 테스트")
    class DeleteInquiryTest {

        @Test
        @DisplayName("문의 삭제 성공")
        void deleteInquiry_Success() {
            // given
            given(inquiryRepository.findByIdWithDetails(1L))
                    .willReturn(Optional.of(testInquiry));

            // when
            inquiryService.deleteInquiry(1L, testUser.getId(), false);

            // then
            verify(inquiryRepository).findByIdWithDetails(1L);
            verify(inquiryRepository).delete(testInquiry);
        }

        @Test
        @DisplayName("문의 삭제 실패 - 존재하지 않는 문의")
        void deleteInquiry_NotFound() {
            // given
            given(inquiryRepository.findByIdWithDetails(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> inquiryService.deleteInquiry(999L, testUser.getId(), false))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                    });
        }
    }

    @Nested
    @DisplayName("댓글 작성 테스트")
    class CreateReplyTest {

        @Test
        @DisplayName("댓글 작성 성공")
        void createReply_Success() {
            // given
            InquiryReplyRequest replyRequest = new InquiryReplyRequest(
                    "답변드립니다.",
                    null
            );

            InquiryReply savedReply = InquiryReply.builder()
                    .inquiry(testInquiry)
                    .user(testUser)
                    .content(replyRequest.content())
                    .replyType(ReplyType.USER)
                    .build();
            ReflectionTestUtils.setField(savedReply, "id", 1L);

            given(inquiryRepository.findById(1L))
                    .willReturn(Optional.of(testInquiry));
            given(inquiryReplyRepository.save(any(InquiryReply.class)))
                    .willReturn(savedReply);

            // when
            Long replyId = inquiryService.createReply(1L, replyRequest, testUser, false);

            // then
            assertThat(replyId).isEqualTo(1L);
            verify(inquiryReplyRepository).save(any(InquiryReply.class));
        }

        @Test
        @DisplayName("관리자 답변 시 상태 변경")
        void createReply_AdminReply_StatusChanged() {
            // given
            InquiryReplyRequest replyRequest = new InquiryReplyRequest(
                    "관리자 답변입니다.",
                    null
            );

            InquiryReply savedReply = InquiryReply.builder()
                    .inquiry(testInquiry)
                    .user(testUser)
                    .content(replyRequest.content())
                    .replyType(ReplyType.ADMIN)
                    .build();
            ReflectionTestUtils.setField(savedReply, "id", 1L);

            given(inquiryRepository.findById(1L))
                    .willReturn(Optional.of(testInquiry));
            given(inquiryReplyRepository.save(any(InquiryReply.class)))
                    .willReturn(savedReply);

            // when
            inquiryService.createReply(1L, replyRequest, testUser, true);

            // then
            assertThat(testInquiry.getStatus()).isEqualTo(InquiryStatus.ANSWERED);
        }
    }
}