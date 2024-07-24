package cn.zhoutaolinmusic.config;

import cn.zhoutaolinmusic.service.user.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    @Autowired
    private UserService userService;
    
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new Interceptor(userService))
                .addPathPatterns("/admin/**","/authorize/**")
                .addPathPatterns("/jjjmusic/**")
                .excludePathPatterns("/jjjmusic/login/**","/jjjmusic/index/**","/jjjmusic/cdn/**", "/jjjmusic/file/**");

    }
}
