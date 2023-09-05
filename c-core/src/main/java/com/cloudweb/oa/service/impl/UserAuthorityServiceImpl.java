package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.entity.UserAuthority;
import com.cloudweb.oa.mapper.UserAuthorityMapper;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.redmoon.oa.pvg.PrivDb;
import com.redmoon.oa.pvg.PrivMgr;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * <p>
 * 用户权限表 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-27
 */
@Service
public class UserAuthorityServiceImpl extends ServiceImpl<UserAuthorityMapper, UserAuthority> implements IUserAuthorityService {

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    UserAuthorityMapper userAuthorityMapper;

    @Override
    public void refreshUserAuthority(String userName) {
        PrivMgr privMgr = new PrivMgr();
        PrivDb[] privs = privMgr.getAllPriv();
        if (privs!=null) {
            // 清除用户的权限
            QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
            queryWrapper.eq("user_name", userName);
            userAuthorityMapper.delete(queryWrapper);

            Privilege pvg = new Privilege();
            UserAuthority userAuthority = new UserAuthority();
            for (PrivDb pd : privs) {
                // layer为1表示权限分组，为2表示权限
                if (pd.getLayer()==PrivDb.LAYER_PRV) {
                    if (pvg.isUserPrivValidByDb(userName, pd.getPriv())) {
                        userAuthority.setUserName(userName);
                        userAuthority.setAuthority(pd.getPriv());
                        userAuthorityMapper.insert(userAuthority);
                    }
                }
            }

            // 刷新缓存中赋予用户的权限
            userAuthorityCache.refreshUserAuthorities(userName);
        }
    }

    /**
     * 取得用户所拥有的权限
     * @param userName
     * @return
     */
    @Override
    public List<String> getUserAuthorities(String userName) {
        QueryWrapper<UserAuthority> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("user_name", userName);
        List<String> authorityList = new ArrayList();
        List<UserAuthority> list = userAuthorityMapper.selectList(queryWrapper);
        for (UserAuthority userAuthority : list) {
            authorityList.add(userAuthority.getAuthority());
        }
        return authorityList;
    }

    @Override
    public void delOfUser(String userName) {
        QueryWrapper<UserAuthority> qw = new QueryWrapper<>();
        qw.eq("user_name", userName);
        userAuthorityMapper.delete(qw);
    }

}
