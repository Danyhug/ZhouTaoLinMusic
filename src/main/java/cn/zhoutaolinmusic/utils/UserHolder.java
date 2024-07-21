package cn.zhoutaolinmusic.utils;

public class UserHolder {
    private static final ThreadLocal<Long> userThreadLocal = new ThreadLocal<>();
    // 添加
    public static void add(Long userId) {
        userThreadLocal.set(userId);
    }
    // 获取
    public static Long get() {
        return userThreadLocal.get();
    }
    // 删除
    public static void remove() {
        userThreadLocal.remove();
    }
}
