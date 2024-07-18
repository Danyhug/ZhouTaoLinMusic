package cn.zhoutaolinmusic.service;

public interface EmailService {
    void send(String email, String subject, String context);
}
