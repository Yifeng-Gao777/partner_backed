package com.gyf.partner_backend.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.gyf.partner_backend.model.domain.Tag;
import com.gyf.partner_backend.service.TagService;
import com.gyf.partner_backend.mapper.TagMapper;
import org.springframework.stereotype.Service;

/**
* @author gyf
* @description 针对表【tag(标签)】的数据库操作Service实现
* @createDate 2024-10-08 20:24:19
*/
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag>
    implements TagService {

}




