package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.UserOfGroup;
import com.cloudweb.oa.mapper.UserOfGroupMapper;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.service.IUserOfGroupService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-03
 */
@Service
public class UserOfGroupServiceImpl extends ServiceImpl<UserOfGroupMapper, UserOfGroup> implements IUserOfGroupService {
    @Autowired
    UserOfGroupMapper userOfGroupMapper;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    UserCache userCache;

    @Override
    public List<UserOfGroup> listByUserName(String userName) {
        QueryWrapper<UserOfGroup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        return list(qw);
    }

    @Override
    public List<UserOfGroup> listByGroupCode(String groupCode) {
        QueryWrapper<UserOfGroup> qw = new QueryWrapper<>();
        qw.eq("group_code", groupCode);
        return list(qw);
    }

    @Override
    public boolean delOfUser(String userName) {
        QueryWrapper<UserOfGroup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        return remove(qw);
    }

    @Override
    public void removeAllGroupOfUser(String userName) {
        QueryWrapper<UserOfGroup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        userOfGroupMapper.delete(qw);

        userCache.refreshGroups(userName);
    }

    @Override
    public UserOfGroup getUserOfGroup(String userName, String groupCode) {
        QueryWrapper<UserOfGroup> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        qw.eq("group_code", groupCode);
        return getOne(qw, false);
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean create(String groupCode, String[] users) {
        boolean re = true;
        for (String userName : users) {
            // 如果用户已属于该用户组，则不添加
            if (getUserOfGroup(userName, groupCode)!=null) {
                continue;
            }

            UserOfGroup userOfGroup = new UserOfGroup();
            userOfGroup.setGroupCode(groupCode);
            userOfGroup.setUserName(userName);
            re = userOfGroup.insert();

            // 刷新用户所拥有的权限
            userAuthorityService.refreshUserAuthority(userName);
        }
        return re;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean del(String groupCode, String[] users) {
        boolean re = true;
        for (String userName : users) {
            QueryWrapper<UserOfGroup> qw = new QueryWrapper<>();
            qw.eq("user_name", userName);
            qw.eq("group_code", groupCode);
            re = remove(qw);

            // 刷新用户所拥有的权限
            userAuthorityService.refreshUserAuthority(userName);
        }
        return re;
    }
}
