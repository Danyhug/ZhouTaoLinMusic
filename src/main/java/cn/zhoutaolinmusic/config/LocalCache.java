package cn.zhoutaolinmusic.config;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

// 本地缓存
public class LocalCache {
    // 为了适配多线程，使用ConcurrentHashMap
    private static Map<String, Object> cache = new ConcurrentHashMap<>();

    public static void put(String key, Object value) {
        cache.put(key, value);
    }

    public static Boolean containsKey(String key) {
        return cache.containsKey(key);
    }

    public static void rm(String key) {
        cache.remove(key);
    }
}
