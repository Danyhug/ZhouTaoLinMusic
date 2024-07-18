package cn.zhoutaolinmusic.config;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.PropertyAccessor;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.serializer.Jackson2JsonRedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
public class RedisConfig {
    @Bean
    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory factory) {
        RedisTemplate<String, Object> template = new RedisTemplate<>();
        // 设置连接工厂
        template.setConnectionFactory(factory);

        // 创建一个Jackson2JsonRedisSerializer对象，用于序列化和反序列化对象
        Jackson2JsonRedisSerializer<Object> jackson2JsonRedisSerializer = new Jackson2JsonRedisSerializer<>(Object.class);

        // 创建一个ObjectMapper对象，用于控制JSON序列化和反序列化的行为
        ObjectMapper om = new ObjectMapper();

        // 设置ObjectMapper的可见性，使其可以访问所有属性
        om.setVisibility(PropertyAccessor.ALL, JsonAutoDetect.Visibility.ANY);

        // 启用默认类型信息，以便在序列化和反序列化时可以识别对象的类型
        om.enableDefaultTyping(ObjectMapper.DefaultTyping.NON_FINAL);

        // 将ObjectMapper对象设置到Jackson2JsonRedisSerializer中
        jackson2JsonRedisSerializer.setObjectMapper(om);

        // 创建一个StringRedisSerializer对象，用于序列化和反序列化字符串
        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();

        // 设置RedisTemplate的key序列化器
        template.setKeySerializer(stringRedisSerializer);

        // 设置RedisTemplate的hash key序列化器
        template.setHashKeySerializer(stringRedisSerializer);

        // 设置RedisTemplate的value序列化器
        template.setValueSerializer(jackson2JsonRedisSerializer);

        // 设置RedisTemplate的hash value序列化器
        template.setHashValueSerializer(jackson2JsonRedisSerializer);

        // 调用afterPropertiesSet方法，确保所有的属性都已经设置完毕
        template.afterPropertiesSet();

        // 返回配置好的RedisTemplate对象
        return template;
    }

}
