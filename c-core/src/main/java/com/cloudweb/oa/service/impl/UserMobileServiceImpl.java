package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.UserMobile;
import com.cloudweb.oa.mapper.UserMobileMapper;
import com.cloudweb.oa.service.IUserMobileService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.stereotype.Service;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-19
 */
@Service
public class UserMobileServiceImpl extends ServiceImpl<UserMobileMapper, UserMobile> implements IUserMobileService {

    @Override
    public boolean delByUserName(String userName) {
        QueryWrapper<UserMobile> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return remove(qw);
    }
}
