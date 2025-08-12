package com.gyf.partner_backend.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import com.gyf.partner_backend.common.ErrorCode;
import com.gyf.partner_backend.exception.BusinessException;
import com.gyf.partner_backend.exception.GlobalExceptionHandler;
import com.gyf.partner_backend.model.domain.User;
import com.gyf.partner_backend.service.UserService;

import com.gyf.partner_backend.mapper.UserMapper;
import com.gyf.partner_backend.utils.AlgorithmUtils;
import javafx.util.Pair;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.DigestUtils;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static com.gyf.partner_backend.contant.UserContant.ADMIN_ROLE;
import static com.gyf.partner_backend.contant.UserContant.USER_LOGIN_STATE;


/**
 * @author lihui
 * @author lihui
 * @description 针对表【user(用户)】的数据库操作Service实现
 * @createDate 2024-07-26 19:06:08
 * <p>
 * 用户实现类
 */
@Service
@Slf4j
public class UserServiceImpl extends ServiceImpl<UserMapper, User> implements UserService {
    @Resource
    private UserMapper userMapper;
    /**
     * 加盐混淆密码
     */
    final static String SALT = "lihui"; //加盐
    @Autowired
    private GlobalExceptionHandler globalExceptionHandler;
    //    /**
    //     * 用户登录态的key
    //     */
    //    public static String USER_LOGIN_STATE = "userLoginState";

    @Override
//    public long userRegister(String userAccount, String userPassword, String checkPassword, String planetCode) {
    public long userRegister(String userAccount, String userPassword, String checkPassword) {
        // 1. 校验
        // 非空
//        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "参数为空");
        }
        // 账户长度不小于4位，密码长度不小于8位
        if (userAccount.length() < 4) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户账号过短");
        }
        if (userPassword.length() < 8 || checkPassword.length() < 8) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户密码过短");
        }
//        if (planetCode.length() > 5) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "星球编号过长");
//        }
        // 账户不能包含特殊字符
        String validPattern = "\\W";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) { // 不要写反
            return -1;
        }
        // 账户不能重复
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        long count = userMapper.selectCount(queryWrapper);
        if (count > 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "账号重复");
        }

        // 星球编号不重复
//        queryWrapper = new QueryWrapper<>();
//        queryWrapper.eq("planetCode", planetCode);
//        count = userMapper.selectCount(queryWrapper);
//        if (count > 0) {
//            throw new BusinessException(ErrorCode.PARAMS_ERROR, "编号重复");
//        }

        // 密码校验
        if (!userPassword.equals(checkPassword)) {
            return -1;
        }
        // 2. 加密
//        final String SALT = "lihui";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        // 3. 插入数据
        User user = new User();
        user.setUserAccount(userAccount);
        user.setUserPassword(encryptPassword);
//        user.setPlanetCode(planetCode);
        //todo 默认头像
        boolean saveResult = this.save(user);
        if (!saveResult) {
            return -1;
        }
        return user.getId();
    }

    @Override
    public User userLogin(String userAccount, String userPassword, HttpServletRequest request) {
        //todo 修改自定义异常

        // 1. 校验
        // 非空
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return null;
        }
        // 账户长度不小于4位，密码长度不小于8位
        if (userAccount.length() < 4) {
            return null;
        }
        if (userPassword.length() < 8) {
            return null;
        }
        // 账户不能包含特殊字符
        String validPattern = "\\W";
        Matcher matcher = Pattern.compile(validPattern).matcher(userAccount);
        if (matcher.find()) { // 不要写反
            return null;
        }
        // 2.查询用户是否存在
//        final String SALT = "lihui";
        String encryptPassword = DigestUtils.md5DigestAsHex((SALT + userPassword).getBytes());
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userAccount", userAccount);
        queryWrapper.eq("userPassword", encryptPassword);
        User user = userMapper.selectOne(queryWrapper);
        if (user == null) {
            log.info("user login failed,userAccount cannot match userPassword");
            return null;
        }
        //3.用户脱敏
//        User safeUser = new User();
//        safeUser.setId(user.getId());
//        safeUser.setUsername(user.getUsername());
//        safeUser.setUserAccount(user.getUserAccount());
//        safeUser.setAvatarUrl(user.getAvatarUrl());
//        safeUser.setGender(user.getGender());
//        safeUser.setPhone(user.getPhone());
//        safeUser.setEmail(user.getEmail());
//        safeUser.setUserRole(user.getUserRole());
//        safeUser.setUserStatus(user.getUserStatus());
//        safeUser.setCreateTime(user.getCreateTime());
//        safeUser.setIsDelete(0);

        User safeUser = getSafetyUser(user);

        //4.记录用户的登录态
        request.getSession().setAttribute(USER_LOGIN_STATE, safeUser);
        return safeUser;
    }

    /**
     * 用户脱敏
     *
     * @param originUser
     * @return
     */
    @Override
    public User getSafetyUser(User originUser) {
        if (originUser == null) {
            return null;
        }
        User safeUser = new User();
        safeUser.setId(originUser.getId());
        safeUser.setUsername(originUser.getUsername());
        safeUser.setUserAccount(originUser.getUserAccount());
        safeUser.setAvatarUrl(originUser.getAvatarUrl());
        safeUser.setGender(originUser.getGender());
        safeUser.setPhone(originUser.getPhone());
        safeUser.setEmail(originUser.getEmail());
//        safeUser.setPlanetCode(originUser.getPlanetCode());
        safeUser.setUserRole(originUser.getUserRole());
        safeUser.setUserStatus(originUser.getUserStatus());
        safeUser.setCreateTime(originUser.getCreateTime());
        safeUser.setIsDelete(0);
        safeUser.setTags(originUser.getTags()); //用户标签json
        safeUser.setProfile(originUser.getProfile()); //用户简介
        return safeUser;

    }

    /**
     * 用户注销
     *
     * @param request
     * @return
     */
    @Override
    public int userLogout(HttpServletRequest request) {
        //移除登录
        request.getSession().removeAttribute(USER_LOGIN_STATE);
        return 1;
    }

    /**
     * 根据标签搜索用户 内存查询
     */
    @Override
    public List<User> searchUsersByTags(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 1. 查询所有的用户
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        List<User> userList = userMapper.selectList(queryWrapper);

        Gson gson = new Gson();

        // 2. 遍历用户列表，查询标签列表并判断是否包含要求的标签（不区分大小写）
        return userList.stream().filter(user -> {
                    String tagsStr = user.getTags();
                    if (StringUtils.isBlank(tagsStr)) {
                        return false; // 如果标签为空，跳过
                    }

                    // 获取用户标签集合，并确保它是 Set<String> 类型
                    Set<String> userTags = gson.fromJson(tagsStr, new TypeToken<Set<String>>() {
                    }.getType());

                    if (userTags == null || userTags.isEmpty()) {
                        return false; // 如果解析结果为空或不是有效的集合，跳过
                    }

                    // 将用户标签集合转为小写
                    userTags = userTags.stream()
                            .map(tag -> tag.toLowerCase())  // 转为小写
                            .collect(Collectors.toSet());

                    // 将传入的标签列表转换为小写
                    List<String> lowercaseTagNames = tagNameList.stream()
                            .map(String::toLowerCase)  // 转为小写
                            .collect(Collectors.toList());

                    // 判断每个标签是否都存在（不区分大小写）
                    for (String tagName : lowercaseTagNames) {
                        if (!userTags.contains(tagName)) {
                            return false;
                        }
                    }

                    return true;
                })
                .map(this::getSafetyUser)  // 转换为安全的用户对象
                .collect(Collectors.toList());
    }


    @Override
    public int updateUser(User user, User loginUser) {
        long userId = loginUser.getId();
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        boolean hasUpdates =
                StringUtils.isNotBlank(user.getUsername()) ||
                        StringUtils.isNotBlank(user.getAvatarUrl()) ||
                        user.getGender() != null ||
                        StringUtils.isNotBlank(user.getPhone()) ||
                        StringUtils.isNotBlank(user.getEmail()) ||
                        StringUtils.isNotBlank(user.getProfile());

        if (!hasUpdates) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "没有传递任何要更新的值");
        }

        if (!isAdmin(loginUser) && userId != loginUser.getId()) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR);
        }
        return userMapper.updateById(user);
    }


    @Override
    public User getLoginUser(HttpServletRequest request) {
        if (request == null) {
            return null;
        }
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);

        if (userObj == null) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }

        return (User) userObj;
    }

    /**
     * 鉴权 仅管理员可见
     *
     * @param request
     * @return
     */
    @Override
    public boolean isAdmin(HttpServletRequest request) {
        //鉴权 仅管理员可见
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User user = (User) userObj;
        if (user == null || user.getUserRole() != ADMIN_ROLE) { //用常量替换
            return false;
        }
        return true;
    }

    /**
     * 鉴权 仅管理员可见
     *
     * @param loginUser
     * @return
     */
    @Override
    public boolean isAdmin(User loginUser) {
        if (loginUser == null || loginUser.getUserRole() != ADMIN_ROLE) { //用常量替换
            return false;
        }
        return true;
    }

    @Override
    public List<User> matchUsers(long num, User loginUser) {
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        queryWrapper.select("id", "tags");
        queryWrapper.isNotNull("tags");
        List<User> userList = this.list(queryWrapper);
        String tags = loginUser.getTags();
        Gson gson = new Gson();
        List<String> tagList = gson.fromJson(tags, new TypeToken<List<String>>() {
        }.getType());
        // 用户列表的下标 => 相似度
        List<Pair<User, Long>> list = new ArrayList<>();
        // 依次计算所有用户和当前用户的相似度
        for (int i = 0; i < userList.size(); i++) {
            User user = userList.get(i);
            String userTags = user.getTags();
            // 无标签或者为当前用户自己
            if (StringUtils.isBlank(userTags) || user.getId().equals(loginUser.getId())) {
                continue;
            }
            List<String> userTagList = gson.fromJson(userTags, new TypeToken<List<String>>() {
            }.getType());
            // 计算分数
            long distance = AlgorithmUtils.minDistance(tagList, userTagList);
            list.add(new Pair<>(user, distance));
        }
        // 按编辑距离由小到大排序
        List<Pair<User, Long>> topUserPairList = list.stream()
                .sorted((a, b) -> (int) (a.getValue() - b.getValue()))
                .limit(num)
                .collect(Collectors.toList());
//        List<User> userVOList = topUserPairList.stream().map(Pair::getKey).collect(Collectors.toList());
//        return userVOList;
        // 原本顺序的 userId 列表
        List<Long> userIdList = topUserPairList.stream().map(pair -> pair.getKey().getId()).collect(Collectors.toList());
        QueryWrapper<User> userQueryWrapper = new QueryWrapper<>();
        userQueryWrapper.in("id", userIdList);
        // 1, 3, 2
        // User1、User2、User3
        // 1 => User1, 2 => User2, 3 => User3
        Map<Long, List<User>> userIdUserListMap = this.list(userQueryWrapper)
                .stream()
                .map(user -> getSafetyUser(user))
                .collect(Collectors.groupingBy(User::getId));
        List<User> finalUserList = new ArrayList<>();
        for (Long userId : userIdList) {
            finalUserList.add(userIdUserListMap.get(userId).get(0));
        }
        return finalUserList;
    }

    @Override
    public int updateTags(String oldTag, String newTag, String operation, User loginUser) {
        long userId = loginUser.getId();
        if (userId < 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "用户ID无效");
        }

        User oldUser = userMapper.selectById(userId);
        if (oldUser == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "用户不存在");
        }

        // 1. 获取当前用户的标签并转换成列表
        List<String> currentTags = convertJsonToTags(oldUser.getTags());

        // 2. 根据操作类型处理标签
        switch (operation.toLowerCase()) {
            case "add":
                if (!currentTags.contains(newTag)) {
                    currentTags.add(newTag);  // 添加新标签
                } else {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签已存在");
                }
                break;

            case "remove":
                if (currentTags.contains(oldTag)) {
                    currentTags.remove(oldTag);  // 删除旧标签
                    // 如果标签列表中删除后出现空字符串，移除空字符串
                    currentTags.removeIf(tag -> tag.isEmpty());
                } else {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到指定的标签");
                }
                break;

            case "update":
                if (currentTags.contains(oldTag)) {
                    int index = currentTags.indexOf(oldTag);  // 获取旧标签的索引
                    currentTags.set(index, newTag);  // 替换为新标签
                } else {
                    throw new BusinessException(ErrorCode.PARAMS_ERROR, "未找到指定的标签");
                }
                break;

            default:
                throw new BusinessException(ErrorCode.PARAMS_ERROR, "无效的操作类型");
        }

        // 3. 更新数据库中的标签字段
        String updatedTagsJson = convertTagsToJson(currentTags);
        oldUser.setTags(updatedTagsJson);

        return userMapper.updateById(oldUser);  // 更新数据库中的用户信息
    }

    // 转换标签字符串为列表
    private List<String> convertJsonToTags(String tagsJson) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.readValue(tagsJson, new TypeReference<List<String>>() {
            });
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签格式错误");
        }
    }

    // 转换标签列表为JSON字符串
    private String convertTagsToJson(List<String> tags) {
        try {
            ObjectMapper objectMapper = new ObjectMapper();
            return objectMapper.writeValueAsString(tags);
        } catch (JsonProcessingException e) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签格式错误");
        }
    }


    /**
     * 根据标签搜索用户 SQL查询
     * <p>
     * 根据标签搜索用户 SQL查询
     */
    @Deprecated
    private List<User> searchUsersByTagsBySQL(List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        for (String tagName : tagNameList) {
            queryWrapper = queryWrapper.like("tags", tagName);
        }
        List<User> userList = userMapper.selectList(queryWrapper);
        return userList.stream().map(this::getSafetyUser).collect(Collectors.toList());

    }


}




