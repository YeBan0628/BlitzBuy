package cn.hfbin.seckill.redis;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.serializer.GenericToStringSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;
import org.springframework.stereotype.Service;

import com.alibaba.fastjson.JSON;

import java.util.Collections;
import java.util.concurrent.TimeUnit;

@Service
public class RedisService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 秒杀库存原子扣减脚本（判断+扣减在一次 EVAL 内完成，避免超卖）
     */
    private static final DefaultRedisScript<Long> STOCK_DECR_SCRIPT;

    static {
        STOCK_DECR_SCRIPT = new DefaultRedisScript<>();
        STOCK_DECR_SCRIPT.setLocation(new ClassPathResource("lua/seckill_stock.lua"));
        STOCK_DECR_SCRIPT.setResultType(Long.class);
        STOCK_DECR_SCRIPT.afterPropertiesSet();
    }

    /**
     * 原子扣减秒杀库存。
     *
     * @return >=0 扣减后的剩余库存；-1 已售罄；-2 库存未初始化
     */
    public long seckillStockDecr(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        // key 用 StringRedisSerializer 序列化，与写入库存时保持一致
        Long result = redisTemplate.execute(STOCK_DECR_SCRIPT,
                new StringRedisSerializer(),
                new GenericToStringSerializer<>(Long.class),
                Collections.singletonList(realKey));
        return result == null ? -2 : result;
    }

    /**
     * 获取当个对象
     */
    public <T> T get(KeyPrefix prefix, String key, Class c) {
        //生成真正的key
        String realKey = prefix.getPrefix() + key;
        return (T) redisTemplate.opsForValue().get(realKey);
    }

    public void expire(KeyPrefix prefix, String key, int exTime) {
        String realKey = prefix.getPrefix() + key;
        redisTemplate.expire(realKey, exTime, TimeUnit.SECONDS);
    }

    /**
     * 设置对象
     */
    public <T> Boolean set(KeyPrefix prefix, String key, T value, int exTime) {
        String realKey = prefix.getPrefix() + key;
        try {
            if (exTime > 0) {
                redisTemplate.opsForValue().set(realKey, value, exTime, TimeUnit.SECONDS);
            } else {
                redisTemplate.opsForValue().set(realKey, value);
            }
            return true;
        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }

    public Boolean del(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.delete(realKey);
    }

    /**
     * 判断key是否存在
     */
    public <T> Boolean exists(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.hasKey(realKey);
    }

    /**
     * 增加值
     */
    public <T> Long incr(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().increment(realKey);
    }

    /**
     * 减少值
     */
    public <T> Long decr(KeyPrefix prefix, String key) {
        String realKey = prefix.getPrefix() + key;
        return redisTemplate.opsForValue().decrement(realKey);
    }

    /**
     * bean 转 String
     *
     * @param value
     * @param <T>
     * @return
     */
    public static <T> String beanToString(T value) {
        if (value == null) {
            return null;
        }
        Class<?> clazz = value.getClass();
        if (clazz == int.class || clazz == Integer.class) {
            return "" + value;
        } else if (clazz == String.class) {
            return (String) value;
        } else if (clazz == long.class || clazz == Long.class) {
            return "" + value;
        } else {
            return JSON.toJSONString(value);
        }
    }


    /**
     * string转bean
     *
     * @param str
     * @param clazz
     * @param <T>
     * @return
     */
    public static <T> T stringToBean(String str, Class<T> clazz) {
        if (str == null || str.length() <= 0 || clazz == null) {
            return null;
        }
        if (clazz == int.class || clazz == Integer.class) {
            return (T) Integer.valueOf(str);
        } else if (clazz == String.class) {
            return (T) str;
        } else if (clazz == long.class || clazz == Long.class) {
            return (T) Long.valueOf(str);
        } else {
            return JSON.toJavaObject(JSON.parseObject(str), clazz);
        }
    }

}
