package com.back.domain.support.notice.controller;

import com.back.domain.support.notice.entity.Notice;
import com.back.domain.support.notice.repository.NoticeDocumentRepository;
import com.back.domain.support.notice.repository.NoticeRepository;
import com.back.domain.user.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithUserDetails;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.transaction.annotation.Transactional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@ActiveProfiles("test")
@SpringBootTest
@AutoConfigureMockMvc
@Transactional
@DisplayName("공지사항 컨트롤러 테스트")
public class NoticeControllerTest {

    @Autowired
    private MockMvc mvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private NoticeRepository noticeRepository;

    @Autowired
    private NoticeDocumentRepository noticeDocumentRepository;

    @Test
    @DisplayName("1. 공지사항 목록 조회 - 성공")
    void t1() throws Exception {
        // given - 공지사항 생성
        Notice notice1 = Notice.builder()
                .title("설날 연휴 배송 안내")
                .content("설날 연휴 기간에는 배송이 지연될 수 있습니다.")
                .isImportant(true)
                .build();
        noticeRepository.save(notice1);

        Notice notice2 = Notice.builder()
                .title("신규 작가 입점 안내")
                .content("새로운 작가님들이 입점했습니다.")
                .isImportant(false)
                .build();
        noticeRepository.save(notice2);

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/notices")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공지사항 목록 조회 성공"))
                .andExpect(jsonPath("$.data.notices").isArray())
                .andExpect(jsonPath("$.data.notices.length()").value(2))
                .andExpect(jsonPath("$.data.currentPage").value(0))
                .andExpect(jsonPath("$.data.totalElements").value(2));
    }

    @Test
    @DisplayName("2. 공지사항 목록 조회 - 빈 목록")
    void t2() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/support/notices")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공지사항 목록 조회 성공"))
                .andExpect(jsonPath("$.data.notices").isArray())
                .andExpect(jsonPath("$.data.notices.length()").value(0));
    }

    @Test
    @DisplayName("3. 공지사항 검색 - 성공")
    void t3() throws Exception {
        // given
        Notice notice1 = Notice.builder()
                .title("배송 관련 공지")
                .content("배송이 지연됩니다.")
                .isImportant(false)
                .build();
        noticeRepository.save(notice1);

        Notice notice2 = Notice.builder()
                .title("작가 입점 안내")
                .content("새로운 작가님들이 입점했습니다.")
                .isImportant(false)
                .build();
        noticeRepository.save(notice2);

        // when - "배송" 키워드로 검색
        ResultActions resultActions = mvc.perform(get("/api/support/notices")
                        .param("keyword", "배송")
                        .param("page", "1")
                        .param("size", "10")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.data.notices.length()").value(1))
                .andExpect(jsonPath("$.data.notices[0].title").value("배송 관련 공지"));
    }

    @Test
    @DisplayName("4. 공지사항 상세 조회 - 성공")
    void t4() throws Exception {
        // given
        Notice notice = Notice.builder()
                .title("테스트 공지사항")
                .content("테스트 내용입니다.")
                .isImportant(true)
                .build();
        noticeRepository.save(notice);

        Long initialViewCount = notice.getViewCount();

        // when
        ResultActions resultActions = mvc.perform(get("/api/support/notices/" + notice.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공지사항 상세 조회 성공"))
                .andExpect(jsonPath("$.data.id").value(notice.getId()))
                .andExpect(jsonPath("$.data.title").value("테스트 공지사항"))
                .andExpect(jsonPath("$.data.content").value("테스트 내용입니다."))
                .andExpect(jsonPath("$.data.isImportant").value(true))
                .andExpect(jsonPath("$.data.viewCount").value(initialViewCount + 1));
    }

    @Test
    @DisplayName("5. 공지사항 상세 조회 - 실패 (존재하지 않는 공지사항)")
    void t5() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(get("/api/support/notices/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 공지사항입니다."));
    }

    // ========================================
    // 관리자 전용 API 테스트
    // ========================================

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("6. 공지사항 생성 - 성공 (관리자)")
    void t6() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/notices")
                        .param("title", "새로운 공지사항")
                        .param("content", "새로운 공지사항 내용입니다.")
                        .param("isImportant", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공지사항이 성공적으로 등록되었습니다."))
                .andExpect(jsonPath("$.data").isNumber());
    }

    @Test
    @DisplayName("7. 공지사항 생성 - 실패 (미인증)")
    void t7() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/notices")
                        .param("title", "새로운 공지사항")
                        .param("content", "새로운 공지사항 내용입니다.")
                        .param("isImportant", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("8. 공지사항 생성 - 실패 (일반 사용자)")
    void t8() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/notices")
                        .param("title", "새로운 공지사항")
                        .param("content", "새로운 공지사항 내용입니다.")
                        .param("isImportant", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("9. 공지사항 생성 - 실패 (필수 필드 누락)")
    void t9() throws Exception {
        // when - 제목 누락
        ResultActions resultActions = mvc.perform(multipart("/api/support/notices")
                        .param("content", "내용만 있습니다.")
                        .param("isImportant", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isBadRequest());
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("10. 공지사항 수정 - 성공")
    void t10() throws Exception {
        // given
        Notice notice = Notice.builder()
                .title("수정 전 제목")
                .content("수정 전 내용")
                .isImportant(false)
                .build();
        noticeRepository.save(notice);

        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/notices/" + notice.getId())
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "수정 후 제목")
                        .param("content", "수정 후 내용")
                        .param("isImportant", "true")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공지사항이 성공적으로 수정되었습니다."));

        // 실제로 수정되었는지 확인
        Notice updatedNotice = noticeRepository.findById(notice.getId()).orElseThrow();
        assertThat(updatedNotice.getTitle()).isEqualTo("수정 후 제목");
        assertThat(updatedNotice.getContent()).isEqualTo("수정 후 내용");
        assertThat(updatedNotice.getIsImportant()).isTrue();
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("11. 공지사항 수정 - 실패 (존재하지 않는 공지사항)")
    void t11() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(multipart("/api/support/notices/99999")
                        .with(request -> {
                            request.setMethod("PUT");
                            return request;
                        })
                        .param("title", "수정 제목")
                        .param("content", "수정 내용")
                        .param("isImportant", "false")
                        .contentType(MediaType.MULTIPART_FORM_DATA))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 공지사항입니다."));
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("12. 공지사항 삭제 - 성공")
    void t12() throws Exception {
        // given
        Notice notice = Notice.builder()
                .title("삭제할 공지사항")
                .content("삭제할 내용")
                .isImportant(false)
                .build();
        noticeRepository.save(notice);

        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/notices/" + notice.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isOk())
                .andExpect(jsonPath("$.resultCode").value("200"))
                .andExpect(jsonPath("$.msg").value("공지사항이 삭제되었습니다."));

        // 실제로 삭제되었는지 확인
        assertThat(noticeRepository.findById(notice.getId())).isEmpty();
    }

    @Test
    @WithUserDetails("admin1@admin.com")
    @DisplayName("13. 공지사항 삭제 - 실패 (존재하지 않는 공지사항)")
    void t13() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/notices/99999")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.resultCode").value("404"))
                .andExpect(jsonPath("$.msg").value("존재하지 않는 공지사항입니다."));
    }

    @Test
    @WithUserDetails("user1@user.com")
    @DisplayName("14. 공지사항 삭제 - 실패 (일반 사용자)")
    void t14() throws Exception {
        // given
        Notice notice = Notice.builder()
                .title("삭제할 공지사항")
                .content("삭제할 내용")
                .isImportant(false)
                .build();
        noticeRepository.save(notice);

        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/notices/" + notice.getId())
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("15. 공지사항 삭제 - 실패 (미인증)")
    void t15() throws Exception {
        // when
        ResultActions resultActions = mvc.perform(delete("/api/support/notices/1")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print());

        // then
        resultActions.andExpect(status().isUnauthorized());
    }
}
