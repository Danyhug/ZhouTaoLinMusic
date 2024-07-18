package cn.zhoutaolinmusic.exception;

import lombok.Data;

@Data
public class BaseException extends RuntimeException {
    private String msg;

    public BaseException(String msg) {
        this.msg = msg;
    }
}
