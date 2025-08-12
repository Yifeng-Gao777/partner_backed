//
//package com.lihui.yupao_backend.service;
//
//import com.lihui.yupao_backend.mapper.UserMapper;
//import com.lihui.yupao_backend.model.domain.User;
//import org.junit.jupiter.api.Test;
//import org.springframework.boot.test.context.SpringBootTest;
//import org.springframework.util.StopWatch;
//
//import javax.annotation.Resource;
//import java.util.ArrayList;
//import java.util.List;
//import java.util.concurrent.*;
//
//@SpringBootTest
//public class InsterUserTest {
//    @Resource
//    private UserMapper userMapper;
////    private ExecutorService executorService = new ThreadPoolExecutor(40, 1000, 10000, TimeUnit.MINUTES, new ArrayBlockingQueue<>(10000));
//
//
//    /*
//     * @Description: 批量插入用户
//     * @Author: lihui
//     */
//    @Test
//    public void doInsertUsers() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
////        final int INSTER_NUM = 10000000;
//        final int INSTER_NUM = 1000;
//
//        for (int i = 0; i < INSTER_NUM; i++) {
//            User user = new User();
//            user.setUsername("假的李慧");
//            user.setUserAccount("fakelihui");
//            user.setAvatarUrl("http://b.hiphotos.baidu.com/image/pic/item/359b033b5bb5c9ea5c0e3c23d139b6003bf3b374.jpg");
//            user.setGender(0);
//            user.setUserPassword("cf7a6e3ef218a1d63c758ea0922ea9e7");
//            user.setPhone("123");
//            user.setEmail("123@qq.com");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("11111111");
//            user.setTags("[]");
//            userMapper.insert(user);
//        }
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//
//    }
//
//    /**
//     * 批量插入用户 userService + ArrayList + saveBatch
//     */
//    @Resource
//    private UserService userService;
//
//    @Test
//    public void doInsertUsers2() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
//        final int INSTER_NUM = 9000000;
//        List<User> userList = new ArrayList<>();
//        for (int i = 0; i < INSTER_NUM; i++) {
//            User user = new User();
//            user.setUsername("假的李慧");
//            user.setUserAccount("fakelihui");
//            user.setAvatarUrl("http://b.hiphotos.baidu.com/image/pic/item/359b033b5bb5c9ea5c0e3c23d139b6003bf3b374.jpg");
//            user.setGender(0);
//            user.setUserPassword("cf7a6e3ef218a1d63c758ea0922ea9e7");
//            user.setPhone("123");
//            user.setEmail("123@qq.com");
//            user.setUserStatus(0);
//            user.setUserRole(0);
//            user.setPlanetCode("11111111");
//            user.setTags("[]");
////            userService.insert(user);
//            userList.add(user);
//        }
//        userService.saveBatch(userList, 900000);
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//
//    }
//
//
//    /**
//     * 并发插入用户
//     */
//    @Test
//    public void doInsertUsers3() {
//        StopWatch stopWatch = new StopWatch();
//        stopWatch.start();
////        final int INSTER_NUM = 5000;
////        分成10组
//        int j = 0;
//        int bandSize = 5000;
//        List<CompletableFuture<Void>> futureList = new ArrayList<>();
//        for (int i = 0; i < 20; i++) {
//            List<User> userList = new ArrayList<>();
//            while (true) {
//                j++;
//                User user = new User();
//                user.setUsername("假的李慧");
//                user.setUserAccount("fakelihui");
//                user.setAvatarUrl("http://b.hiphotos.baidu.com/image/pic/item/359b033b5bb5c9ea5c0e3c23d139b6003bf3b374.jpg");
//                user.setGender(0);
//                user.setUserPassword("cf7a6e3ef218a1d63c758ea0922ea9e7");
//                user.setPhone("123");
//                user.setEmail("123@qq.com");
//                user.setUserStatus(0);
//                user.setUserRole(0);
//                user.setPlanetCode("11111111");
//                user.setTags("[]");
//                if (j % bandSize == 0) {
//                    break;
//                }
//            }
//            //异步执行
//            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
//                userService.saveBatch(userList, bandSize);
//            });
//            futureList.add(future);
//        }
//
//        CompletableFuture.allOf(futureList.toArray(new CompletableFuture[]{})).join();
//        stopWatch.stop();
//        System.out.println(stopWatch.getTotalTimeMillis());
//
//    }
//}
