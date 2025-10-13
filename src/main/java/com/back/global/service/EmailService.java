package com.back.global.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${custom.site.name}")
    private String siteName;

    /**
     * 임시 비밀번호 발송
     */
    public void sendTemporaryPassword(String toEmail, String temporaryPassword) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject(String.format("[%s] 임시 비밀번호 안내", siteName));
            message.setText(buildPasswordResetEmailContent(temporaryPassword));

            mailSender.send(message);
            log.info("임시 비밀번호 이메일 발송 완료: {}", toEmail);

        } catch (Exception e) {
            log.error("이메일 발송 실패: {}", toEmail, e);
            throw new RuntimeException("이메일 발송에 실패했습니다.");
        }
    }

    /**
     * 이메일 본문 내용 생성
     */
    private String buildPasswordResetEmailContent(String temporaryPassword) {
        return String.format("""
            안녕하세요, %s입니다.
            
            임시 비밀번호가 발급되었습니다.
            아래 임시 비밀번호로 로그인 후 반드시 비밀번호를 변경해주세요.
            
            임시 비밀번호: %s
            
            본인이 요청하지 않았다면 즉시 고객센터로 문의해주세요.
            
            감사합니다.
            """, siteName, temporaryPassword);
    }
}