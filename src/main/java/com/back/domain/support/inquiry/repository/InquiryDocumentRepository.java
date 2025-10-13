package com.back.domain.support.inquiry.repository;

import com.back.domain.support.inquiry.entity.InquiryDocument;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryDocumentRepository extends JpaRepository<InquiryDocument, Long> {
    // 기본 메서드만 사용
}
