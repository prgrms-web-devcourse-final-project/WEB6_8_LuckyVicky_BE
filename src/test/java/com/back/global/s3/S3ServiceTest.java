package com.back.global.s3;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.multipart.MultipartFile;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;

@SpringBootTest
@ActiveProfiles("test") // test 프로필 활성화 (application-test.yml 사용)
class S3ServiceTest {

    @Autowired
    private S3Service s3Service;

    @Autowired
    private S3ValidationService s3ValidationService;

    /**
     * 테스트용 1x1 픽셀의 PNG 이미지 바이트 배열을 생성하는 헬퍼 메서드
     */
    private byte[] createDummyImageBytes() throws IOException {
        BufferedImage image = new BufferedImage(1, 1, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream baos = new ByteArrayOutputStream();
        ImageIO.write(image, "png", baos);
        return baos.toByteArray();
    }

    @Test
    @DisplayName("S3 통합 테스트: 이미지(썸네일 포함) 업로드, 다운로드, 삭제")
    void s3IntegrationTest_uploadWithThumbnail_download_delete() throws IOException {
        // given: 테스트할 실제 이미지 데이터 생성
        byte[] imageBytes = createDummyImageBytes();

        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "test-image.png",
                "image/png",
                imageBytes // 텍스트 대신 실제 이미지 바이트 사용
        );
        List<String> keysToDelete = new ArrayList<>();

        try {
            // === 1. Upload ===
            // when: 파일을 업로드
            List<UploadResultResponse> uploadResults = s3Service.uploadFiles(
                    List.of(imageFile),
                    "test-dir",
                    List.of(FileType.MAIN)
            );

            // then: 업로드 결과 검증 (원본 1개 + 썸네일 1개 = 총 2개)
            assertThat(uploadResults).isNotNull();
            assertThat(uploadResults.size()).isEqualTo(2);

            // 원본 이미지 결과 찾기
            UploadResultResponse mainImageResult = uploadResults.stream()
                    .filter(r -> r.type() == FileType.MAIN)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("MAIN 이미지 결과가 없습니다."));

            // 썸네일 이미지 결과 찾기
            UploadResultResponse thumbnailResult = uploadResults.stream()
                    .filter(r -> r.type() == FileType.THUMBNAIL)
                    .findFirst()
                    .orElseThrow(() -> new AssertionError("THUMBNAIL 이미지 결과가 없습니다."));

            // 삭제할 키 리스트에 추가
            keysToDelete.add(mainImageResult.s3Key());
            keysToDelete.add(thumbnailResult.s3Key());

            // S3에 파일이 실제로 존재하는지 확인
            assertThat(s3ValidationService.validateFileExists(mainImageResult.s3Key())).isTrue();
            assertThat(s3ValidationService.validateFileExists(thumbnailResult.s3Key())).isTrue();


            // === 2. Download ===
            // when: 업로드한 원본 파일을 다시 다운로드
            byte[] downloadedFileBytes = s3Service.downloadFile(mainImageResult.s3Key());

            // then: 다운로드한 파일의 내용이 원본과 일치하는지 검증
            assertThat(downloadedFileBytes).isEqualTo(imageBytes);

        } finally {
            // === 3. Delete (Cleanup) ===
            // 테스트 중 예외가 발생하더라도, 생성된 파일은 반드시 삭제되도록 finally 블록에서 처리
            if (!keysToDelete.isEmpty()) {
                System.out.println("--- S3 테스트 파일 삭제 시작 ---");
                for (String key : keysToDelete) {
                    System.out.println("삭제 중: " + key);
                    s3Service.deleteFile(key);
                }
                System.out.println("--- S3 테스트 파일 삭제 완료 ---");

                // then: 삭제가 잘 되었는지 최종 확인 (파일이 없어서 예외가 발생해야 성공)
                assertThrows(IllegalArgumentException.class, () -> {
                    s3ValidationService.validateFileExists(keysToDelete.get(0));
                });
                assertThrows(IllegalArgumentException.class, () -> {
                    s3ValidationService.validateFileExists(keysToDelete.get(1));
                });
            }
        }
    }

    @Test
    @DisplayName("S3 엣지 케이스: ADDITIONAL 타입 이미지 업로드 시 썸네일 미생성")
    void upload_NoThumbnail_For_Additional_Type() throws IOException {
        // given: 테스트할 파일 생성 (ADDITIONAL 타입으로 지정)
        byte[] imageBytes = createDummyImageBytes();
        MultipartFile imageFile = new MockMultipartFile(
                "image",
                "additional-image.png",
                "image/png",
                imageBytes
        );
        List<String> keysToDelete = new ArrayList<>();

        try {
            // when: 파일을 ADDITIONAL 타입으로 업로드
            List<UploadResultResponse> uploadResults = s3Service.uploadFiles(
                    List.of(imageFile),
                    "test-dir",
                    List.of(FileType.ADDITIONAL)
            );

            // then: 업로드 결과가 1개인지 검증 (썸네일이 생성되지 않아야 함)
            assertThat(uploadResults).isNotNull();
            assertThat(uploadResults.size()).isEqualTo(1);

            UploadResultResponse result = uploadResults.get(0);
            assertThat(result.type()).isEqualTo(FileType.ADDITIONAL);

            // 삭제할 키 추가
            keysToDelete.add(result.s3Key());

            // 파일이 실제로 존재하는지 확인
            assertThat(s3ValidationService.validateFileExists(result.s3Key())).isTrue();

        } finally {
            // Cleanup
            if (!keysToDelete.isEmpty()) {
                System.out.println("--- S3 엣지 케이스 테스트 파일 삭제 ---");
                s3Service.deleteFile(keysToDelete.get(0));
                System.out.println("삭제 완료: " + keysToDelete.get(0));

                // 삭제 확인
                assertThrows(IllegalArgumentException.class, () -> {
                    s3ValidationService.validateFileExists(keysToDelete.get(0));
                });
            }
        }
    }
}