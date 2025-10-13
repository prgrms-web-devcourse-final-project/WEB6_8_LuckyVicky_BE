package com.back.global.email;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private JavaMailSender mailSender;

    // 동작 확인했습니다 - 테스트 시, 실제로 이메일이 발송되니 주석 처리 해두었습니다.
    /*
    @Test
    void testEmailConnection() {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo("ahdeka01@gmail.com"); // 받는 사람 이메일 주소
        message.setSubject("테스트 메일");
        message.setText("설정 테스트입니다.");

        mailSender.send(message);
        System.out.println("이메일 발송 성공!");
    }
    */
}
