package com.gyf.partner_backend.job;


import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyf.partner_backend.mapper.UserMapper;
import com.gyf.partner_backend.model.domain.User;
import com.gyf.partner_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import javax.annotation.Resource;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.TimeUnit;


@Component
@Slf4j
public class PreCacheJob {

    @Resource
    private UserMapper userMapper;
    @Resource
    private UserService userService;
    @Resource
    private RedisTemplate<String, Object> redisTemplate;
    @Resource
    private RedissonClient redissonClient;

    //重点用户
    private List<Long> mainUserList = Arrays.asList(1l);

    //每天执行一次，预热推荐用户 缓存
    @Scheduled(cron = "0 0 0 * * ?")
    public void doCacherecommendUser() {
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            //只有一个线程能获取到锁
            if (lock.tryLock(0, 30000, TimeUnit.MILLISECONDS)) {
                for (Long UserId : mainUserList) {
                    // 如果没有缓存，则查询数据库
                    QueryWrapper<User> queryWrapper = new QueryWrapper<>();
                    Page<User> userPage = userService.page(new Page<>(1, 20), queryWrapper);
                    String reidsKey = String.format("yupao:user:recommed:%s", UserId);
                    ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
                    // 将查询结果存入缓存
                    try {
                        valueOperations.set(reidsKey, userPage, 30000, TimeUnit.MILLISECONDS);
                    } catch (Exception e) {
                        log.error("redis set error", e);
                    }
                }
            }
        } catch (InterruptedException e) {
            log.error("doCacheJob error", e);
        } finally {
            //只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                lock.unlock();
            }
        }


    }

}
