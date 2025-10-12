package com.back.domain.support.inquiry.repository;

import com.back.domain.support.inquiry.entity.InquiryReply;
import org.springframework.data.jpa.repository.JpaRepository;

public interface InquiryReplyRepository extends JpaRepository<InquiryReply, Long> {
    // 기본 메서드만 사용
}
