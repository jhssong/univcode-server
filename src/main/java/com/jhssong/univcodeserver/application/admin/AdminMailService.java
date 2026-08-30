package com.jhssong.univcodeserver.application.admin;

import com.jhssong.univcode.UnivCodeProperties;
import com.jhssong.univcodeserver.global.exception.CustomException;
import com.jhssong.univcodeserver.global.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class AdminMailService {

    private final JavaMailSender mailSender;
    private final UnivCodeProperties univCodeProperties;

    @Async
    public void sendManual(String to, String subject, String body) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom("UnivCode <" + univCodeProperties.getMailFrom() + ">");
        message.setTo(to);
        message.setSubject(subject);
        message.setText(body);
        try {
            mailSender.send(message);
        } catch (Exception e) {
            throw new CustomException(ErrorCode.MANUAL_MAIL_SEND_FAILED);
        }
    }
}
