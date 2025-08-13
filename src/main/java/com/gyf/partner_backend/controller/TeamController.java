package com.gyf.partner_backend.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.gyf.partner_backend.common.BaseResponse;
import com.gyf.partner_backend.common.DeleteRequest;
import com.gyf.partner_backend.common.ErrorCode;
import com.gyf.partner_backend.common.ResultUtils;
import com.gyf.partner_backend.exception.BusinessException;
import com.gyf.partner_backend.model.domain.Team;
import com.gyf.partner_backend.model.domain.User;
import com.gyf.partner_backend.model.domain.UserTeam;
import com.gyf.partner_backend.model.dto.TeamQuery;
import com.gyf.partner_backend.model.request.TeamAddRequest;
import com.gyf.partner_backend.model.request.TeamJoinRequest;
import com.gyf.partner_backend.model.request.TeamQuitRequest;
import com.gyf.partner_backend.model.request.TeamUpdateRequest;
import com.gyf.partner_backend.model.vo.TeamUserVO;
import com.gyf.partner_backend.service.TeamService;
import com.gyf.partner_backend.service.UserService;
import com.gyf.partner_backend.service.UserTeamService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.Resource;
import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 队伍接口
 *
 * @author gyf
 */

@RestController
@RequestMapping("/team")
@CrossOrigin(origins = {"http://localhost:5173"}, allowCredentials = "true")//添加前端跨域地址
@Slf4j
public class TeamController {

    @Resource
    private UserService userService;

    @Resource
    private TeamService teamService;

    @Resource
    private UserTeamService userTeamService;

    @PostMapping("/add")
    public BaseResponse<Long> addTeam(@RequestBody TeamAddRequest teamAddRequest, HttpServletRequest request) {
        if (teamAddRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        Team team = new Team();
        BeanUtils.copyProperties(teamAddRequest, team);

        long teamId = teamService.addTeam(team, loginUser);
        return ResultUtils.success(teamId);
    }


    @PostMapping("/update")
    public BaseResponse<Boolean> updateTeam(@RequestBody TeamUpdateRequest teamUpdateRequest, HttpServletRequest request) {
        if (teamUpdateRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.updateTeam(teamUpdateRequest, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "更新队伍失败");
        }
        return ResultUtils.success(true);

    }

    @GetMapping("/get")
    public BaseResponse<Team> getTeamById(@RequestParam long id) {
        if (id <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = teamService.getById(id);
        if (team == null) {
            throw new BusinessException(ErrorCode.NULL_ERROR, "队伍不存在");
        }
        return ResultUtils.success(team);
    }

    @GetMapping("/list")
    public BaseResponse<List<TeamUserVO>> listTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        boolean isAdmin = userService.isAdmin(request);
        // 1、查询队伍列表
        List<TeamUserVO> teamList = teamService.listTeams(teamQuery, isAdmin);
        final List<Long> teamIdList = teamList.stream().map(TeamUserVO::getId).collect(Collectors.toList());
        // 2、判断当前用户是否已加入队伍
        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            User loginUser = userService.getLoginUser(request);
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
        }
        // 3、查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);
        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        teamList.forEach(team -> team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size()));
        return ResultUtils.success(teamList);
    }

    //todo 查询分页
    @GetMapping("/list/page")
    public BaseResponse<Page<Team>> listTeamsByPage(TeamQuery teamQuery) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        Team team = new Team();
        BeanUtils.copyProperties(team, teamQuery);
        Page<Team> page = new Page<>(teamQuery.getPageNum(), teamQuery.getPageSize());
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>(team);
        Page<Team> resultPage = teamService.page(page, queryWrapper);
        return ResultUtils.success(resultPage);
    }

    @PostMapping("/join")
    public BaseResponse<Boolean> joinTeam(@RequestBody TeamJoinRequest teamJoinRequest, HttpServletRequest request) {
        if (teamJoinRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.joinTeam(teamJoinRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/quit")
    public BaseResponse<Boolean> quitTeam(@RequestBody TeamQuitRequest teamQuitRequest, HttpServletRequest request) {
        if (teamQuitRequest == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.quitTeam(teamQuitRequest, loginUser);
        return ResultUtils.success(result);
    }

    @PostMapping("/delete")
    public BaseResponse<Boolean> deleteTeam(@RequestBody DeleteRequest deleteRequest, HttpServletRequest request) {
        if (deleteRequest == null || deleteRequest.getId() <= 0) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }
        long id = deleteRequest.getId();
        User loginUser = userService.getLoginUser(request);
        boolean result = teamService.deleteTeam(id, loginUser);
        if (!result) {
            throw new BusinessException(ErrorCode.SYSTEM_ERROR, "删除队伍失败");
        }
        return ResultUtils.success(true);
    }

    /**
     * 获取当前用户创建的队伍
     *
     * @param teamQuery
     * @param request
     * @return
     * @Author: lihui
     */
// todo bug 1 显示问题 不显示本人已经过期创建的队伍
//    2 不显示本人加密的队伍
    @GetMapping("/list/my/create")
    public BaseResponse<List<TeamUserVO>> listMyCreateTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 设置查询条件，查询当前用户创建的团队
        teamQuery.setUserId(loginUser.getId());

        // 构建查询条件
        QueryWrapper<Team> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", teamQuery.getUserId()); // 根据 `userId` 字段进行查询

        // 查询所有队伍
        List<Team> teamList = teamService.list(queryWrapper);

        // 转换为 TeamUserVO 并加入必要字段
        List<TeamUserVO> teamUserVOList = teamList.stream()
                .map(team -> {
                    TeamUserVO teamUserVO = new TeamUserVO();
                    teamUserVO.setId(team.getId());
                    teamUserVO.setName(team.getName());
                    teamUserVO.setDescription(team.getDescription());
                    teamUserVO.setUserId(team.getUserId());
                    teamUserVO.setCreateTime(team.getCreateTime());
                    teamUserVO.setUpdateTime(team.getUpdateTime());
                    teamUserVO.setMaxNum(team.getMaxNum());
                    teamUserVO.setExpireTime(team.getExpireTime());
                    teamUserVO.setDescription(team.getDescription());
                    teamUserVO.setStatus(team.getStatus());
                    // 其他字段的映射
                    return teamUserVO;
                }).collect(Collectors.toList());

        // 获取当前用户已加入的队伍
        List<Long> teamIdList = teamUserVOList.stream().map(TeamUserVO::getId).collect(Collectors.toList());

        QueryWrapper<UserTeam> userTeamQueryWrapper = new QueryWrapper<>();
        try {
            userTeamQueryWrapper.eq("userId", loginUser.getId());
            userTeamQueryWrapper.in("teamId", teamIdList);
            List<UserTeam> userTeamList = userTeamService.list(userTeamQueryWrapper);
            // 已加入的队伍 id 集合
            Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
            teamUserVOList.forEach(team -> {
                boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
                team.setHasJoin(hasJoin);
            });
        } catch (Exception e) {
            // 处理异常情况，日志记录等
        }

        // 查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", teamIdList);
        List<UserTeam> userTeamList = userTeamService.list(userTeamJoinQueryWrapper);

        // 队伍 id => 加入这个队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamList.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));

        teamUserVOList.forEach(team ->
                team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size())
        );

        // 返回所有队伍信息
        return ResultUtils.success(teamUserVOList);
    }


    /**
     * 获取当前用户加入的队伍
     *
     * @Author: lihui
     */
    @GetMapping("/list/my/join")
    public BaseResponse<List<TeamUserVO>> listMyJoinTeams(TeamQuery teamQuery, HttpServletRequest request) {
        if (teamQuery == null) {
            throw new BusinessException(ErrorCode.PARAMS_ERROR);
        }

        // 获取当前登录用户
        User loginUser = userService.getLoginUser(request);

        // 先查询用户已加入的队伍
        QueryWrapper<UserTeam> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("userId", loginUser.getId());
        List<UserTeam> userTeamList = userTeamService.list(queryWrapper);

        // 提取不重复的队伍 ID
        Map<Long, List<UserTeam>> listMap = userTeamList.stream().collect(Collectors.groupingBy(UserTeam::getTeamId));
        ArrayList<Long> idList = new ArrayList<>(listMap.keySet());

        // 设置查询条件，只查询当前用户已加入的队伍
        teamQuery.setIdList(idList);

        // 构建查询条件
        QueryWrapper<Team> teamQueryWrapper = new QueryWrapper<>();
        if (teamQuery.getIdList() != null && !teamQuery.getIdList().isEmpty()) {
            teamQueryWrapper.in("id", teamQuery.getIdList());  // 根据队伍ID查询
        }

        // 如果有搜索条件，加入搜索条件
        if (teamQuery.getSearchText() != null && !teamQuery.getSearchText().isEmpty()) {
            teamQueryWrapper.like("name", teamQuery.getSearchText())  // 根据队伍名称进行模糊搜索
                    .or().like("description", teamQuery.getSearchText());  // 或者根据队伍描述进行模糊搜索
        }

        // 分页处理
        teamQueryWrapper.last("LIMIT " + ((teamQuery.getPageNum() - 1) * teamQuery.getPageSize()) + ", " + teamQuery.getPageSize());

        // 查询队伍列表
        List<Team> teamList = teamService.list(teamQueryWrapper);

        // 转换为 TeamUserVO 并加入必要字段
        List<TeamUserVO> teamUserVOList = teamList.stream()
                .map(team -> {
                    TeamUserVO teamUserVO = new TeamUserVO();
                    teamUserVO.setId(team.getId());
                    teamUserVO.setName(team.getName());
                    teamUserVO.setDescription(team.getDescription());
                    teamUserVO.setUserId(team.getUserId());
                    teamUserVO.setCreateTime(team.getCreateTime());
                    teamUserVO.setUpdateTime(team.getUpdateTime());
                    teamUserVO.setMaxNum(team.getMaxNum());
                    teamUserVO.setExpireTime(team.getExpireTime());
                    teamUserVO.setStatus(team.getStatus());
                    // 其他字段的映射
                    return teamUserVO;
                }).collect(Collectors.toList());

        // 获取当前用户已加入的队伍
        Set<Long> hasJoinTeamIdSet = userTeamList.stream().map(UserTeam::getTeamId).collect(Collectors.toSet());
        teamUserVOList.forEach(team -> {
            boolean hasJoin = hasJoinTeamIdSet.contains(team.getId());
            team.setHasJoin(hasJoin);  // 是否已经加入
        });

        // 查询已加入队伍的人数
        QueryWrapper<UserTeam> userTeamJoinQueryWrapper = new QueryWrapper<>();
        userTeamJoinQueryWrapper.in("teamId", idList);
        List<UserTeam> userTeamListForCount = userTeamService.list(userTeamJoinQueryWrapper);

        // 队伍 ID => 加入该队伍的用户列表
        Map<Long, List<UserTeam>> teamIdUserTeamList = userTeamListForCount.stream()
                .collect(Collectors.groupingBy(UserTeam::getTeamId));

        teamUserVOList.forEach(team ->
                team.setHasJoinNum(teamIdUserTeamList.getOrDefault(team.getId(), new ArrayList<>()).size())  // 设置已加入人数
        );

        // 返回所有队伍信息
        return ResultUtils.success(teamUserVOList);
    }

}
