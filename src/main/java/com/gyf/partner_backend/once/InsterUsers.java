package com.gyf.partner_backend.once;


import com.gyf.partner_backend.mapper.UserMapper;
import com.gyf.partner_backend.model.domain.User;
import org.springframework.stereotype.Component;
import org.springframework.util.StopWatch;

import javax.annotation.Resource;

@Component
public class InsterUsers {
    @Resource
    private UserMapper userMapper;

    /*
     * @Description: 批量插入用户
     * @Author: lihui
     */
//    @Scheduled(initialDelay = 5000, fixedRate = Long.MAX_VALUE)
    public void doInsertUsers() {
        StopWatch stopWatch = new StopWatch();
        stopWatch.start();
//        final int INSTER_NUM = 10000000;
        final int INSTER_NUM = 1000;

        for (int i = 0; i < INSTER_NUM; i++) {
            User user = new User();
            user.setUsername("假的李慧");
            user.setUserAccount("fakelihui");
            user.setAvatarUrl("http://b.hiphotos.baidu.com/image/pic/item/359b033b5bb5c9ea5c0e3c23d139b6003bf3b374.jpg");
            user.setGender(0);
            user.setUserPassword("cf7a6e3ef218a1d63c758ea0922ea9e7");
            user.setPhone("123");
            user.setEmail("123@qq.com");
            user.setUserStatus(0);
            user.setUserRole(0);
//            user.setPlanetCode("11111111");
            user.setTags("[]");
            userMapper.insert(user);
        }
        stopWatch.stop();
        System.out.println(stopWatch.getTotalTimeMillis());

    }

}
