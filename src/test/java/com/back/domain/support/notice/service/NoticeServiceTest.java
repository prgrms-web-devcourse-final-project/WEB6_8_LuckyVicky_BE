package com.back.domain.support.notice.service;

import com.back.domain.support.notice.dto.request.NoticeCreateRequest;
import com.back.domain.support.notice.dto.request.NoticeUpdateRequest;
import com.back.domain.support.notice.dto.response.NoticeDetailResponse;
import com.back.domain.support.notice.dto.response.NoticeListResponse;
import com.back.domain.support.notice.entity.Notice;
import com.back.domain.support.notice.repository.NoticeDocumentRepository;
import com.back.domain.support.notice.repository.NoticeRepository;
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
import static org.mockito.BDDMockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("NoticeService 단위 테스트")
public class NoticeServiceTest {

    @Mock
    private NoticeRepository noticeRepository;
    @Mock
    private NoticeDocumentRepository noticeDocumentRepository;
    @Mock
    private S3Service s3Service;

    @InjectMocks
    private NoticeService noticeService;

    private Notice testNotice;
    private NoticeCreateRequest validCreateRequest;

    @BeforeEach
    void setUp() {
        // 테스트용 Notice 생성
        testNotice = Notice.builder()
                .title("테스트 공지사항")
                .content("테스트 내용입니다.")
                .isImportant(false)
                .build();
        ReflectionTestUtils.setField(testNotice, "id", 1L);

        // 테스트용 CreateRequest 생성
        validCreateRequest = new NoticeCreateRequest(
                "새로운 공지사항",
                "새로운 공지사항 내용입니다.",
                false,
                null
        );
    }

    @Nested
    @DisplayName("공지사항 생성 테스트")
    class CreateNoticeTest {

        @Test
        @DisplayName("공지사항 생성 성공")
        void createNotice_Success() {
            // given
            Notice savedNotice = Notice.builder()
                    .title(validCreateRequest.title())
                    .content(validCreateRequest.content())
                    .isImportant(validCreateRequest.isImportant())
                    .build();
            ReflectionTestUtils.setField(savedNotice, "id", 1L);

            given(noticeRepository.save(any(Notice.class))).willReturn(savedNotice);

            // when
            Long noticeId = noticeService.createNotice(validCreateRequest);

            // then
            assertThat(noticeId).isEqualTo(1L);
            verify(noticeRepository).save(any(Notice.class));
        }
    }

    @Nested
    @DisplayName("공지사항 목록 조회 테스트")
    class GetNoticesTest {

        @Test
        @DisplayName("공지사항 목록 조회 성공")
        void getNotices_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            List<Notice> notices = List.of(testNotice);
            Page<Notice> noticePage = new PageImpl<>(notices, pageable, 1);

            given(noticeRepository.findAllByOrderByIsImportantDescCreateDateDesc(pageable))
                    .willReturn(noticePage);

            // when
            NoticeListResponse response = noticeService.getNotices(null, pageable);

            // then
            assertThat(response.notices()).hasSize(1);
            assertThat(response.totalElements()).isEqualTo(1);
            assertThat(response.currentPage()).isEqualTo(0);
            verify(noticeRepository).findAllByOrderByIsImportantDescCreateDateDesc(pageable);
        }

        @Test
        @DisplayName("공지사항 검색 성공")
        void getNotices_SearchSuccess() {
            // given
            String keyword = "테스트";
            Pageable pageable = PageRequest.of(0, 10);
            List<Notice> notices = List.of(testNotice);
            Page<Notice> noticePage = new PageImpl<>(notices, pageable, 1);

            given(noticeRepository.searchByKeyword(keyword, pageable))
                    .willReturn(noticePage);

            // when
            NoticeListResponse response = noticeService.getNotices(keyword, pageable);

            // then
            assertThat(response.notices()).hasSize(1);
            verify(noticeRepository).searchByKeyword(keyword, pageable);
        }

        @Test
        @DisplayName("공지사항 목록 조회 - 빈 목록")
        void getNotices_EmptyList() {
            // given
            Pageable pageable = PageRequest.of(0, 10);
            Page<Notice> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);

            given(noticeRepository.findAllByOrderByIsImportantDescCreateDateDesc(pageable))
                    .willReturn(emptyPage);

            // when
            NoticeListResponse response = noticeService.getNotices(null, pageable);

            // then
            assertThat(response.notices()).isEmpty();
            assertThat(response.totalElements()).isEqualTo(0);
        }
    }

    @Nested
    @DisplayName("공지사항 상세 조회 테스트")
    class GetNoticeTest {

        @Test
        @DisplayName("공지사항 상세 조회 성공")
        void getNotice_Success() {
            // given
            given(noticeRepository.findByIdWithDocuments(1L))
                    .willReturn(Optional.of(testNotice));

            Long initialViewCount = testNotice.getViewCount();

            // when
            NoticeDetailResponse response = noticeService.getNotice(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.title()).isEqualTo("테스트 공지사항");
            assertThat(response.viewCount()).isEqualTo(initialViewCount + 1);
            verify(noticeRepository).findByIdWithDocuments(1L);
        }

        @Test
        @DisplayName("공지사항 상세 조회 실패 - 존재하지 않는 공지사항")
        void getNotice_NotFound() {
            // given
            given(noticeRepository.findByIdWithDocuments(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.getNotice(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 공지사항입니다.");
                    });
        }
    }

    @Nested
    @DisplayName("공지사항 수정 테스트")
    class UpdateNoticeTest {

        @Test
        @DisplayName("공지사항 수정 성공")
        void updateNotice_Success() {
            // given
            NoticeUpdateRequest updateRequest = new NoticeUpdateRequest(
                    "수정된 제목",
                    "수정된 내용",
                    true,
                    null,
                    null
            );

            given(noticeRepository.findByIdWithDocuments(1L))
                    .willReturn(Optional.of(testNotice));

            // when
            noticeService.updateNotice(1L, updateRequest);

            // then
            assertThat(testNotice.getTitle()).isEqualTo("수정된 제목");
            assertThat(testNotice.getContent()).isEqualTo("수정된 내용");
            assertThat(testNotice.getIsImportant()).isTrue();
            verify(noticeRepository).findByIdWithDocuments(1L);
        }

        @Test
        @DisplayName("공지사항 수정 실패 - 존재하지 않는 공지사항")
        void updateNotice_NotFound() {
            // given
            NoticeUpdateRequest updateRequest = new NoticeUpdateRequest(
                    "수정된 제목",
                    "수정된 내용",
                    false,
                    null,
                    null
            );

            given(noticeRepository.findByIdWithDocuments(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.updateNotice(999L, updateRequest))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 공지사항입니다.");
                    });
        }
    }

    @Nested
    @DisplayName("공지사항 삭제 테스트")
    class DeleteNoticeTest {

        @Test
        @DisplayName("공지사항 삭제 성공")
        void deleteNotice_Success() {
            // given
            given(noticeRepository.findByIdWithDocuments(1L))
                    .willReturn(Optional.of(testNotice));

            // when
            noticeService.deleteNotice(1L);

            // then
            verify(noticeRepository).findByIdWithDocuments(1L);
            verify(noticeRepository).delete(testNotice);
        }

        @Test
        @DisplayName("공지사항 삭제 실패 - 존재하지 않는 공지사항")
        void deleteNotice_NotFound() {
            // given
            given(noticeRepository.findByIdWithDocuments(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> noticeService.deleteNotice(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("존재하지 않는 공지사항입니다.");
                    });
        }
    }
}