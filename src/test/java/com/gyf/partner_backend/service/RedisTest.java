package com.gyf.partner_backend.service;

import com.gyf.partner_backend.model.domain.User;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import javax.annotation.Resource;

/**
 * Redis 测试
 *
 * @author lihui
 */
@SpringBootTest
public class RedisTest {

    @Resource
    private RedisTemplate redisTemplate;

    @Test
    void test() {
        ValueOperations valueOperations = redisTemplate.opsForValue();
        // 增
        valueOperations.set("lihuiString", "dog");
        valueOperations.set("lihuiInt", 1);
        valueOperations.set("lihuiDouble", 2.0);
        User user = new User();
        user.setId(1L);
        user.setUsername("lihui");
        valueOperations.set("lihuiUser", user);
        // 查
        Object lihui = valueOperations.get("lihuiString");
        Assertions.assertTrue("dog".equals((String) lihui));
        lihui = valueOperations.get("lihuiInt");
        Assertions.assertTrue(1 == (Integer) lihui);
        lihui = valueOperations.get("lihuiDouble");
        Assertions.assertTrue(2.0 == (Double) lihui);
        System.out.println(valueOperations.get("lihuiUser"));
        valueOperations.set("lihuiString", "dog");
        redisTemplate.delete("lihuiString");
    }
}
