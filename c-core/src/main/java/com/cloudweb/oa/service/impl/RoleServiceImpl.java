package com.cloudweb.oa.service.impl;

import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.RoleCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.mapper.RoleMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.pvg.Privilege;
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
 * @since 2020-01-23
 */
@Service
public class RoleServiceImpl extends ServiceImpl<RoleMapper, Role> implements IRoleService {
    @Autowired
    RoleMapper roleMapper;

    @Autowired
    IRolePrivService rolePrivService;

    @Autowired
    IUserService userService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    IGroupOfRoleService userGroupOfRoleService;

    @Autowired
    IUserOfGroupService userOfGroupService;

    @Autowired
    IUserSetupService userSetupService;

    @Autowired
    IRoleDeptService roleDeptService;

    @Autowired
    RoleCache roleCache;

    @Override
    public List<Role> getAll() {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.orderByDesc("isSystem")
                .orderByAsc("unit_code")
                .orderByDesc("orders");
        return roleMapper.selectList(qw);
    }

    @Override
    public List<Role> getRolesOfUnit(String unitCode, boolean isWithSystem) {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("unit_code", unitCode);
        if (!isWithSystem) {
            qw.and(wrapper -> wrapper.ne("isSystem", isWithSystem));
        }
        qw.orderByDesc("isSystem")
                .orderByAsc("unit_code")
                .orderByDesc("orders");
        return roleMapper.selectList(qw);
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean update(Role role) {
        Role oldRole = getRole(role.getCode());
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("code", role.getCode());
        boolean re = update(role, qw);
        if (re) {
            List<User> list = getAllUserOfRole(role.getCode(), true);
            for (User user : list) {
                // 如果角色的新配额大于User的，则赋予User该配额
                if (role.getDiskQuota() > user.getDiskSpaceAllowed()) {
                    user.setDiskSpaceAllowed(role.getDiskQuota());
                    user.updateById();
                }
                // 如果角色的新配额小于UserDb，需比较角色原配额与UserSetupDb
                else if (role.getDiskQuota() < user.getDiskSpaceAllowed()) {
                    // 如果角色原配额等于UserDb，则相应调整UserSetupDb
                    if (oldRole.getDiskQuota() >= user.getDiskSpaceAllowed()) {
                        // 如果角色原配额大于User的，则将修改后用户拥有其所有角色中的最大配额赋予User
                        List<UserOfRole> urList = userOfRoleService.listByUserName(user.getName());
                        long q = 0;
                        for (UserOfRole userOfRole : urList) {
                            if (userOfRole.getRoleCode().equals(role.getCode())) {
                                continue;
                            }
                            Role roleTmp = getRole(userOfRole.getRoleCode());
                            if (roleTmp.getDiskQuota() > q) {
                                q = roleTmp.getDiskQuota();
                            }
                        }
                        user.setDiskSpaceAllowed(q);
                        user.updateById();
                    }
                }

                // 如果更新后角色的配额大于用户当前的配额，则重置用户配额
                UserSetup userSetup = userSetupService.getUserSetup(user.getName());
                if (role.getMsgSpaceQuota() > userSetup.getMsgSpaceAllowed()) {
                    userSetup.setMsgSpaceAllowed(role.getMsgSpaceQuota());
                    userSetupService.updateByUserName(userSetup);
                }
                // 如果更新后角色的新配额小于UserSetupDb中的
                else if (role.getMsgSpaceQuota() < userSetup.getMsgSpaceAllowed()) {
                    // 取得用户的其它所有角色，取最大的quota赋予给用户（如果原配额大于用户的msgSpaceAllowed的话）
                    if (oldRole.getMsgSpaceQuota() >= userSetup.getMsgSpaceAllowed()) {
                        List<UserOfRole> urList = userOfRoleService.listByUserName(user.getName());
                        long q = 0;
                        for (UserOfRole userOfRole : urList) {
                            if (userOfRole.getRoleCode().equals(role.getCode())) {
                                continue;
                            }
                            Role roleTmp = getRole(userOfRole.getRoleCode());
                            if (roleTmp.getMsgSpaceQuota() > q) {
                                q = roleTmp.getMsgSpaceQuota();
                            }
                        }
                        userSetup.setMsgSpaceAllowed(q);

                        userSetupService.updateByUserName(userSetup);
                    }
                }
            }

            RoleCache rc = new RoleCache();
            rc.refreshAdminDepts(role.getCode());
            rc.refreshSave(role.getCode());
        }
        return re;
    }

    @Override
    public Role getRole(String code) {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("code", code);
        return getOne(qw, false);
    }

    @Override
    public Role getRoleByDesc(String desc) {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("description", desc);
        return getOne(qw, false);
    }

    @Override
    public List<Role> list(String searchUnitCode, String op, String what, String kind) {
        String sql;
        if ("".equals(searchUnitCode)) {
            Privilege pvg = new Privilege();
            if (pvg.isUserPrivValid(pvg.getUser(), Privilege.ADMIN)) {
                sql = "select * from user_role where 1=1";
            } else {
                String unitCode = pvg.getUserUnitCode();
                sql = "select * from user_role where unit_code=" + StrUtil.sqlstr(unitCode);
            }
        } else {
            sql = "select * from user_role where unit_code=" + StrUtil.sqlstr(searchUnitCode);
        }

        if ("search".equals(op)) {
            if (!"".equals(what)) {
                sql += " and description like " + StrUtil.sqlstr("%" + what + "%");
            }
            if (!"".equals(kind)) {
                sql += " and kind=" + StrUtil.sqlstr(kind);
            }
        }

        sql += " order by isSystem desc, unit_code asc, orders desc, description asc";
        return roleMapper.listBySql(sql);
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean copy(Role userRole) {
        String newCode = RandomSecquenceCreator.getId(20);
        Role role = new Role();
        role.setCode(newCode);
        role.setDescription(userRole.getDescription() + "复制");
        role.setOrders(userRole.getOrders());
        role.setDiskQuota(userRole.getDiskQuota());
        role.setUnitCode(userRole.getUnitCode());
        role.setMsgSpaceQuota(userRole.getMsgSpaceQuota());
        role.setIsSystem(userRole.getIsSystem());
        role.setIsDeptManager(userRole.getIsDeptManager());
        boolean re = role.insert();
        if (re) {
            // 复制角色的权限
            List<RolePriv> list = rolePrivService.listByRoleCode(userRole.getCode());
            for (RolePriv userRolePriv : list) {
                RolePriv rolePriv = new RolePriv();
                rolePriv.setRoleCode(newCode);
                rolePriv.setPriv(userRolePriv.getPriv());
                rolePriv.insert();
            }
        }
        return re;
    }

    /**
     * 取得该角色的所有用户
     *
     * @param isWithGroupUser boolean 是否包含用户组属该角色的用户
     * @return Vector
     */
    @Override
    public List<User> getAllUserOfRole(String roleCode, boolean isWithGroupUser) {
        if (roleCode.equals(ConstUtil.ROLE_MEMBER)) {
            return userService.listAll();
        }

        List<User> list = new ArrayList<>();
        List<UserOfRole> urList = userOfRoleService.listByRoleCode(roleCode);
        for (UserOfRole userOfRole : urList) {
            User user = userService.getUser(userOfRole.getUserName());
            if (user.getIsValid() == 1) {
                list.add(user);
            }
        }

        // 如果需取得属于该角色的组中的用户
        if (isWithGroupUser) {
            List<GroupOfRole> ugrList = userGroupOfRoleService.listByRoleCode(roleCode);
            for (GroupOfRole groupOfRole : ugrList) {
                List<UserOfGroup> ugList = userOfGroupService.listByGroupCode(groupOfRole.getUserGroupCode());
                for (UserOfGroup userOfGroup : ugList) {
                    User user = userService.getUser(userOfGroup.getUserName());
                    list.add(user);
                }
            }
        }
        return list;
    }

    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean del(Role role) {
        // 重置角色中用户的磁盘及消息配额
        List<User> list = getAllUserOfRole(role.getCode(), true);
        for (User user : list) {
            if (role.getDiskQuota() >= user.getDiskSpaceAllowed()) {
                // 取得用户的其它所有角色，取最大的quota赋予给用户（如果大于用户的DiskSpaceAllowed的话）
                List<UserOfRole> urList = userOfRoleService.listByUserName(user.getName());
                long q = 0;
                for (UserOfRole userOfRole : urList) {
                    if (userOfRole.getRoleCode().equals(role.getCode())) {
                        continue;
                    }
                    Role roleTmp = getRole(userOfRole.getRoleCode());
                    if (roleTmp.getDiskQuota() > q) {
                        q = roleTmp.getDiskQuota();
                    }
                }
                user.setDiskSpaceAllowed(q);
                userService.updateByUserName(user);
            }

            UserSetup userSetup = userSetupService.getUserSetup(user.getName());
            if (role.getMsgSpaceQuota() >= userSetup.getMsgSpaceAllowed()) {
                // 取得用户的其它所有角色，取最大的quota赋予给用户（如果大于用户的msgSpaceAllowed的话）
                List<UserOfRole> urList = userOfRoleService.listByUserName(user.getName());
                long q = 0;
                for (UserOfRole userOfRole : urList) {
                    if (userOfRole.getRoleCode().equals(role.getCode())) {
                        continue;
                    }
                    Role roleTmp = getRole(userOfRole.getRoleCode());
                    if (roleTmp.getMsgSpaceQuota() > q) {
                        q = roleTmp.getMsgSpaceQuota();
                    }
                }
                userSetup.setMsgSpaceAllowed(q);

                userSetupService.updateByUserName(userSetup);
            }
        }

        // 删除本角色中包含的用户
        userOfRoleService.delByRoleCode(role.getCode());

        // 删除角色所管理的部门
        roleDeptService.delByRoleCode(role.getCode());

        // 删除角色拥有的权限
        rolePrivService.delByRoleCode(role.getCode());

        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("code", role.getCode());
        boolean re = remove(qw);
        if (re) {
            roleCache.refreshDel(role.getCode());
        }
        return re;
    }

    @Override
    public boolean create(String code, String desc, int isSystem, int orders, long diskQuota,
                          String unitCode, long msgSpaceQuota, int isDeptManager, String kind) {
        Role role = new Role();
        role.setCode(code);
        role.setDescription(desc);
        role.setIsSystem(isSystem == 1);
        role.setOrders(orders);
        role.setDiskQuota(diskQuota);
        role.setUnitCode(unitCode);
        role.setMsgSpaceQuota(msgSpaceQuota);
        role.setIsDeptManager(String.valueOf(isDeptManager));
        role.setKind(kind);
        boolean re = role.insert();
        if (re) {
            roleCache.refreshCreate();
        }
        return re;
    }

    @Override
    public List<Role> getRolesOfUser(String userName, boolean isWithSystem) {
        List<UserOfRole> list = userOfRoleService.listByUserName(userName);
        List<Role> roleList = new ArrayList<>();
        for (UserOfRole uor : list) {
            Role role = getRole(uor.getRoleCode());
            if (role == null) {
                continue;
            }
            if (role.getIsSystem()) {
                if (isWithSystem) {
                    roleList.add(role);
                }
            }
            else {
                roleList.add(role);
            }
        }
        return roleList;
    }
}
