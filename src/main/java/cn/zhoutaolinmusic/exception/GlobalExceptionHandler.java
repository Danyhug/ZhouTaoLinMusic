package cn.zhoutaolinmusic.exception;

import cn.zhoutaolinmusic.utils.Result;
import org.springframework.util.ObjectUtils;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.StringJoiner;

@RestControllerAdvice
public class GlobalExceptionHandler {
    // 捕获自定义异常
    @ExceptionHandler(BaseException.class)
    public Result<String> baseExceptionHandler(BaseException e) {
        return Result.error(e.getMsg());
    }

    // 捕获校验数据异常
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public Result<String> methodArgumentNotValidExceptionHandler(MethodArgumentNotValidException e) {
        BindingResult bindingResult = e.getBindingResult();
        // 将错误信息以，分隔
        StringJoiner joiner = new StringJoiner(",");

        bindingResult.getFieldErrors().forEach(fieldError -> {
            joiner.add(fieldError.getDefaultMessage());
        });

        return Result.error(joiner.toString());
    }

    // 捕获其他异常
    @ExceptionHandler(Exception.class)
    public Result<String> exceptionHandler(Exception e) {
        e.printStackTrace();
        String msg = ObjectUtils.isEmpty(e.getMessage()) ? e.toString() : e.getMessage();
        return Result.error(msg);
    }
}
