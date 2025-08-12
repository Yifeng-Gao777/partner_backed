package com.gyf.partner_backend.service;

import org.junit.jupiter.api.Test;
import org.redisson.api.RList;
import org.redisson.api.RLock;
import org.redisson.api.RMap;
import org.redisson.api.RedissonClient;
import org.springframework.boot.test.context.SpringBootTest;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@SpringBootTest
public class RedissonTest {
    @Resource
    private RedissonClient redissonClient;

    @Test
    void test() {
        // list 数据存在本地JVM内存中
        List<String> list = new ArrayList<>();
        list.add("lihui");
        String JVMlist = list.get(0);
        System.out.println("list:"+JVMlist);
        list.remove(0);

        //数据存在redis的内存中
        RList<String> rList = redissonClient.getList("test-list");
//        rList.add("lihui");
        String Redislist = rList.get(0);
        System.out.println("rlist:"+Redislist);
        rList.remove(0);

        // map
        Map<String, Integer> map = new HashMap<>();
        map.put("lihui", 10);
        map.get("lihui");

        RMap<Object, Object> map1 = redissonClient.getMap("test-map");
    }

    @Test
    void testWatchDog() {
        RLock lock = redissonClient.getLock("yupao:precachejob:docache:lock");
        try {
            // 只有一个线程能获取到锁
            if (lock.tryLock(0, -1, TimeUnit.MILLISECONDS)) {
                Thread.sleep(300000);
                System.out.println("getLock: " + Thread.currentThread().getId());
            }
        } catch (InterruptedException e) {  
            System.out.println(e.getMessage());
        } finally {
            // 只能释放自己的锁
            if (lock.isHeldByCurrentThread()) {
                System.out.println("unLock: " + Thread.currentThread().getId());
                lock.unlock();
            }
        }
    }

}
