package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.entity.GroupOfRole;
import com.cloudweb.oa.entity.GroupPriv;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserOfGroup;
import com.cloudweb.oa.mapper.GroupPrivMapper;
import com.cloudweb.oa.service.IGroupPrivService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.service.IUserAuthorityService;
import com.cloudweb.oa.service.IUserOfGroupService;
import com.cloudweb.oa.service.IUserService;
import com.cloudweb.oa.utils.ConstUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * <p>
 *  服务实现类
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
@Service
public class GroupPrivServiceImpl extends ServiceImpl<GroupPrivMapper, GroupPriv> implements IGroupPrivService {

    @Autowired
    IUserOfGroupService userOfGroupService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    IUserService userService;

    @Override
    public GroupPriv getGroupPriv(String groupCode, String priv) {
        QueryWrapper<GroupPriv> qw = new QueryWrapper<>();
        qw.eq("groupCode", groupCode);
        qw.eq("priv", priv);
        return getOne(qw);
    }

    @Override
    public List<GroupPriv> listByPriv(String priv) {
        QueryWrapper<GroupPriv> qw = new QueryWrapper<>();
        qw.eq("priv", priv);
        return list(qw);
    }

    @Override
    public boolean isGroupPrivValid(String groupCode, String priv) {
        return getGroupPriv(groupCode, priv)!=null;
    }

    @Override
    public boolean delGroupPriv(String groupCode, String priv) {
        QueryWrapper<GroupPriv> qw = new QueryWrapper<>();
        qw.eq("groupCode", groupCode);
        qw.eq("priv", priv);
        boolean re = remove(qw);
        if (re) {
            refreshUserAuthority(groupCode);
        }
        return re;
    }

    @Override
    public boolean setPrivs(String groupCode, String[] privs) {
        QueryWrapper<GroupPriv> qw = new QueryWrapper<>();
        qw.eq("groupCode", groupCode);
        remove(qw);

        for (String priv : privs) {
            GroupPriv groupPriv = new GroupPriv();
            groupPriv.setPriv(priv);
            groupPriv.setGroupCode(groupCode);
            groupPriv.insert();
        }

        refreshUserAuthority(groupCode);
        return true;
    }

    public boolean create(String groupCode, String priv) {
        GroupPriv groupPriv = new GroupPriv();
        groupPriv.setPriv(priv);
        groupPriv.setGroupCode(groupCode);
        boolean re = groupPriv.insert();
        if (re) {
            refreshUserAuthority(groupCode);
        }
        return re;
    }

    public void refreshUserAuthority(String groupCode) {
        if (ConstUtil.GROUP_EVERYONE.equals(groupCode)) {
            List<User> list = userService.listAll();
            for (User user : list) {
                userAuthorityService.refreshUserAuthority(user.getName());
            }
        }
        else {
            // 刷新属于该用户组的用户的权限
            List<UserOfGroup> list = userOfGroupService.listByGroupCode(groupCode);
            for (UserOfGroup userOfRole : list) {
                userAuthorityService.refreshUserAuthority(userOfRole.getUserName());
            }
        }
    }
}
