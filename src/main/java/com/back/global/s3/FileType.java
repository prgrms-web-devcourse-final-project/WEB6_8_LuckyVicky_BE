package com.back.global.s3;

public enum FileType {
    MAIN, // 대표 이미지
    ADDITIONAL, //추가 이미지
    THUMBNAIL,// 목록에서 보일 썸네일 이미지
    DOCUMENT // 그 외 다른 파일(pdf, doc 등)
}