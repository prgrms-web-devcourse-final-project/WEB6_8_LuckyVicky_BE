package com.back.domain.artist.service;

import com.back.domain.artist.dto.request.ArtistApplicationRequest;
import com.back.domain.artist.dto.response.ArtistApplicationResponse;
import com.back.domain.artist.dto.response.ArtistApplicationSimpleResponse;
import com.back.domain.artist.dto.response.ArtistBusinessInfoResponse;
import com.back.domain.artist.entity.ApplicationStatus;
import com.back.domain.artist.entity.ArtistApplication;
import com.back.domain.artist.repository.ArtistApplicationRepository;
import com.back.domain.artist.repository.ArtistDocumentRepository;
import com.back.domain.notification.service.NotificationService;
import com.back.domain.user.entity.User;
import com.back.domain.user.repository.UserRepository;
import com.back.global.exception.ServiceException;
import com.back.global.s3.FileType;
import com.back.global.s3.S3Service;
import com.back.global.s3.UploadResultResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.util.ReflectionTestUtils;
import org.springframework.web.multipart.MultipartFile;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.verify;

@ActiveProfiles("test")
@ExtendWith(MockitoExtension.class)
@DisplayName("ArtistApplicationService 단위 테스트")
public class ArtistApplicationServiceTest {

    @Mock
    private ArtistApplicationRepository artistApplicationRepository;

    @Mock
    private ArtistDocumentRepository artistDocumentRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private S3Service s3Service;

    @Mock
    private NotificationService notificationService;

    @InjectMocks
    private ArtistApplicationService artistApplicationService;

    private User testUser;
    private ArtistApplicationRequest validRequest;
    private List<MultipartFile> validDocumentFiles;

    @BeforeEach
    void setUp() {
        // 테스트용 User 생성
        testUser = User.createLocalUser(
                "artist@test.com",
                "encodedPassword",
                "테스트작가",
                "010-1234-5678"
        );
        ReflectionTestUtils.setField(testUser, "id", 1L);

        validRequest = new ArtistApplicationRequest(
                "홍길동",
                "artist@test.com",
                "010-1234-5678",
                "아티스트홍",
                "123-45-67890",
                "서울시 강남구",
                "테헤란로 123",
                "12345",
                "2024-서울강남-00001",
                // 선택 필드들
                "홍길동아트",
                "인스타그램@artist",
                "도자기, 그림",
                "010-9876-5432",
                "카카오뱅크",
                "1234567890",
                "홍길동"
        );

        validDocumentFiles = Arrays.asList(
                new MockMultipartFile(
                        "documents",
                        "사업자등록증.pdf",
                        "application/pdf",
                        "business license content".getBytes()
                ),
                new MockMultipartFile(
                        "documents",
                        "통신판매업신고증.pdf",
                        "application/pdf",
                        "telecom cert content".getBytes()
                ),
                new MockMultipartFile(
                        "documents",
                        "포트폴리오.jpg",
                        "image/jpeg",
                        "portfolio content".getBytes()
                )
        );
    }

    @Nested
    @DisplayName("작가 신청서 생성 테스트")
    class CreateApplicationTest {

        @Test
        @DisplayName("정상적인 작가 신청서 생성 성공")
        void createApplication_Success() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(false);

            ArtistApplication savedApplication = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName(validRequest.ownerName())
                    .email(validRequest.email())
                    .phone(validRequest.phone())
                    .artistName(validRequest.artistName())
                    .businessNumber(validRequest.businessNumber())
                    .businessAddress(validRequest.businessAddress())
                    .businessAddressDetail(validRequest.businessAddressDetail())
                    .businessZipCode(validRequest.businessZipCode())
                    .telecomSalesNumber(validRequest.telecomSalesNumber())
                    .build();
            ReflectionTestUtils.setField(savedApplication, "id", 1L);

            given(artistApplicationRepository.save(any(ArtistApplication.class)))
                    .willReturn(savedApplication);

            // ✅ S3 업로드 결과 Mock
            List<UploadResultResponse> uploadResults = Arrays.asList(
                    new UploadResultResponse(
                            "https://s3.../business.pdf",
                            FileType.DOCUMENT,
                            "docs/uuid1.pdf",
                            "사업자등록증.pdf"
                    ),
                    new UploadResultResponse(
                            "https://s3.../telecom.pdf",
                            FileType.DOCUMENT,
                            "docs/uuid2.pdf",
                            "통신판매업신고증.pdf"
                    ),
                    new UploadResultResponse(
                            "https://s3.../portfolio.jpg",
                            FileType.DOCUMENT,
                            "docs/uuid3.jpg",
                            "포트폴리오.jpg"
                    )
            );

            given(s3Service.uploadFiles(anyList(), anyString(), anyList()))
                    .willReturn(uploadResults);

            // ✅ 관리자 목록 Mock
            given(userRepository.findAllAdmins()).willReturn(Collections.emptyList());

            // when
            Long applicationId = artistApplicationService.createApplication(
                    1L,
                    validRequest,
                    validDocumentFiles  // ✅ 파일 추가
            );

            // then
            assertThat(applicationId).isEqualTo(1L);
            verify(userRepository).findById(1L);
            verify(artistApplicationRepository).existsByUserIdAndStatus(1L, ApplicationStatus.PENDING);
            verify(artistApplicationRepository).save(any(ArtistApplication.class));
            verify(s3Service).uploadFiles(anyList(), anyString(), anyList());
            verify(artistDocumentRepository).saveAll(anyList());
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 신청 실패")
        void createApplication_UserNotFound() {
            // given
            given(userRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(
                    999L, validRequest, validDocumentFiles))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("중복 신청으로 실패 (이미 PENDING 상태 신청서 존재)")
        void createApplication_DuplicateApplication() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(true);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(
                    1L, validRequest, validDocumentFiles))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).isEqualTo("이미 심사 대기 중인 신청서가 있습니다.");
                    });
        }

        @Test
        @DisplayName("필수 서류 파일 누락으로 신청 실패 (파일이 2개 미만)")
        void createApplication_MissingDocuments() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(false);

            // ✅ 1개 파일만 제공
            List<MultipartFile> incompleteFiles = Arrays.asList(
                    new MockMultipartFile(
                            "documents",
                            "사업자등록증.pdf",
                            "application/pdf",
                            "business license content".getBytes()
                    )
            );

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(
                    1L, validRequest, incompleteFiles))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).contains("필수 서류 파일이 누락되었습니다");
                    });
        }

        @Test
        @DisplayName("파일명에 필수 키워드 누락으로 신청 실패 (사업자등록증 키워드 없음)")
        void createApplication_MissingBusinessLicenseKeyword() {
            // given
            given(userRepository.findById(1L)).willReturn(Optional.of(testUser));
            given(artistApplicationRepository.existsByUserIdAndStatus(1L, ApplicationStatus.PENDING))
                    .willReturn(false);

            ArtistApplication savedApplication = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName(validRequest.ownerName())
                    .email(validRequest.email())
                    .phone(validRequest.phone())
                    .artistName(validRequest.artistName())
                    .businessNumber(validRequest.businessNumber())
                    .businessAddress(validRequest.businessAddress())
                    .businessAddressDetail(validRequest.businessAddressDetail())
                    .businessZipCode(validRequest.businessZipCode())
                    .telecomSalesNumber(validRequest.telecomSalesNumber())
                    .build();
            ReflectionTestUtils.setField(savedApplication, "id", 1L);

            given(artistApplicationRepository.save(any(ArtistApplication.class)))
                    .willReturn(savedApplication);

            // ✅ 잘못된 파일명 (키워드 없음)
            List<MultipartFile> wrongNameFiles = Arrays.asList(
                    new MockMultipartFile(
                            "documents",
                            "document1.pdf",  // 키워드 없음
                            "application/pdf",
                            "content".getBytes()
                    ),
                    new MockMultipartFile(
                            "documents",
                            "document2.pdf",  // 키워드 없음
                            "application/pdf",
                            "content".getBytes()
                    )
            );

            List<UploadResultResponse> uploadResults = Arrays.asList(
                    new UploadResultResponse(
                            "https://s3.../doc1.pdf",
                            FileType.DOCUMENT,
                            "docs/uuid1.pdf",
                            "document1.pdf"
                    ),
                    new UploadResultResponse(
                            "https://s3.../doc2.pdf",
                            FileType.DOCUMENT,
                            "docs/uuid2.pdf",
                            "document2.pdf"
                    )
            );

            given(s3Service.uploadFiles(anyList(), anyString(), anyList()))
                    .willReturn(uploadResults);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.createApplication(
                    1L, validRequest, wrongNameFiles))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).contains("필수 서류가 누락되었습니다");
                        assertThat(serviceEx.getMsg()).contains("사업자등록증");
                        assertThat(serviceEx.getMsg()).contains("통신판매업신고증");
                    });
        }
    }

    @Nested
    @DisplayName("내 신청서 목록 조회 테스트")
    class GetMyApplicationsTest {

        @Test
        @DisplayName("내 신청서 목록 조회 성공")
        void getMyApplications_Success() {
            // given
            given(userRepository.existsById(1L)).willReturn(true);

            ArtistApplication application1 = ArtistApplication.builder()
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
            ReflectionTestUtils.setField(application1, "id", 1L);

            ArtistApplication application2 = ArtistApplication.builder()
                    .user(testUser)
                    .ownerName("홍길동")
                    .email("artist@test.com")
                    .phone("010-1234-5678")
                    .artistName("아티스트홍2")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00002")
                    .build();
            ReflectionTestUtils.setField(application2, "id", 2L);

            given(artistApplicationRepository.findByUserIdOrderByCreateDateDesc(1L))
                    .willReturn(Arrays.asList(application1, application2));

            // when
            List<ArtistApplicationSimpleResponse> responses =
                    artistApplicationService.getMyApplications(1L);

            // then
            assertThat(responses).hasSize(2);
            assertThat(responses.get(0).artistName()).isEqualTo("아티스트홍");
            assertThat(responses.get(1).artistName()).isEqualTo("아티스트홍2");
            verify(artistApplicationRepository).findByUserIdOrderByCreateDateDesc(1L);
        }

        @Test
        @DisplayName("신청서가 없는 경우 빈 목록 반환")
        void getMyApplications_EmptyList() {
            // given
            given(userRepository.existsById(1L)).willReturn(true);
            given(artistApplicationRepository.findByUserIdOrderByCreateDateDesc(1L))
                    .willReturn(Collections.emptyList());

            // when
            List<ArtistApplicationSimpleResponse> responses =
                    artistApplicationService.getMyApplications(1L);

            // then
            assertThat(responses).isEmpty();
            verify(artistApplicationRepository).findByUserIdOrderByCreateDateDesc(1L);
        }

        @Test
        @DisplayName("존재하지 않는 사용자로 목록 조회 실패")
        void getMyApplications_UserNotFound() {
            // given
            given(userRepository.existsById(999L)).willReturn(false);

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getMyApplications(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("사용자를 찾을 수 없습니다.");
                    });

            verify(userRepository).existsById(999L);
        }
    }

    @Nested
    @DisplayName("신청서 상세 조회 테스트")
    class GetApplicationByIdTest {

        @Test
        @DisplayName("본인 신청서 상세 조회 성공")
        void getApplicationById_Success() {
            // given
            ArtistApplication application = ArtistApplication.builder()
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
            ReflectionTestUtils.setField(application, "id", 1L);

            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when
            ArtistApplicationResponse response =
                    artistApplicationService.getApplicationById(1L, 1L);

            // then
            assertThat(response).isNotNull();
            assertThat(response.artistName()).isEqualTo("아티스트홍");
            assertThat(response.userId()).isEqualTo(1L);
            verify(artistApplicationRepository).findById(1L);
        }

        @Test
        @DisplayName("존재하지 않는 신청서 조회 실패")
        void getApplicationById_NotFound() {
            // given
            given(artistApplicationRepository.findById(999L)).willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getApplicationById(1L, 999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("신청서를 찾을 수 없습니다.");
                    });
        }

        @Test
        @DisplayName("다른 사람의 신청서 조회 실패")
        void getApplicationById_Forbidden() {
            // given
            User otherUser = User.createLocalUser(
                    "other@test.com",
                    "encodedPassword",
                    "다른사용자",
                    "010-9999-9999"
            );
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            ArtistApplication application = ArtistApplication.builder()
                    .user(otherUser)
                    .ownerName("홍길동")
                    .email("other@test.com")
                    .phone("010-9999-9999")
                    .artistName("다른작가")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00001")
                    .build();
            ReflectionTestUtils.setField(application, "id", 1L);

            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getApplicationById(1L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("403");
                        assertThat(serviceEx.getMsg()).isEqualTo("본인의 신청서만 조회할 수 있습니다.");
                    });
        }
    }

    @Nested
    @DisplayName("사업자 정보 조회 테스트")
    class GetBusinessInfoTest {

        @Test
        @DisplayName("사업자 정보 정상 조회")
        void getBusinessInfo_Success() {
            // given
            ArtistApplication application = ArtistApplication.builder()
                    .user(testUser)
                    .businessName("홍길동 작가실")
                    .businessNumber("123-45-67890")
                    .ownerName("홍길동")
                    .managerPhone("010-1234-5678")
                    .email("artist@test.com")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("2층")
                    .telecomSalesNumber("2023-서울강남-0001")
                    .build();

            given(artistApplicationRepository.findByUserId(1L))
                    .willReturn(Optional.of(application));

            // when
            ArtistBusinessInfoResponse response = artistApplicationService.getBusinessInfo(1L);

            // then
            assertThat(response.businessName()).isEqualTo("홍길동 작가실");
            assertThat(response.businessNumber()).isEqualTo("123-45-67890");
            assertThat(response.ownerName()).isEqualTo("홍길동");
            assertThat(response.asManager()).isEqualTo("홍길동 작가실/010-1234-5678");
            assertThat(response.email()).isEqualTo("artist@test.com");
            assertThat(response.businessAddress()).isEqualTo("서울시 강남구 2층");
            assertThat(response.telecomSalesNumber()).isEqualTo("2023-서울강남-0001");

            verify(artistApplicationRepository).findByUserId(1L);
        }

        @Test
        @DisplayName("사업자 정보 조회 실패 - 신청서 없음")
        void getBusinessInfo_NotFound() {
            // given
            given(artistApplicationRepository.findByUserId(999L))
                    .willReturn(Optional.empty());

            // when & then
            assertThatThrownBy(() -> artistApplicationService.getBusinessInfo(999L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("404");
                        assertThat(serviceEx.getMsg()).isEqualTo("작가 신청 정보가 없습니다.");
                    });

            verify(artistApplicationRepository).findByUserId(999L);
        }
    }

    @Nested
    @DisplayName("작가 신청 취소 테스트")
    class CancelApplicationTest {

        @Test
        @DisplayName("PENDING 상태의 신청서 취소 성공")
        void cancelApplication_Success() {
            // given
            ArtistApplication application = ArtistApplication.builder()
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
            ReflectionTestUtils.setField(application, "id", 1L);

            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when
            artistApplicationService.cancelApplication(1L, 1L);

            // then
            assertThat(application.getStatus()).isEqualTo(ApplicationStatus.CANCELLED);
            assertThat(application.isCancelled()).isTrue();
            verify(artistApplicationRepository).findById(1L);
        }

        @Test
        @DisplayName("다른 사람의 신청서 취소 실패")
        void cancelApplication_Forbidden() {
            // given
            User otherUser = User.createLocalUser(
                    "other@test.com",
                    "encodedPassword",
                    "다른사용자",
                    "010-9999-9999"
            );
            ReflectionTestUtils.setField(otherUser, "id", 2L);

            ArtistApplication application = ArtistApplication.builder()
                    .user(otherUser)
                    .ownerName("다른사람")
                    .email("other@test.com")
                    .phone("010-9999-9999")
                    .artistName("다른작가")
                    .businessNumber("123-45-67890")
                    .businessAddress("서울시 강남구")
                    .businessAddressDetail("테헤란로 123")
                    .businessZipCode("12345")
                    .telecomSalesNumber("2024-서울강남-00001")
                    .build();
            ReflectionTestUtils.setField(application, "id", 1L);

            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when & then
            assertThatThrownBy(() -> artistApplicationService.cancelApplication(1L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .hasFieldOrPropertyWithValue("resultCode", "403");
        }

        @Test
        @DisplayName("APPROVED 상태의 신청서 취소 실패")
        void cancelApplication_AlreadyApproved() {
            // given
            ArtistApplication application = ArtistApplication.builder()
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
            ReflectionTestUtils.setField(application, "id", 1L);

            application.approve(999L, "관리자");
            given(artistApplicationRepository.findById(1L)).willReturn(Optional.of(application));

            // when & then
            assertThatThrownBy(() -> artistApplicationService.cancelApplication(1L, 1L))
                    .isInstanceOf(ServiceException.class)
                    .satisfies(ex -> {
                        ServiceException serviceEx = (ServiceException) ex;
                        assertThat(serviceEx.getResultCode()).isEqualTo("400");
                        assertThat(serviceEx.getMsg()).contains("심사 대기 중인 신청서만 취소할 수 있습니다");
                    });
        }
    }
}