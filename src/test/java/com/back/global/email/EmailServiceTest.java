package com.back.global.email;

import com.back.domain.auth.service.AuthService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.RepeatedTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mail.javamail.JavaMailSender;

import java.lang.reflect.Method;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
class EmailServiceTest {

    @Autowired
    private JavaMailSender mailSender;

    @Autowired
    private AuthService authService;

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

    @Test
    @DisplayName("임시 비밀번호는 8자리여야 한다")
    void temporaryPasswordLength() throws Exception {
        // given & when
        String password = generateTemporaryPassword();

        // then
        assertThat(password).hasSize(8);
    }

    @RepeatedTest(10)
    @DisplayName("임시 비밀번호는 영문, 숫자, 특수문자를 각각 1개 이상 포함해야 한다")
    void temporaryPasswordContainsAllTypes() throws Exception {
        // given & when
        String password = generateTemporaryPassword();

        // then
        assertThat(password).hasSize(8);

        // 영문 포함 확인 (대소문자)
        assertThat(password).matches(".*[a-zA-Z].*");

        // 숫자 포함 확인
        assertThat(password).matches(".*[0-9].*");

        // 특수문자 포함 확인
        assertThat(password).matches(".*[!@#$%^&*].*");

        System.out.println("생성된 임시 비밀번호: " + password);
    }

    /**
     * 리플렉션을 사용하여 private 메서드인 generateTemporaryPassword() 호출
     */
    private String generateTemporaryPassword() throws Exception {
        Method method = AuthService.class.getDeclaredMethod("generateTemporaryPassword");
        method.setAccessible(true);
        return (String) method.invoke(authService);
    }
}
