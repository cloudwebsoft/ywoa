package com.cloudweb.oa.service.impl;

import cn.js.fan.util.RandomSecquenceCreator;
import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.RoleCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.mapper.RoleMapper;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.ConstUtil;
import com.redmoon.oa.Config;
import com.redmoon.oa.db.SequenceManager;
import com.redmoon.oa.kernel.License;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.sys.DebugUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.function.Function;
import java.util.function.Predicate;
import java.util.stream.Collectors;

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

    @Autowired
    IUserRolePostService userRolePostService;

    @Autowired
    IPostUserService postUserService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Autowired
    IUserRoleDepartmentService userRoleDepartmentService;

    @Autowired
    UserCache userCache;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    IGroupService groupService;

    @Autowired
    IGroupOfRoleService groupOfRoleService;

    @Autowired
    IPostService postService;

    @Override
    public List<Role> getAll() {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("status", 1);
        qw.orderByDesc("isSystem")
                .orderByAsc("unit_code")
                .orderByDesc("orders");
        return roleMapper.selectList(qw);
    }

    @Override
    public List<Role> getRolesOfUnit(String unitCode, boolean isWithSystem) {
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("unit_code", unitCode);
        qw.eq("status", 1);
        if (!isWithSystem) {
            qw.and(wrapper -> wrapper.ne("isSystem", isWithSystem));
        }
        qw.orderByDesc("isSystem")
                .orderByAsc("unit_code")
                .orderByDesc("orders");
        return roleMapper.selectList(qw);
    }

    /**
     * 更新角色
     * @param role
     * @param isRefreshUserAuthority 是否刷新角色下所有用户的权限，当启用/停用时
     * @return
     */
    @Override
    @Transactional(rollbackFor={Exception.class, RuntimeException.class})
    public boolean update(Role role, boolean isRefreshUserAuthority) {
        Role oldRole = getRole(role.getCode());
        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("code", role.getCode());
        boolean re = update(role, qw);
        if (re) {
            List<User> list = getAllUserOfRole(role.getCode(), true);
            for (User user : list) {
                if (user.getName().equals(ConstUtil.USER_SYSTEM) || user.getName().equals(ConstUtil.USER_ADMIN)) {
                    continue;
                }

                // 刷新用户的角色，因为如果更改了角色能否管理本部门，则需要刷新，否则流程中判断是否为部门管理员时就会有问题
                userCache.refreshRoles(user.getName());

                // 当启用/停用角色时，刷新用户权限
                if (isRefreshUserAuthority) {
                    userAuthorityService.refreshUserAuthority(user.getName());
                }

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

    /**
     * 取出启用的角色
     * @param searchUnitCode
     * @param op
     * @param what
     * @param kind
     * @return
     */
    @Override
    public List<Role> list(String searchUnitCode, String op, String what, String kind) {
        return list(searchUnitCode, op, what, kind, 1);
    }

    /**
     *
     * @param searchUnitCode
     * @param op
     * @param what
     * @param kind
     * @param status 为-1时表示取出全部的角色
     * @return
     */
    @Override
    public List<Role> list(String searchUnitCode, String op, String what, String kind, int status) {
        String sql;

        if (License.getInstance().isPlatformGroup()) {
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
        } else {
            if ("".equals(searchUnitCode)) {
                Privilege pvg = new Privilege();
                if (pvg.isUserPrivValid(pvg.getUser(), Privilege.ADMIN)) {
                    sql = "select * from user_role where 1=1";
                } else {
                    sql = "select * from user_role where 1=1";
                }
            } else {
                sql = "select * from user_role where 1=1";
            }
        }

        if ("search".equals(op)) {
            if (!"".equals(what)) {
                sql += " and description like " + StrUtil.sqlstr("%" + what + "%");
            }
            if (!"".equals(kind)) {
                sql += " and kind=" + StrUtil.sqlstr(kind);
            }
        }

        if (status != -1) {
            sql += " and status=" + status;
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
        role.setStatus(userRole.getStatus());
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
     * @param isWithDeptAndGroupAndPostUser boolean 是否包含部门、用户组及职位属该角色的用户
     * @return Vector
     */
    @Override
    public List<User> getAllUserOfRole(String roleCode, boolean isWithDeptAndGroupAndPostUser) {
        if (roleCode.equals(ConstUtil.ROLE_MEMBER)) {
            return userService.listAll();
        }

        List<User> list = new ArrayList<>();
        List<UserOfRole> urList = userOfRoleService.listByRoleCode(roleCode);
        for (UserOfRole userOfRole : urList) {
            User user = userCache.getUser(userOfRole.getUserName());
            if (user == null) {
                DebugUtil.i(getClass(), "getAllUserOfRole", "用户：" + userOfRole.getUserName() + " 不存在");
            }
            else {
                if (user.getIsValid() == 1) {
                    list.add(user);
                }
            }
        }

        if (isWithDeptAndGroupAndPostUser) {
            // 取得角色下部门中的用户
            List<UserRoleDepartment> urdList = userRoleDepartmentService.list("", roleCode, "");
            for (UserRoleDepartment userRoleDepartment : urdList) {
                // 如果包含子部门
                if (userRoleDepartment.getInclude()) {
                    // 取所有的子部门中的用户
                    List<Department> listIncludeChildren = new ArrayList<>();
                    departmentService.getAllChild(listIncludeChildren, userRoleDepartment.getDeptCode());
                    for (Department department : listIncludeChildren) {
                        List<DeptUser> duList = deptUserService.listByDeptCode(department.getCode());
                        for (DeptUser deptUser : duList) {
                            User user = userCache.getUser(deptUser.getUserName());
                            if (user.getIsValid() == 1) {
                                list.add(user);
                            }
                        }
                    }
                }
                // 取部门中的用户
                List<DeptUser> duList = deptUserService.listByDeptCode(userRoleDepartment.getDeptCode());
                for (DeptUser deptUser : duList) {
                    User user = userCache.getUser(deptUser.getUserName());
                    if (user.getIsValid() == 1) {
                        list.add(user);
                    }
                }
            }

            List<GroupOfRole> ugrList = userGroupOfRoleService.listByRoleCode(roleCode);
            for (GroupOfRole groupOfRole : ugrList) {
                // 取出用户组中的所有用户
                List<User> guList = groupService.getAllUserOfGroup(groupOfRole.getUserGroupCode());
                list.addAll(guList);
            }

            // 如果启用了职位，则取出角色下的所有职位中的用户
            Config cfg = Config.getInstance();
            if (cfg.getBoolean("isPostUsed")) {
                List<UserRolePost> rolePostList = userRolePostService.listRolePost("", roleCode, "");
                for (UserRolePost userRolePost : rolePostList) {
                    List<PostUser> postUserList = postUserService.listByPostId(userRolePost.getPostId());
                    for (PostUser postUser : postUserList) {
                        User user = userService.getUser(postUser.getUserName());
                        if (user!=null) {
							if (user.getIsValid() == 1) {
                            	list.add(user);
							}
                        }
						else {
							DebugUtil.w(getClass(), "getAllUserOfRole", "职位下的用户：" + postUser.getUserName() + "不存在");
						}
                    }
                }
            }
        }

        // 去重
        return list.stream().filter(distinctByKey(User::getName)).collect(Collectors.toList());
    }

    public static <T> Predicate<T> distinctByKey(Function<? super T, ?> keyExtractor) {
        Map<Object,Boolean> seen = new ConcurrentHashMap<>();
        return t -> seen.putIfAbsent(keyExtractor.apply(t), Boolean.TRUE) == null;
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
            if (userSetup == null) {
                DebugUtil.w(getClass(), "del", "角色" + role.getDescription() + " 中用户 " + user.getRealName() + "(" + user.getName() + ") 的setup信息已不存在");
            }
            else {
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
        }

        // 删除本角色中包含的用户
        userOfRoleService.delByRoleCode(role.getCode());

        // 删除角色所管理的部门
        roleDeptService.delByRoleCode(role.getCode());

        // 删除角色拥有的权限
        rolePrivService.delByRoleCode(role.getCode());

        // 删除角色下的职位关联记录
        userRolePostService.delByRoleCode(role.getCode());

        QueryWrapper<Role> qw = new QueryWrapper<>();
        qw.eq("code", role.getCode());
        boolean re = remove(qw);
        if (re) {
            roleCache.refreshDel(role.getCode());

            // 刷新角色下所有用户的权限
            for (User user : list) {
                userAuthorityService.refreshUserAuthority(user.getName());
                userCache.refreshRoles(user.getName());
            }
        }
        return re;
    }

    @Override
    public boolean create(String code, String desc, int isSystem, int orders, long diskQuota,
                          String unitCode, long msgSpaceQuota, int isDeptManager, String kind, Integer id) {
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
        if (id == null) {
            role.setId((int) SequenceManager.nextID(SequenceManager.OA_ROLE));
        }
        else {
            role.setId(id);
        }
        boolean re = role.insert();
        if (re) {
            roleCache.refreshCreate();
        }
        return re;
    }

    /**
     * 取出用户所属的角色，不含member
     * @param userName
     * @param isWithSystem
     * @return
     */
    @Override
    public List<Role> getAllRolesOfUser(String userName, boolean isWithSystem) {
        List<UserOfRole> list = userOfRoleService.listByUserName(userName);
        List<Role> roleList = new ArrayList<>();
        for (UserOfRole uor : list) {
            Role role = getRole(uor.getRoleCode());
            if (role == null || !role.getStatus()) {
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

        // 取用户所在的部门所属的角色
        List<DeptUser> deptUserList = deptUserService.listByUserName(userName);
        for (DeptUser deptUser : deptUserList) {
            List<UserRoleDepartment> urdList = userRoleDepartmentService.listByDeptCode(deptUser.getDeptCode());
            for (UserRoleDepartment userRoleDepartment : urdList) {
                Role role = getRole(userRoleDepartment.getRoleCode());
                if (role != null) {
                    if (role.getStatus()) {
                        roleList.add(role);
                    }
                }
                else {
                    DebugUtil.e(getClass(), "getAllRolesOfUser", "部门" + deptUser.getDeptCode() + " 所属的角色：" + userRoleDepartment.getRoleCode() + " 不存在");
                }
            }
        }

        // 取出角色中包含子部门的记录
        List<UserRoleDepartment> urdIncludeList = userRoleDepartmentService.listAllInclude();
        for (UserRoleDepartment userRoleDepartment : urdIncludeList) {
            if (deptUserService.isUserBelongToDept(userName, userRoleDepartment.getDeptCode())) {
                Role role = getRole(userRoleDepartment.getRoleCode());
                if (role.getStatus()) {
                    roleList.add(role);
                }
            }
        }

        // 取得用户所在的用户组所属的角色
        // 20220313 用户组与角色不再关联
        /*List<Group> groupList = groupService.getAllGroupsOfUser(userName);
        for (Group group : groupList) {
            // 取得用户组所属的角色
            List<GroupOfRole> gorList = groupOfRoleService.listByGroupCode(group.getCode());
            // 判断用户组所属的角色是否已在list中，如果不在则加入
            for (GroupOfRole groupOfRole : gorList) {
                Role role = getRole(groupOfRole.getRoleCode());
                if (role.getStatus()) {
                    roleList.add(role);
                }
            }
        }*/

        boolean isPostUsed = Config.getInstance().getBooleanProperty("isPostUsed");
        if (isPostUsed) {
            // 取得用户的职位
            List<PostUser> postUserList = postUserService.listByUserName(userName);
            for (PostUser postUser : postUserList) {
                Post post = postService.getById(postUser.getPostId());
                if (post == null) {
                    DebugUtil.i(getClass(), "getAllRolesOfUser", "Post id:" + postUser.getPostId() + " is not exist");
                    continue;
                }
                // 如果职位未启用则跳过
                if (!post.getStatus()) {
                    continue;
                }

                // 取得职位所属的角色
                List<UserRolePost> userRolePostList = userRolePostService.listByPostId(postUser.getId());
                for (UserRolePost userRolePost : userRolePostList) {
                    Role role = getRole(userRolePost.getRoleCode());
                    if (role.getStatus()) {
                        roleList.add(role);
                    }
                }
            }
        }

        return roleList.stream().filter(distinctByKey(Role::getCode)).collect(Collectors.toList());
    }
}
