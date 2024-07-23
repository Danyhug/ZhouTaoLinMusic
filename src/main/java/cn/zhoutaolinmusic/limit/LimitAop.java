package cn.zhoutaolinmusic.limit;

import cn.zhoutaolinmusic.constant.RedisConstant;
import cn.zhoutaolinmusic.exception.BaseException;
import cn.zhoutaolinmusic.utils.RedisCacheUtil;
import cn.zhoutaolinmusic.utils.UserHolder;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Aspect
@Component
public class LimitAop {

    @Autowired
    private RedisCacheUtil redisCacheUtil;

    @Around("@annotation(limiter)")
    public Object restriction(ProceedingJoinPoint joinPoint, Limit limiter) throws Throwable {
        Long userId = UserHolder.get();
        int limitCount = limiter.limit();
        String msg = limiter.msg();
        long time = limiter.time();

        // 判断缓存是否存在
        String key = RedisConstant.VIDEO_LIMIT + userId;
        Object obj = redisCacheUtil.get(key);
        if (ObjectUtils.isEmpty(obj)) {
            // 没有缓存，新增一个
            redisCacheUtil.set(key, 1, time);
        } else {
            // 已有缓存，判断缓存值是否大于限制值
            if (Integer.parseInt(obj.toString()) >= limitCount) {
                throw new BaseException(msg);
            } else {
                // 缓存值小于限制值，增加缓存值
                redisCacheUtil.increment(key, 1);
            }
        }
        // 执行方法
        return joinPoint.proceed();
    }
}
