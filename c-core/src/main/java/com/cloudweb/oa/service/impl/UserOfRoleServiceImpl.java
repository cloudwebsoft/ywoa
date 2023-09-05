package com.cloudweb.oa.service.impl;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.web.Global;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.annotation.SysLog;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.Role;
import com.cloudweb.oa.entity.User;
import com.cloudweb.oa.entity.UserOfRole;
import com.cloudweb.oa.entity.UserSetup;
import com.cloudweb.oa.enums.LogLevel;
import com.cloudweb.oa.enums.LogType;
import com.cloudweb.oa.mapper.UserOfRoleMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.cloudweb.oa.utils.SpringUtil;
import com.github.pagehelper.PageHelper;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.IOException;
import java.util.ArrayList;
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
public class UserOfRoleServiceImpl extends ServiceImpl<UserOfRoleMapper, UserOfRole> implements IUserOfRoleService {
    @Autowired
    UserOfRoleMapper userOfRoleMapper;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    IRoleService roleService;

    @Autowired
    IUserService userService;

    @Autowired
    UserCache userCache;

    @Override
    public List<UserOfRole> listByUserName(String userName) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return userOfRoleMapper.selectList(qw);
    }

    @Override
    public List<UserOfRole> listByRoleCode(String roleCode) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode).orderByAsc("orders");
        return userOfRoleMapper.selectList(qw);
    }

    @Override
    public void removeAllRoleOfUser(String userName) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        userOfRoleMapper.delete(qw);

        userCache.refreshRoles(userName);
    }

    /**
     * 删除用户的所有角色
     *
     * @param userName
     * @return
     */
    @Override
    public boolean delOfUser(String userName) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        return userOfRoleMapper.delete(qw) == 1;
    }

    @Override
    public boolean del(String userName, String roleCode) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        qw.eq("roleCode", roleCode);
        return userOfRoleMapper.delete(qw) == 1;
    }

    @Override
    public UserOfRole getUserOfRole(String userName, String roleCode) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", userName);
        qw.eq("roleCode", roleCode);
        return getOne(qw, false);
    }

    @Override
    public boolean delByRoleCode(String roleCode) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("roleCode", roleCode);
        return remove(qw);
    }

    /**
     * 排序
     *
     * @param roleCode
     */
    @Override
    public void sortUser(String roleCode) {
        int k = 1;
        List<UserOfRole> list = listByRoleCode(roleCode);
        for (UserOfRole userOfRole : list) {
            userOfRole.setOrders(k);
            QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
            qw.eq("userName", userOfRole.getUserName());
            qw.eq("roleCode", roleCode);
            update(userOfRole, qw);
            k++;
        }
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean create(String roleCode, String[] users) {
        boolean re = true;
        Integer orders = userOfRoleMapper.getMaxOrders(roleCode);
        if (orders == null) {
            orders = 0;
        }
        for (String userName : users) {
            // 如果用户已属于该角色，则不添加
            if (getUserOfRole(userName, roleCode) != null) {
                continue;
            }

            orders++;

            UserOfRole userOfRole = new UserOfRole();
            userOfRole.setRoleCode(roleCode);
            userOfRole.setUserName(userName);
            userOfRole.setOrders(orders);
            re = userOfRole.insert();

            // 刷新用户所拥有的权限
            userAuthorityService.refreshUserAuthority(userName);

            userService.refreshOrders(userName);

            userCache.refreshRoles(userName);

            Role role = roleService.getRole(roleCode);
            User user = userService.getUser(userName);
            // 角色中添加用户时，如果UserSetupDb中的小，则赋予UserSetupDb角色的配额
            if (role.getDiskQuota() > user.getDiskSpaceAllowed()) {
                user.setDiskSpaceAllowed(role.getDiskQuota());
                user.updateById();
            }

            UserSetup userSetup = userSetupService.getUserSetup(userName);
            if (role.getMsgSpaceQuota() > userSetup.getMsgSpaceAllowed()) {
                userSetup.setMsgSpaceAllowed(role.getMsgSpaceQuota());
                userSetupService.updateByUserName(userSetup);
            }
        }
        return re;
    }

    @Override
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean delBatch(String roleCode, String[] users) {
        boolean re = true;
        for (String userName : users) {
            QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
            qw.eq("userName", userName);
            qw.eq("roleCode", roleCode);
            re = remove(qw);

            // 刷新用户所拥有的权限
            userAuthorityService.refreshUserAuthority(userName);

            userCache.refreshRoles(userName);

            // 更新排序号
            userService.refreshOrders(userName);

//            Role role = roleService.getRole(roleCode);
//            User user = userService.getOne(new QueryWrapper<User>().eq("realName", userName));
//            UserSetup userSetup = userSetupService.getUserSetup(userName);

            // 将修改后用户拥有角色中的最大配额赋予User
//            List<UserOfRole> urList = listByUserName(user.getName());
//            if (role.getDiskQuota().longValue() == user.getDiskSpaceAllowed().longValue()) {
//                long q = 0;
//                for (UserOfRole userOfRole : urList) {
//                    Role roleTmp = roleService.getRole(userOfRole.getRoleCode());
//                    if (roleTmp.getDiskQuota() > q) {
//                        q = roleTmp.getDiskQuota();
//                    }
//                }
//                user.setDiskSpaceAllowed(q);
//                user.updateById();
//            }

            // 将修改后用户拥有角色中的最大配额赋予UserSetup
//            if (role.getMsgSpaceQuota().longValue() == userSetup.getMsgSpaceAllowed().longValue()) {
//                long q = 0;
//                for (UserOfRole userOfRole : urList) {
//                    Role roleTmp = roleService.getRole(userOfRole.getRoleCode());
//                    if (roleTmp.getMsgSpaceQuota() > q) {
//                        q = roleTmp.getMsgSpaceQuota();
//                    }
//                }
//                userSetup.setMsgSpaceAllowed(q);
//                userSetupService.updateByUserName(userSetup);
//            }
        }
        return re;
    }

    @Override
    @SysLog(type = LogType.AUTHORIZE, action = "赋予角色给用户${userName}", remark = "${roleCodes}", debug = true, level = LogLevel.NORMAL)
    @Transactional(rollbackFor = {Exception.class, RuntimeException.class})
    public boolean setRoleOfUser(String userName, String[] roleCodes) {
        delOfUser(userName);

        boolean re = true;
        if (roleCodes != null) {
            List<UserOfRole> list = new ArrayList<>();
            int i = 0;
            for (String code : roleCodes) {
                if (code.equals(ConstUtil.ROLE_MEMBER)) {
                    continue;
                }
                UserOfRole userOfRole = new UserOfRole();
                userOfRole.setUserName(userName);
                userOfRole.setRoleCode(code);
                userOfRole.setOrders(i);
                list.add(userOfRole);
                i++;
            }
            re = saveBatch(list);
        }

        // 刷新用户的权限
        userAuthorityService.refreshUserAuthority(userName);

        // 刷新排序号
        userService.refreshOrders(userName);

        // 刷新缓存
        userCache.refreshRoles(userName);

        return re;
    }

    /**
     * 取得在排序的指定方向上的兄弟人员
     *
     * @param userOfRole
     * @param direction
     * @return
     */
    public UserOfRole getBrother(UserOfRole userOfRole, String direction) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.lambda().eq(UserOfRole::getRoleCode, userOfRole.getRoleCode());
        if ("down".equals(direction)) {
            qw.lambda().gt(UserOfRole::getOrders, userOfRole.getOrders());
            qw.orderByAsc("orders");
        } else {
            qw.lambda().lt(UserOfRole::getOrders, userOfRole.getOrders());
            qw.orderByDesc("orders");
        }

        PageHelper.startPage(1, 1); // 分页查询
        List<UserOfRole> list = list(qw);
        if (list.size() > 0) {
            return list.get(0);
        }

        return null;
    }

    @Override
    public boolean move(String roleCode, String userName, String direction) throws ErrMsgException {
        UserOfRole userOfRole = getUserOfRole(userName, roleCode);
        int orders = userOfRole.getOrders();

        // 根据direction检查是否可以移动
        if ("up".equals(direction)) {
            if (orders == 1) {
                throw new ErrMsgException("该项已处在首位！");
            }
        }

        if ("down".equals(direction)) {
            if (orders == userOfRoleMapper.getMaxOrders(roleCode)) {
                throw new ErrMsgException("该项已处于最后一位！");
            }
        }

        UserOfRole urBrother = getBrother(userOfRole, direction);
        if (urBrother == null) {
            throw new ErrMsgException("该项不能被移动！");
        }

        int borders = urBrother.getOrders();

        if ("up".equals(direction)) {
            if (userOfRole.getOrders() == 1) {
                return true;
            }
        } else {
            int maxorders = userOfRoleMapper.getMaxOrders(roleCode);
            if (orders == maxorders) {
                return true;
            }
        }

        boolean re = false;
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", urBrother.getUserName());
        qw.eq("roleCode", roleCode);
        urBrother.setOrders(orders);
        update(urBrother, qw);

        qw = new QueryWrapper<>();
        qw.eq("userName", userOfRole.getUserName());
        qw.eq("roleCode", roleCode);
        userOfRole.setOrders(borders);
        re = update(userOfRole, qw);

        return re;
    }

    @Override
    public boolean moveTo(UserOfRole userOfRole, String targetUser, int pos) {
        boolean re = false;
        UserOfRole ur = getUserOfRole(targetUser, userOfRole.getRoleCode());
        if (pos == 0) { // 之前
            userOfRoleMapper.updateOrders(userOfRole.getRoleCode(), ur.getOrders());

            userOfRole.setOrders(ur.getOrders());
            QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
            qw.eq("userName", userOfRole.getUserName());
            qw.eq("roleCode", userOfRole.getRoleCode());
            re = update(userOfRole, qw);
        } else {
            userOfRoleMapper.updateOrders(userOfRole.getRoleCode(), ur.getOrders() + 1);
            userOfRole.setOrders(ur.getOrders() + 1);
            QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
            qw.eq("userName", userOfRole.getUserName());
            qw.eq("roleCode", userOfRole.getRoleCode());
            re = update(userOfRole, qw);
        }

        // 生新整理顺序
        sortUser(userOfRole.getRoleCode());
        return re;
    }

    @Override
    public boolean update(UserOfRole userOfRole) {
        QueryWrapper<UserOfRole> qw = new QueryWrapper<>();
        qw.eq("userName", userOfRole.getUserName());
        qw.eq("roleCode", userOfRole.getRoleCode());
        return update(userOfRole, qw);
    }

    /**
     * 角色在部门中是否有效
     *
     * @param userName
     * @param roleCode
     * @param deptCode
     * @return
     */
    @Override
    public boolean isRoleOfDept(String userName, String roleCode, String deptCode) {
        UserOfRole userOfRole = getUserOfRole(userName, roleCode);
        if (userOfRole == null) {
            return false;
        }
        String depts = userOfRole.getDepts();
        if (StringUtils.isEmpty(depts)) {
            return true;
        }
        com.alibaba.fastjson.JSONObject deptsJson = com.alibaba.fastjson.JSONObject.parseObject(depts);
        if (deptsJson.containsKey(deptCode)) {
            return deptsJson.getIntValue(deptCode) != ConstUtil.ROLE_OF_DEPT_NO;
        } else {
            return true;
        }
    }

    @Override
    public int create(UserOfRole userOfRole) {
        return userOfRoleMapper.insert(userOfRole);
    }

    @Override
    public boolean create(String userName, String roleCode) {
        UserOfRole userOfRole = new UserOfRole();
        userOfRole.setRoleCode(roleCode);
        userOfRole.setUserName(userName);
        return userOfRoleMapper.insert(userOfRole) == 1;
    }
}