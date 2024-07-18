package cn.zhoutaolinmusic.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.SimpleMailMessage;

/**
 * @description:
 * @Author: Xhy
 * @CreateTime: 2023-10-25 12:11
 */
@Configuration
public class EmailConfig {

    @Bean
    public SimpleMailMessage simpleMailMessage(){
        return new SimpleMailMessage();
    }
}
