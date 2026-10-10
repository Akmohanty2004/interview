package com.interviewprep.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String senderEmail;

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendOtpEmail(String to, String otp) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(senderEmail, "InterviewPrep Platform");
            helper.setTo(to);
            helper.setSubject("Your Secure Verification Code");
            
            String htmlContent = "<div style=\"font-family: Helvetica,Arial,sans-serif;min-width:1000px;overflow:auto;line-height:2\">" +
                    "<div style=\"margin:50px auto;width:70%;padding:20px 0\">" +
                    "<div style=\"border-bottom:1px solid #eee\">" +
                    "<a href=\"#\" style=\"font-size:1.4em;color: #4f46e5;text-decoration:none;font-weight:600\">InterviewPrep Platform</a>" +
                    "</div>" +
                    "<p style=\"font-size:1.1em\">Hi,</p>" +
                    "<p>Thank you for registering on InterviewPrep. Use the following OTP to complete your email verification. This OTP is valid for 10 minutes.</p>" +
                    "<h2 style=\"background: #4f46e5;margin: 0 auto;width: max-content;padding: 0 15px;color: #fff;border-radius: 6px;letter-spacing: 2px;\">" + otp + "</h2>" +
                    "<p style=\"font-size:0.9em;\">Regards,<br />The InterviewPrep Team</p>" +
                    "<hr style=\"border:none;border-top:1px solid #eee; margin-top: 40px;\" />" +
                    "<div style=\"float:right;padding:8px 0;color:#aaa;font-size:0.8em;line-height:1;font-weight:300\">" +
                    "<p>InterviewPrep Platform</p>" +
                    "<p>Empowering Students</p>" +
                    "</div>" +
                    "</div>" +
                    "</div>";
            
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
            logger.info("HTML OTP email sent successfully to {}", to);
        } catch (Exception e) {
            logger.error("Failed to send OTP email to {}", to, e);
        }
    }
}
