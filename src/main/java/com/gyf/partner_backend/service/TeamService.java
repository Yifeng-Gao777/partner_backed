package com.gyf.partner_backend.service;

import com.gyf.partner_backend.model.domain.Team;
import com.baomidou.mybatisplus.extension.service.IService;
import com.gyf.partner_backend.model.domain.User;
import com.gyf.partner_backend.model.dto.TeamQuery;
import com.gyf.partner_backend.model.request.TeamJoinRequest;
import com.gyf.partner_backend.model.request.TeamQuitRequest;
import com.gyf.partner_backend.model.request.TeamUpdateRequest;
import com.gyf.partner_backend.model.vo.TeamUserVO;

import java.util.List;

/**
 * @author lihui
 * @description 针对表【team(队伍)】的数据库操作Service
 * @createDate 2024-11-05 15:05:06
 */
public interface TeamService extends IService<Team> {

    /**
     * 创建队伍
     *
     * @param team
     * @param loginUser
     * @return
     */
    long addTeam(Team team, User loginUser);

    /**
     * 查询队伍列表
     *
     * @param teamQuery
     * @param isAdmin
     * @return
     */
    List<TeamUserVO> listTeams(TeamQuery teamQuery, boolean isAdmin);

    /**
     * 更新队伍信息
     *
     * @param teamUpdateRequest
     * @return
     */

    boolean updateTeam(TeamUpdateRequest teamUpdateRequest,User loginUser);

    /**
     * 加入队伍
     *
     * @param teamJoinRequest
     * @param loginUser
     * @return
     */
    boolean joinTeam(TeamJoinRequest teamJoinRequest, User loginUser);

    /**
     * 退出队伍
     * @param teamQuitRequest
     * @param loginUser
     * @return
     */
    boolean quitTeam(TeamQuitRequest teamQuitRequest, User loginUser);

    /**
     * 解散队伍
     * @param id
     * @param loginUser
     * @return
     */
    boolean deleteTeam(long id ,User loginUser);

}
