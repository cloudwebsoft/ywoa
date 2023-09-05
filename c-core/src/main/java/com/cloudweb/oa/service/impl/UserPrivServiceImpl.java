package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.UserPriv;
import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;
import com.cloudweb.oa.mapper.UserPrivMapper;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.service.IUserPrivService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-04
 */
@Service
public class UserPrivServiceImpl extends ServiceImpl<UserPrivMapper, UserPriv> implements IUserPrivService {
    @Autowired
    UserPrivMapper userPrivMapper;

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    UserCache userCache;

    @Override
    public UserPriv getUserPriv(String userName, String priv) {
        QueryWrapper<UserPriv> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        qw.eq("priv", priv);
        return userPrivMapper.selectOne(qw);
    }

    @Override
    public boolean isUserPrivValid(String userName, String priv) {
        return getUserPriv(userName, priv) != null;
    }

    @Override
    public boolean delUserPriv(String userName, String priv) {
        QueryWrapper<UserPriv> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        qw.eq("priv", priv);
        boolean re = remove(qw);
        if (re) {
            // 刷新用户所拥有的权限
            IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
            userAuthorityService.refreshUserAuthority(userName);

            userCache.refreshPrivs(userName);
        }
        return re;
    }

    @Override
    public List<UserPriv> listByPriv(String priv) {
        QueryWrapper<UserPriv> qw = new QueryWrapper<>();
        qw.eq("priv", priv);
        return list(qw);
    }

    @Override
    public List<UserPriv> listByUserName(String userName) {
        QueryWrapper<UserPriv> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return list(qw);
    }

    @Override
    public boolean delOfUser(String userName) {
        QueryWrapper<UserPriv> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return remove(qw);
    }

    @Override
    @SysLog(type = LogType.AUTHORIZE, action = "授权给用户${userName}", remark="${privs}", debug = false, level = LogLevel.NORMAL)
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean setPrivs(String userName, String[] privs) {
        delOfUser(userName);

        List<UserPriv> list = new ArrayList<>();
        if (privs!=null) {
            for (String priv : privs) {
                UserPriv userPriv = new UserPriv();
                userPriv.setUsername(userName);
                userPriv.setPriv(priv);
                list.add(userPriv);
            }
        }
        boolean re = saveBatch(list);

        // 刷新用户所拥有的权限
        IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
        userAuthorityService.refreshUserAuthority(userName);

        userCache.refreshPrivs(userName);

        return re;
    }

    @Override
    public boolean create(String userName, String priv) {
        UserPriv userPriv = new UserPriv();
        userPriv.setUsername(userName);
        userPriv.setPriv(priv);
        boolean re = userPriv.insert();
        if (re) {
            // 刷新用户所拥有的权限
            IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
            userAuthorityService.refreshUserAuthority(userName);

            userCache.refreshPrivs(userName);
        }
        return re;
    }

    @Override
    public void removeAllPrivOfUser(String userName) {
        QueryWrapper<UserPriv> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        userPrivMapper.delete(qw);

        // 刷新用户所拥有的权限
        IUserAuthorityService userAuthorityService = SpringUtil.getBean(IUserAuthorityService.class);
        userAuthorityService.refreshUserAuthority(userName);

        userCache.refreshPrivs(userName);
    }
}
