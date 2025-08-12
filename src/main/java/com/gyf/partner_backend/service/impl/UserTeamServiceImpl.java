package com.gyf.partner_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gyf.partner_backend.model.domain.UserTeam;
import com.gyf.partner_backend.service.UserTeamService;
import com.gyf.partner_backend.mapper.UserTeamMapper;
import org.springframework.stereotype.Service;

/**
* @author lihui
* @description 针对表【user_team(用户队伍关系)】的数据库操作Service实现
* @createDate 2024-11-05 15:07:05
*/
@Service
public class UserTeamServiceImpl extends ServiceImpl<UserTeamMapper, UserTeam>
    implements UserTeamService{

}




