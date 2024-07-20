package cn.zhoutaolinmusic;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication
@EnableTransactionManagement
@MapperScan(basePackages = "cn.zhoutaolinmusic.mapper")
public class ZhouTaoLinMusicApplication {
    public static void main(String[] args) {
        SpringApplication.run(ZhouTaoLinMusicApplication.class, args);
    }
}
