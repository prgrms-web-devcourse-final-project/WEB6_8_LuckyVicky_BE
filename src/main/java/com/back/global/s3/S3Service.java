package com.back.global.s3;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

import javax.imageio.ImageIO;
import java.awt.*;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class S3Service {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * 여러 파일 업로드
     */
    public List<UploadResult> uploadFiles(List<MultipartFile> files, String folder, List<FileType> types) {
        if (types != null && files.size() != types.size()) {
            throw new IllegalArgumentException("files와 types의 개수가 일치해야 합니다.");
        }

        List<CompletableFuture<UploadResult>> futures = new ArrayList<>();
        for (int i = 0; i < files.size(); i++) {
            MultipartFile file = files.get(i);
            FileType type = (types != null ? types.get(i) : null);
            futures.add(uploadFileAsync(file, folder, type));
        }

        return futures.stream()
                .map(CompletableFuture::join)
                .collect(Collectors.toList());
    }

    @Async
    public CompletableFuture<UploadResult> uploadFileAsync(MultipartFile file, String folder, FileType type) {
        try {
            return CompletableFuture.completedFuture(uploadFile(file, folder, type));
        } catch (Exception e) {
            log.error("파일 업로드 실패: {}", file.getOriginalFilename(), e);
            throw new RuntimeException("파일 업로드 실패: " + file.getOriginalFilename(), e);
        }
    }

    /**
     * 단일 파일 업로드
     */
    public UploadResult uploadFile(MultipartFile file, String folder, FileType type) throws IOException {
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || originalFilename.isBlank()) {
            throw new IllegalArgumentException("파일 이름이 없습니다.");
        }

        String extension = originalFilename.substring(originalFilename.lastIndexOf('.') + 1).toLowerCase();
        FileCategory category = FileCategory.fromExtension(extension);

        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";

        // S3 Key 생성 (UUID 사용)
        String key = folder + "/" + UUID.randomUUID() + "." + extension;
        putS3Object(file.getBytes(), key, contentType);

        String originalUrl = s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(key)).toString();

        String thumbnailUrl = null;
        if (category == FileCategory.IMAGE && type == FileType.MAIN) {
            byte[] thumbBytes = resizeImageSafe(file.getBytes(), 300, 300, extension);
            String thumbKey = folder + "/thumbnail-" + UUID.randomUUID() + "." + extension;
            putS3Object(thumbBytes, thumbKey, contentType);
            thumbnailUrl = s3Client.utilities().getUrl(b -> b.bucket(bucketName).key(thumbKey)).toString();
        }

        return new UploadResult(originalUrl, thumbnailUrl, type, category, key);
    }

    private void putS3Object(byte[] data, String key, String contentType) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucketName)
                .key(key)
                .contentType(contentType)
                .build();
        s3Client.putObject(request, RequestBody.fromBytes(data));
    }

    private byte[] resizeImageSafe(byte[] originalImageBytes, int width, int height, String extension) throws IOException {
        BufferedImage originalImage = ImageIO.read(new ByteArrayInputStream(originalImageBytes));
        if (originalImage == null) throw new IOException("이미지 형식 오류");

        BufferedImage resized = new BufferedImage(width, height,
                originalImage.getType() == 0 ? BufferedImage.TYPE_INT_ARGB : originalImage.getType());
        Graphics2D g = resized.createGraphics();
        g.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
        g.drawImage(originalImage, 0, 0, width, height, null);
        g.dispose();

        ByteArrayOutputStream bos = new ByteArrayOutputStream();
        ImageIO.write(resized, extension, bos);
        return bos.toByteArray();
    }

    // 문서 다운로드 전용
    public byte[] downloadFile(String key) {
        try {
            ResponseBytes<GetObjectResponse> s3Object = s3Client.getObjectAsBytes(GetObjectRequest.builder()
                    .bucket(bucketName)
                    .key(key)
                    .build());
            return s3Object.asByteArray();
        } catch (Exception e) {
            log.error("파일 다운로드 실패: {}", key, e);
            throw new RuntimeException("파일 다운로드 실패: " + key, e);
        }
    }

    public enum FileCategory {
        IMAGE, DOCUMENT, OTHER;

        public static FileCategory fromExtension(String extension) {
            String ext = extension.toLowerCase();
            if (List.of("jpg","jpeg","png","gif","bmp","webp").contains(ext)) return IMAGE;
            if (List.of("pdf","doc","docx","xls","xlsx","ppt","pptx","hwp").contains(ext)) return DOCUMENT;
            return OTHER;
        }
    }

    public static class UploadResult {
        private String originalUrl;
        private String thumbnailUrl;
        private FileType type;
        private FileCategory category;
        private String key;

        public UploadResult(String originalUrl, String thumbnailUrl, FileType type, FileCategory category, String key) {
            this.originalUrl = originalUrl;
            this.thumbnailUrl = thumbnailUrl;
            this.type = type;
            this.category = category;
            this.key = key;
        }

        public String getOriginalUrl() { return originalUrl; }
        public String getThumbnailUrl() { return thumbnailUrl; }
        public FileType getType() { return type; }
        public FileCategory getCategory() { return category; }
        public String getKey() { return key; }
    }
}
