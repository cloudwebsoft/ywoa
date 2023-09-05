package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.RolePriv;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserOfRole;
import com.cloudweb.oa.mapper.RolePrivMapper;
import com.cloudweb.oa.service.IRolePrivService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.service.IUserOfRoleService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 * 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
@Service
public class RolePrivServiceImpl extends ServiceImpl<RolePrivMapper, RolePriv> implements IRolePrivService {
    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    IUserService userService;

    @Override
    public RolePriv getRolePriv(String roleCode, String priv) {
        QueryWrapper<RolePriv> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        qw.eq("priv", priv);
        return getOne(qw);
    }

    @Override
    public boolean del(String roleCode, String priv) {
        QueryWrapper<RolePriv> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        qw.eq("priv", priv);
        boolean re = remove(qw);
        if (re) {
            refreshRoleUserAuthority(roleCode);
        }
        return re;
    }

    @Override
    public boolean isRolePrivValid(String roleCode, String priv) {
        return getRolePriv(roleCode, priv) != null;
    }

    @Override
    public List<RolePriv> listByRoleCode(String roleCode) {
        QueryWrapper<RolePriv> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        return list(qw);
    }

    @Override
    public boolean delByRoleCode(String roleCode) {
        QueryWrapper<RolePriv> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        return remove(qw);
    }

    @Override
    public List<RolePriv> listByRolePriv(String priv) {
        QueryWrapper<RolePriv> qw = new QueryWrapper<>();
        qw.eq("priv", priv);
        return list(qw);
    }

    @Override
    public boolean setPrivs(String roleCode, String[] privs) {
        QueryWrapper<RolePriv> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        remove(qw);

        if (privs!=null) {
            for (String priv : privs) {
                RolePriv rolePriv = new RolePriv();
                rolePriv.setPriv(priv);
                rolePriv.setRoleCode(roleCode);
                rolePriv.insert();
            }
        }

        refreshRoleUserAuthority(roleCode);
        return true;
    }

    @Override
    public boolean create(String roleCode, String priv) {
        RolePriv rolePriv = new RolePriv();
        rolePriv.setPriv(priv);
        rolePriv.setRoleCode(roleCode);
        boolean re = rolePriv.insert();
        if (re) {
            refreshRoleUserAuthority(roleCode);
        }
        return re;
    }

    @Override
    public void refreshRoleUserAuthority(String roleCode) {
        if (ConstUtil.ROLE_MEMBER.equals(roleCode)) {
            List<User> list = userService.listAll();
            for (User user : list) {
                userAuthorityService.refreshUserAuthority(user.getName());
            }
        }
        else {
            // 刷新属于该角色的用户的权限
            List<UserOfRole> list = userOfRoleService.listByRoleCode(roleCode);
            for (UserOfRole userOfRole : list) {
                userAuthorityService.refreshUserAuthority(userOfRole.getUserName());
            }
        }
    }
}
