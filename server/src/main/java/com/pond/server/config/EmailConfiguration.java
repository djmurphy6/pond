//package com.pond.server.config;
//
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.context.annotation.Bean;
//import org.springframework.mail.javamail.JavaMailSender;
//import org.springframework.mail.javamail.JavaMailSenderImpl;
//import org.springframework.context.annotation.Configuration;
//import org.springframework.validation.annotation.Validated;
//
//import java.util.Properties;
//
//@Configuration
//public class EmailConfiguration {
//
//    // Give the email username we are going to send codes from.
//    @Value("${spring.mail.username}")
//    private String emailUsername;
//
//    // Give the password for the admin email that forwards email confirmation codes.
//    @Value("${spring.mail.password}")
//    private String emailPassword;
//
//    // Create the policy and everything we need to send
//    @Bean
//    public JavaMailSender javaMailSender(){
//        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
//        mailSender.setHost("smtp.gmail.com");
//        mailSender.setPort(587);
//        mailSender.setUsername(emailUsername);
//        mailSender.setPassword(emailPassword);
//
//        Properties props =mailSender.getJavaMailProperties();
//        props.put("mail.transport.protocol", "smtp");
//        props.put("mail.smtp.auth", "true");
//        props.put("mail.smtp.starttls.enable", "true");
//        props.put("mail.debug", "true");
//
//        return mailSender;
//    }
//
//}
