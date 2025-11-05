package com.pond.server.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;

@Service
public class EmailService {

    @Autowired
    private JavaMailSender emailSender;

    public void sendVerificationEmail(String to, String verificationCode) {
        try {
            MimeMessage message = emailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true);

            helper.setTo(to);
            helper.setSubject("Verify Your Pawnd Account");
            
            String htmlContent = buildVerificationEmailHtml(verificationCode);
            helper.setText(htmlContent, true);

            emailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Failed to send verification email", e);
        }
    }

    private String buildVerificationEmailHtml(String verificationCode) {
        return """
            <!DOCTYPE html>
            <html>
            <head>
                <style>
                    body { font-family: Arial, sans-serif; line-height: 1.6; color: #333; }
                    .container { max-width: 600px; margin: 0 auto; padding: 20px; }
                    .header { background-color: #18453B; color: white; padding: 20px; text-align: center; border-radius: 5px 5px 0 0; }
                    .content { background-color: #f9f9f9; padding: 30px; border-radius: 0 0 5px 5px; }
                    .code { font-size: 32px; font-weight: bold; color: #18453B; text-align: center; padding: 20px; background-color: white; border-radius: 5px; letter-spacing: 5px; }
                    .footer { text-align: center; margin-top: 20px; font-size: 12px; color: #666; }
                </style>
            </head>
            <body>
                <div class="container">
                    <div class="header">
                        <h1>Welcome to Pawnd!</h1>
                    </div>
                    <div class="content">
                        <p>Thank you for signing up! Please use the verification code below to verify your email address:</p>
                        <div class="code">%s</div>
                        <p>This code will expire in 15 minutes.</p>
                        <p>If you didn't create an account with Pawnd, please ignore this email.</p>
                    </div>
                    <div class="footer">
                        <p>© 2025 Pawnd - University of Oregon</p>
                    </div>
                </div>
            </body>
            </html>
            """.formatted(verificationCode);
    }
}
