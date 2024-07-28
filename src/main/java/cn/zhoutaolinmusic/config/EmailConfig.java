package cn.zhoutaolinmusic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

@Configuration
public class EmailConfig {

    @Bean
    public SimpleMailMessage simpleMailMessage(){
        return new SimpleMailMessage();
    }
}
