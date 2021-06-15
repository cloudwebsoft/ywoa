package com.cloudweb.oa.security;

import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.UserOfRole;
import com.cloudweb.oa.mapper.UserMapper;
import com.redmoon.oa.pvg.Privilege;
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

@Service("userDetailServiceImpl")
public class UserDetailServiceImpl implements UserDetailsService {

    @Autowired
    private UserMapper userMapper;

    @Autowired
    private UserAuthorityCache userAuthorityCache;

    @Autowired
    private UserCache userCache;

    @Override
    public UserDetails loadUserByUsername(String userName) throws UsernameNotFoundException {
/*        QueryWrapper<Users> queryWrapper = new QueryWrapper<>();
        queryWrapper.eq("name", userName);
        User users = userMapper.selectOne(queryWrapper);*/

        com.cloudweb.oa.entity.User user = userCache.getUser(userName);
        // com.cloudweb.oa.entity.User user = userMapper.getUserByNameWithRole(userName);
        if (user == null) {
            throw new UsernameNotFoundException("auth_fail"); // "用户不存在");
        }
        /*String pwd = passwordEncoder.encode(sysUser.getPassword());
        System.out.println(pwd);*/

        return new User(user.getName(), user.getPwd(), getRolesAndAuthorities(user.getName(), user.getUserRoleList()));
    }

    private Collection<GrantedAuthority> getRolesAndAuthorities(String userName, List<UserOfRole> roles) {
        if (Privilege.ADMIN.equals(userName)) {
            // 给admin加入ROLE_ADMIN角色
            return AuthorityUtils.commaSeparatedStringToAuthorityList("admin,ROLE_ADMIN");
        }

        List<GrantedAuthority> list = new ArrayList<>();

        // 加入ROLE_LOGIN，表示已登录
        SimpleGrantedAuthority grantedAuthority = new SimpleGrantedAuthority("ROLE_LOGIN");
        list.add(grantedAuthority);

        // 1. 放入角色时需要加前缀ROLE_，而在controller使用时不需要加ROLE_前缀
        // 2. 放入的是权限时，不能加ROLE_前缀，hasAuthority与放入的权限名称对应即可
        if (roles!=null) {
            for (UserOfRole userOfRole : roles) {
                grantedAuthority = new SimpleGrantedAuthority("ROLE_" + userOfRole.getRoleCode());
                list.add(grantedAuthority);
            }
        }

        List<String> listAuthority = userAuthorityCache.getUserAuthorities(userName);
        for (String authority : listAuthority) {
            grantedAuthority = new SimpleGrantedAuthority(authority);
            list.add(grantedAuthority);
        }

        return list;
    }
}