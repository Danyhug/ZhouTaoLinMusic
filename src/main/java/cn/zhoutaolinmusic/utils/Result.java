package cn.zhoutaolinmusic.utils;

import lombok.Data;

import java.io.Serializable;

@Data
public class Result<T> implements Serializable {
    private T type;

    // 响应码 0成功1失败
    private int code;
    // 返回状态
    private boolean state;
    // 返回消息
    private String message;
    // 返回数据
    private Object data;
    // 返回的数据总数
    private long count;

    public static <T> Result<T> ok() {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setState(true);
        result.setMessage("成功");
        return result;
    }

    public static Result<String> ok(String msg) {
        Result<String> result = new Result<>();
        result.setCode(0);
        result.setState(true);
        result.setMessage(msg);
        return result;
    }

    public static <T> Result<T> ok(T data) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setState(true);
        result.setData(data);
        return result;
    }

    public static <T> Result<T> ok(T data, long count) {
        Result<T> result = new Result<>();
        result.setCode(0);
        result.setState(true);
        result.setData(data);
        result.setCount(count);
        return result;
    }

    public static <T> Result<T> error() {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage("失败");
        return result;
    }

    public static <T> Result<T> error(String msg) {
        Result<T> result = new Result<>();
        result.setCode(1);
        result.setMessage(msg);
        return result;
    }
}
