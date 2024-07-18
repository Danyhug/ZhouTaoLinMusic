package cn.zhoutaolinmusic.service.user.impl;

import cn.zhoutaolinmusic.service.EmailService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

@Service
public class EmailServiceImpl implements EmailService {
    @Autowired
    private SimpleMailMessage simpleMailMessage;

    @Autowired
    JavaMailSender javaMailSender;

    @Value("${spring.mail.username}")
    String fromName;

    @Override
    @Async
    public void send(String email, String subject, String context) {
        simpleMailMessage.setSubject("周陶林乐");
        simpleMailMessage.setSubject(subject);
        simpleMailMessage.setFrom(fromName);
        simpleMailMessage.setTo(email);
        simpleMailMessage.setText(context);
        System.out.println(simpleMailMessage);
        javaMailSender.send(simpleMailMessage);
    }
}
