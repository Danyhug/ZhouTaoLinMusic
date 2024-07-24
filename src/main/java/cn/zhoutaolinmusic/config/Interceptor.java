package cn.zhoutaolinmusic.config;

import cn.zhoutaolinmusic.entity.user.User;
import cn.zhoutaolinmusic.service.user.UserService;
import cn.zhoutaolinmusic.utils.JwtUtils;
import cn.zhoutaolinmusic.utils.Result;
import cn.zhoutaolinmusic.utils.UserHolder;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.checkerframework.checker.units.qual.A;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.servlet.HandlerInterceptor;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;

// 拦截器
@Component
public class Interceptor implements HandlerInterceptor {
    private static final ObjectMapper objectMapper = new ObjectMapper();
    private final UserService userService;

    public Interceptor(UserService userService) {
        this.userService = userService;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws IOException {
        // 预检直接放行
        if (request.getMethod().equals("OPTIONS")) return true;

        // Token无效直接禁止
        if (!JwtUtils.checkToken(request)) {
            this.send(Result.error("请登录后再操作"), response);
            return false;
        }

        Long userId = JwtUtils.getUserId(request);
        User user = userService.getById(userId);
        if (ObjectUtils.isEmpty(user)) {
            this.send(Result.error("用户不存在"), response);
            return false;
        }

        // 保存用户信息
        UserHolder.add(userId);
        return true;
    }

    private void send(Result<String> result, HttpServletResponse response) throws IOException {
        response.setHeader("Access-Control-Allow-Origin", "*");
        response.setHeader("Cache-Control", "no-cache");
        response.setCharacterEncoding("UTF-8");
        response.setContentType("application/json");

        PrintWriter writer = response.getWriter();
        writer.println(objectMapper.writeValueAsString(result));
        writer.flush();
    }
}
