package com.back.global.jpa.entity;

import com.back.global.s3.FileType;
import jakarta.persistence.Column;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.MappedSuperclass;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

@Getter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
@MappedSuperclass
public abstract class FileBaseEntity extends BaseEntity {

    @Column(nullable = false)
    private String fileUrl; // S3에 업로드된 파일의 URL

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private FileType fileType; // 파일 종류 (MAIN, ADDITIONAL, THUMBNAIL, DOCUMENT)

    @Column(nullable = false)
    private String key; // S3의 파일들을 식별할 수 있는 객체 키

    @Column(nullable = false)
    private String originalFilename; // 원본 파일명 (이미지는 저장 x, 문서의 경우 저장)
}

