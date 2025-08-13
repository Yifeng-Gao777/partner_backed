package com.gyf.partner_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyf.partner_backend.common.BaseResponse;
import com.gyf.partner_backend.common.ErrorCode;
import com.gyf.partner_backend.common.ResultUtils;
import com.gyf.partner_backend.exception.BusinessException;
import com.gyf.partner_backend.model.domain.User;
import com.gyf.partner_backend.model.request.UpdateTagsRequest;
import com.gyf.partner_backend.model.request.UserLoginRequest;
import com.gyf.partner_backend.model.request.UserRegistRequest;
import com.gyf.partner_backend.service.UserService;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import static com.gyf.partner_backend.contant.UserContant.USER_LOGIN_STATE;

/**
 * 用户接口
 *
 * @author gyf
 */

@RestController
@RequestMapping("/user")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")//添加前端跨域地址
@Slf4j
public class UserController {

    @Resource
    private UserService userService;

    @Resource
    private RedisTemplate<String, Object> redisTemplate;

    @PostMapping("/register")
    public BaseResponse<Long> userRegister(@RequestBody UserRegistRequest userRegistRequest) {
        if (userRegistRequest == null) {
            return null;
        }
        String userAccount = userRegistRequest.getUserAccount();
        String userPassword = userRegistRequest.getUserPassword();
        String checkPassword = userRegistRequest.getCheckPassword();
//        String planetCode = userRegistRequest.getPlanetCode();
//        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword, planetCode)) {
        if (StringUtils.isAnyBlank(userAccount, userPassword, checkPassword)) {
            return null;
        }
//        long result = userService.userRegister(userAccount, userPassword, checkPassword, planetCode);
        long result = userService.userRegister(userAccount, userPassword, checkPassword);
        return ResultUtils.success(result);

    }

    @PostMapping("/login")
    public BaseResponse<User> userLogin(@RequestBody UserLoginRequest userLoginRequest, HttpServletRequest request) {
        if (userLoginRequest == null) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        String userAccount = userLoginRequest.getUserAccount();
        String userPassword = userLoginRequest.getUserPassword();
        if (StringUtils.isAnyBlank(userAccount, userPassword)) {
            return ResultUtils.error(ErrorCode.PARAMS_ERROR);
        }
        User user = userService.userLogin(userAccount, userPassword, request);
        return ResultUtils.success(user);
    }

    @PostMapping("/logout")
    public BaseResponse<Integer> userLogout(HttpServletRequest request) {
        if (request == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        int result = userService.userLogout(request);
        return ResultUtils.success(result);
    }

    @GetMapping("/current")
    public BaseResponse<User> getCurrentUser(HttpServletRequest request) {
        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
        User currentUser = (User) userObj;
        if (currentUser == null) {
            throw new BusinessException(ErrorCode.NOT_LOGIN);
        }
        long userId = currentUser.getId();
        // TODO 校验用户是否合法
        User user = userService.getById(userId);
        User safetyUser = userService.getSafetyUser(user);
        return ResultUtils.success(safetyUser);
    }

    /**
     * POST http://localhost:7070/user/login
     * Content-Type: application/json
     * <p>
     * {
     * "userAccount": "lihui11",
     * "userPassword": "12345678"
     * }
     **/
    @GetMapping("/search")
    public BaseResponse<List<User>> searchUsers(String username, HttpServletRequest request) {
        User loginUser = userService.getLoginUser(request);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }
//        if (!userService.isAdmin(request)) {
//            throw new BusinessException(ErrorCode.NO_AUTH, "缺少管理员权限");
//        }
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        if (StringUtils.isNotBlank(username)) {
            queryWrapper.like("username", username);
        }
        List<User> userList = userService.list(queryWrapper);
        List<User> list = userList.stream().map(user -> userService.getSafetyUser(user)).collect(Collectors.toList());
        return ResultUtils.success(list);
    }

//        //鉴权 仅管理员可见
//        Object userObj = request.getSession().getAttribute(USER_LOGIN_STATE);
//        User user = (User) userObj;
//        if (user == null || user.getRole() != ADMIN_ROLE) { //用常量替换
//            return new ArrayList<>();
//        }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteUser(@RequestBody long id, HttpServletRequest request) {
        if (!userService.isAdmin(request)) {
            throw new BusinessException(ErrorCode.NO_AUTH);
        }
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean b = userService.removeById(id);
        return ResultUtils.success(b);
    }


    @GetMapping("/search/tags")
    public BaseResponse<List<User>> searchUsersTags(@RequestParam(required = false) List<String> tagNameList) {
        if (CollectionUtils.isEmpty(tagNameList)) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        List<User> userList = userService.searchUsersByTags(tagNameList);
        return ResultUtils.success(userList);
    }

    // todo 推荐多个，未实现
    @GetMapping("/recommend")
    public BaseResponse<Page<User>> recommendUsers(long pageSize, long pageNum, HttpServletRequest request) {
        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 使用分页参数构建缓存key，避免不同页的数据冲突
        String reidsKey = String.format("yupao:user:recommed:%s:%d:%d", loginUser.getId(), pageNum, pageSize);
        ValueOperations<String, Object> valueOperations = redisTemplate.opsForValue();
        Page<User> userPage = (Page<User>) valueOperations.get(reidsKey);

        // 如果缓存命中，直接返回缓存的数据
        if (userPage != null) {
            return ResultUtils.success(userPage);
        }

        // 如果缓存未命中，查询数据库
        QueryWrapper<User> queryWrapper = new QueryWrapper<>();
        // 执行分页查询
        userPage = userService.page(new Page<>(pageNum, pageSize), queryWrapper);

        // 将查询结果存入缓存，设置合适的缓存过期时间（例如1分钟）
        try {
            valueOperations.set(reidsKey, userPage, 1, TimeUnit.MINUTES);
        } catch (Exception e) {
            log.error("redis set error", e);
        }

        // 返回查询结果
        return ResultUtils.success(userPage);
    }


    @PostMapping("/update")
    public BaseResponse<Integer> updateUser(@RequestBody User user, HttpServletRequest request) {
        //1.校验参数是否为空
        if (user == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        //2.校验权限
        User loginUser = userService.getLoginUser(request);

        int result = userService.updateUser(user, loginUser);
        return ResultUtils.success(result);

        //3.触发更像


    }

    @GetMapping("/match")
    public BaseResponse<List<User>> matchUsers(long num, HttpServletRequest request) {
        if (num <= 0 || num > 20) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        List<User> userList = userService.matchUsers(num, loginUser);
        return ResultUtils.success(userList);
    }

    /**
     * @param request
     * @param request
     * @return
     */
    // 更新标签
    @PostMapping("/updateTags")
    public BaseResponse<Integer> updateTags(@RequestBody UpdateTagsRequest request, HttpServletRequest httpRequest) {
        // 1. 校验参数是否为空
        if (request == null || request.getOperation() == null || request.getOldTag() == null || request.getNewTag() == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR, "标签列表、操作类型、旧标签和新标签不能为空");
        }

        // 2. 校验权限
        User loginUser = userService.getLoginUser(httpRequest);
        if (loginUser == null) {
            throw new BusinessException(ErrorCode.NO_AUTH, "用户未登录");
        }

        // 3. 调用服务层更新标签
        int result = userService.updateTags(request.getOldTag(), request.getNewTag(), request.getOperation(), loginUser);
        return ResultUtils.success(result);
    }




}
