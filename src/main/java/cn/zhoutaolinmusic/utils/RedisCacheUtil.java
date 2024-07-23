package cn.zhoutaolinmusic.utils;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class RedisCacheUtil {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 普通缓存放入
     * @param key
     * @param value
     * @return
     */
    public boolean set(String key, Object value) {
        try {
            redisTemplate.opsForValue().set(key, value);
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 普通缓存放入并设置时间
     *
     * @param key   键
     * @param value 值
     * @param time  时间(秒) time要大于0 如果time小于等于0 将设置无限期
     * @return true成功 false 失败
     */
    public boolean set(String key, Object value, long time) {
        try {
            if (time > 0) {
                redisTemplate.opsForValue().set(key, value, time, TimeUnit.SECONDS);
            } else {
                // 无限时间
                this.set(key, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    /**
     * 获取缓存
     * @param key
     * @return
     */
    public Object get(String key) {
        return key == null ? null : redisTemplate.opsForValue().get(key);
    }

    /**
     * 递增
     * @param key
     * @param num
     */
    public void increment(String key, long num) {
        if (num <= 0) {
            throw new RuntimeException("递增因子必须大于0");
        }
        redisTemplate.opsForValue().increment(key, num);
    }

    /**
     * 设置set缓存
     * @param key
     * @param value
     */
    public void sSet(String key, Object... value) {
        redisTemplate.opsForSet().add(key, value);
    }

    /**
     * 获取hash缓存
     * @param key
     * @return 对应的多个键值
     */
    public Map<Object, Object> getHashMap(String key) {
        return redisTemplate.opsForHash().entries(key);
    }

    /**
     * 设置hash缓存
     * @param key
     * @param value
     */
    public void setHashMap(String key, Map<Object, Object> value) {
        redisTemplate.opsForHash().putAll(key, value);
    }
}
