package com.securenotes.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;
import java.util.concurrent.ExecutorService;

@Configuration
public class EmailConfig {

    @Value("${spring.mail.host}")
    private String emailHost;

    @Value("${spring.mail.port}")
    private String emailPort;

    @Value("${spring.mail.username}")
    private String emailUsername;

    @Value("${spring.mail.password}")
    private String emailPassword;


    @Bean
    public JavaMailSender getJavaMailSender(){
        JavaMailSenderImpl javaMailSender = new JavaMailSenderImpl();
        javaMailSender.setHost(emailHost);
        javaMailSender.setPort(Integer.parseInt(emailPort));
        javaMailSender.setPassword(emailPassword);
        javaMailSender.setUsername(emailUsername);

        Properties properties = javaMailSender.getJavaMailProperties();
        properties.put("mail.smtp.starttls.enable","true");
        return  javaMailSender;

    }

    public ExecutorService executorService{
        return executorService.
    }
}
