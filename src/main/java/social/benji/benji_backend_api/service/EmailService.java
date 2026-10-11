package social.benji.benji_backend_api.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailService {

    private final JavaMailSender mailSender;

    public void sendOtpEmail(String to, String otp) {
        log.info("Sending OTP email to: {}", to);
        
        SimpleMailMessage message = new SimpleMailMessage();
        message.setTo(to);
        message.setSubject("Your OTP Code for Benji Pet Platform");
        message.setText("Your One-Time Password (OTP) is: " + otp + 
            "\n\nThis OTP is valid for 10 minutes. Do not share it with anyone." +
            "\n\nIf you did not request this code, please ignore this email.");
        
        mailSender.send(message);
        log.info("OTP email sent successfully to: {}", to);
    }
}
