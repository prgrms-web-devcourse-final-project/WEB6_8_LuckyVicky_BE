package com.back.domain.artist.service;

import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.entity.ArtistProfile;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistProfileRepository;
import com.back.domain.user.entity.Role;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
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
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Arrays;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;
import static org.mockito.Mockito.never;

@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistApplicationAdminService 단위 테스트")
class ArtistApplicationAdminServiceTest {

    @Mock
    private ArtistApplicationRepository artistApplicationRepository;

    @Mock
    private ArtistProfileRepository artistProfileRepository;

    @Mock
    private UserRepository userRepository;
    @InjectMocks
    private ArtistApplicationAdminService adminService;

    private User testUser;
    private ArtistApplication testApplication;

    @BeforeEach
    void setUp() {
        testUser = User.createLocalUser(
                "artist@test.com",
                "encodedPassword",
                "테스트작가",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);

        testApplication = ArtistApplication.builder()
                .user(testUser)
                .ownerName("홍길동")
                .email("artist@test.com")
                .phone("010-1234-5678")
                .artistName("아티스트홍")
                .businessNumber("123-45-67890")
                .businessAddress("서울시 강남구")
                .businessAddressDetail("테헤란로 123")
                .businessZipCode("12345")
                .telecomSalesNumber("2024-서울강남-00001")
                .build();
        ReflectionTestUtils.setField(testApplication, "id", 1L);
    }

    @Nested
    @DisplayName("전체 신청서 목록 조회 테스트")
    class GetAllApplicationsTest {

        @Test
        @DisplayName("전체 신청서 목록 조회 성공 (페이징)")
        void getAllApplications_Success() {
            // given
            Pageable pageable = PageRequest.of(0, 10);

            ArtistApplication application1 = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName("홍길동1")
                    .email("artist1@test.com")
                    .phone("010-1111-1111")
                    .artistName("아티스트1")
                    .businessNumber("111-11-11111")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 111")
                    .businessZipCode("11111")
                    .telecomSalesNumber("2024-서울강남-00001")
                    .build();
            ReflectionTestUtils.setField(application1, "id", 1L);

            ArtistApplication application2 = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName("홍길동2")
                    .email("artist2@test.com")
                    .phone("010-2222-2222")
                    .artistName("아티스트2")
                    .businessNumber("222-22-22222")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 222")
                    .businessZipCode("22222")
                    .telecomSalesNumber("2024-서울강남-00002")
                    .build();
            ReflectionTestUtils.setField(application2, "id", 2L);

            Page<ArtistApplication> applicationPage = new PageImpl<>(
                    Arrays.asList(application1, application2),
                    pageable,
                    2
            );

            given(artistApplicationRepository.findAllByOrderByCreateDateDesc(pageable))
                    .willReturn(applicationPage);

            // when
            Page<ArtistApplicationSimpleResponse> result = adminService.getAllApplications(pageable);

            // then
            assertThat(result.getContent()).hasSize(2);
            assertThat(result.getTotalElements()).isEqualTo(2);
            assertThat(result.getContent().get(0).artistName()).isEqualTo("아티스트1");
            assertThat(result.getContent().get(1).artistName()).isEqualTo("아티스트2");
            verify(artistApplicationRepository).findAllByOrderByCreateDateDesc(pageable);
        }
    }

    @Nested
    @DisplayName("신청서 상세 조회 테스트")
    class GetApplicationByIdTest {

        @Test
        @DisplayName("신청서 상세 조회 성공")
        void getApplicationById_Success() {
            // given
            given(artistApplicationRepository.findById(1L))
                    .willReturn(Optional.of(testApplication));

            // when
            ArtistApplicationResponse response = adminService.getApplicationById(1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(1L);
            assertThat(response.artistName()).isEqualTo("아티스트홍");
            assertThat(response.ownerName()).isEqualTo("홍길동");
            verify(artistApplicationRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 신청서 조회 실패")
        void getApplicationById_NotFound() {
            // given
            given(artistApplicationRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminService.getApplicationById(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("신청서를 찾을 수 없습니다.");
                    });

            verify(artistApplicationRepository).findById(999L);
        }
    }

    @Nested
    @DisplayName("신청서 승인 테스트")
    class ApproveApplicationTest {

        @Test
        @DisplayName("신청서 승인 성공 - User가 ARTIST로 승급됨")
        void approveApplication_Success() {
            // given
            given(artistApplicationRepository.findById(1L))
                    .willReturn(Optional.of(testApplication));

            given(artistProfileRepository.existsByUserId(testUser.getId()))
                    .willReturn(false);

            given(artistProfileRepository.save(any(ArtistProfile.class)))
                    .willAnswer(invocation -> invocation.getArgument(0));

            // when
            adminService.approveApplication(1L, 100L, "관리자");

            // then
            // 신청서 상태 확인
            assertThat(testApplication.getStatus()).isEqualTo(ApplicationStatus.APPROVED);
            assertThat(testApplication.getReviewedById()).isEqualTo(100L);
            assertThat(testApplication.getReviewedByName()).isEqualTo("관리자");
            assertThat(testApplication.getReviewedAt()).isNotNull();

            // User Role 확인
            assertThat(testUser.getRole()).isEqualTo(Role.ARTIST);
            assertThat(testUser.getIsArtistVerified()).isTrue();
            assertThat(testUser.getArtistVerifiedAt()).isNotNull();

            verify(artistApplicationRepository).findById(1L);
            verify(artistProfileRepository).existsByUserId(testUser.getId());
            verify(artistProfileRepository).save(any(ArtistProfile.class));
        }

        @Test
        @DisplayName("존재하지 않는 신청서 승인 실패")
        void approveApplication_NotFound() {
            // given
            given(artistApplicationRepository.findById(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> adminService.approveApplication(999L, 100L, "관리자"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("신청서를 찾을 수 없습니다.");
                    });

            verify(artistApplicationRepository).findById(999L);
        }

        @Test
        @DisplayName("이미 승인된 신청서 재승인 시도 실패")
        void approveApplication_AlreadyApproved() {
            // given
            // 이미 승인된 신청서
            testApplication.approve(99L, "이전관리자");

            given(artistApplicationRepository.findById(1L))
                    .willReturn(Optional.of(testApplication));

            // when & then
            assertThatThrownBy(() -> adminService.approveApplication(1L, 100L, "관리자"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("500");
                        assertThat(serviceEx.getMsg()).isEqualTo("대기중 상태의 신청서만 승인/거절할 수 있습니다.");
                    });

            verify(artistApplicationRepository).findById(1L);
        }

        @Test
        @DisplayName("거절된 신청서 승인 시도 실패")
        void approveApplication_AlreadyRejected() {
            // given
            // 이미 거절된 신청서
            testApplication.reject(99L, "이전관리자", "서류 미비");

            given(artistApplicationRepository.findById(1L))
                    .willReturn(Optional.of(testApplication));

            // when & then
            assertThatThrownBy(() -> adminService.approveApplication(1L, 100L, "관리자"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("500");
                        assertThat(serviceEx.getMsg()).isEqualTo("대기중 상태의 신청서만 승인/거절할 수 있습니다.");
                    });

            verify(artistApplicationRepository).findById(1L);
        }

        @Test
        @DisplayName("이미 작가 프로필이 있는 경우 승인 실패")
        void approveApplication_AlreadyHasProfile() {
            // given
            given(artistApplicationRepository.findById(1L))
                    .willReturn(Optional.of(testApplication));

            // ⭐ 이미 작가 프로필 존재
            given(artistProfileRepository.existsByUserId(testUser.getId()))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> adminService.approveApplication(1L, 100L, "관리자"))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 작가 프로필이 존재합니다.");
                    });

            // ⭐ 검증: findById는 호출되지만 save는 호출되지 않음
            verify(artistApplicationRepository).findById(1L);
            verify(artistProfileRepository).existsByUserId(testUser.getId());
            verify(artistProfileRepository, never()).save(any());
        }
    }
}