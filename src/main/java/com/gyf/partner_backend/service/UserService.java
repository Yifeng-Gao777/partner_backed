package com.gyf.partner_backend.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.gyf.partner_backend.model.domain.User;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * @author gyf
 * @description 针对表【user(用户)】的数据库操作Service
 * @createDate 2024-07-26 19:06:08
 * <p>
 * 用户服务
 */

public interface UserService extends IService<User> {


    /**
     * 用户注册
     *
     * @param userAccount   用户账户
     * @param userPassword  用户密码
     * @param checkPassword 校验密码
//     * @param planetCode    星球编码
     * @return 新用户id
     */
//    long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode);
    long userRegister(String userAccount, String userPassword, String checkPassword);


    /**
     * 用户登录
     *
     * @param userAccount  用户账户
     * @param userPassword 用户密码
     * @return 脱敏后的用户信息
     */

    User userLogin(String userAccount, String userPassword, HttpServletRequest request);

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    User getSafetyUser(User originUser);

    /**
     * 用户注销
     *
     * @param request
     */

    int userLogout(HttpServletRequest request);

    /**
     * 根据标签搜索用户
     *
     * @param tagNameList
     * @return
     */
    List<User> searchUsersByTags(List<String> tagNameList);


    /*
     * 更新用户信息
     * @param user
     * @return
     */
    int updateUser(User user, User loginUser);

    /**
     * 获取当前登录用户
     *
     * @return
     */
    User getLoginUser(HttpServletRequest request);

    /**
     * * 鉴权 仅管理员可见
     *
     * @param request
     * @return
     */
    boolean isAdmin(HttpServletRequest request);

    /**
     * * 鉴权 仅管理员可见
     *
     * @param loginUser
     * @return
     */

    boolean isAdmin(User loginUser);

    /**
     * 根据用户标签匹配用户
     * @param num
     * @param loginUser
     * @return
     */

    List<User> matchUsers(long num, User loginUser);


    int updateTags(String oldTag, String newTag, String operation, User loginUser);

//    int updateUserTags(User user, User loginUser);
}
