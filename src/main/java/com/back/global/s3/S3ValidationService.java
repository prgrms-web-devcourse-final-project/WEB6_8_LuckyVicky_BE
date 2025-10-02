package com.back.global.s3;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.HeadObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import com.back.global.exception.ServiceException;

@Service
@RequiredArgsConstructor
public class S3ValidationService {

    private final S3Client s3Client;

    @Value("${cloud.aws.s3.bucket}")
    private String bucketName;

    /**
     * S3에 해당 key의 파일이 존재하는지 확인하는 메서드
     * @param s3Key S3 객체 키
     * @return 존재하면 true
     * @throws IllegalArgumentException 존재하지 않으면 예외 발생
     */
    public boolean validateFileExists(String s3Key) {
        try {
            HeadObjectRequest request = HeadObjectRequest.builder()
                    .bucket(bucketName)
                    .key(s3Key)
                    .build();

            s3Client.headObject(request);
            return true;
        } catch (NoSuchKeyException e) {
            throw new ServiceException("400", "S3에 파일이 존재하지 않습니다");
        }
    }
}
