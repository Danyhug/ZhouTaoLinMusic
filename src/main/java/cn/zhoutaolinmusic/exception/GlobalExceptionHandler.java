package cn.zhoutaolinmusic.exception;

import cn.zhoutaolinmusic.utils.Result;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 捕获自定义异常
    @ExceptionHandler(BaseException.class)
    public Result<String> baseExceptionHandler(BaseException e) {
        return Result.error(e.getMsg());
    }

    // 捕获其他异常
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception e) {
        e.printStackTrace();
        String msg = ObjectUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
        return Result.error(msg);
    }
}
