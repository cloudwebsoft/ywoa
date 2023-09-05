package com.cloudweb.oa.security;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Account;
import com.cloudweb.oa.entity.UserOfRole;
import com.cloudweb.oa.mapper.UserMapper;
import com.cloudweb.oa.service.IAccountService;
import com.cloudweb.oa.service.IUserService;
import com.redmoon.oa.Config;
import com.redmoon.oa.account.AccountDb;
import com.redmoon.oa.person.UserDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.AuthorityUtils;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * 暂无用
 */
@Service("userDetailServiceImpl")
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserCache userCache;

    @Autowired
    IAccountService accountService;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
        com.cloudweb.oa.entity.User user = userCache.getUser(userName);
        // DebugUtil.i(getClass(), "loadUserByUsername userName: ", userName);
        if (user == null) {
            com.redmoon.oa.Config cfg = Config.getInstance();
            // 检查是否使用了工号登录
            boolean isUseAccount = cfg.getBooleanProperty("isUseAccount");
            if (isUseAccount) {
                Account account = accountService.getAccount(userName);
                if (account != null) {
                    user = userCache.getUser(account.getUserName());
                    if (user == null) {
                        throw new UsernameNotFoundException("auth_fail");
                    }
                }
                else {
                    throw new UsernameNotFoundException("auth_fail");
                }
            }
            else {
                throw new UsernameNotFoundException("auth_fail");
            }
        }

        return new User(user.getName(), user.getPwd(), Privilege.getRolesAndAuthorities(user.getName(), user.getUserRoleList()));
    }
}