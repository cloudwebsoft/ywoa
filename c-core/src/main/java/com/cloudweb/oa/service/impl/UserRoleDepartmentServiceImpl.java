package com.cloudweb.oa.service.impl;

import cn.js.fan.util.StrUtil;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.RoleCache;
import com.cloudweb.oa.cache.UserAuthorityCache;
import com.cloudweb.oa.cache.UserCache;
import com.cloudweb.oa.entity.*;
import com.cloudweb.oa.mapper.UserRoleDepartmentMapper;
import com.cloudweb.oa.security.AuthUtil;
import com.cloudweb.oa.service.*;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.cloudweb.oa.utils.SpringUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * <p>
 * 角色关联的部门 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2022-03-13
 */
@Service
public class UserRoleDepartmentServiceImpl extends ServiceImpl<UserRoleDepartmentMapper, UserRoleDepartment> implements IUserRoleDepartmentService {

    @Autowired
    UserRoleDepartmentMapper userRoleDepartmentMapper;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    IDeptUserService deptUserService;

    @Autowired
    UserAuthorityCache userAuthorityCache;

    @Autowired
    RoleCache roleCache;

    @Autowired
    IDepartmentService departmentService;

    @Autowired
    UserCache userCache;

    @Autowired
    IRoleService roleService;

    @Autowired
    IUserOfRoleService userOfRoleService;

    @Autowired
    IUserAuthorityService userAuthorityService;

    @Override
    public boolean update(UserRoleDepartment userRoleDepartment, boolean isRefreshUserAuthority, boolean isIncludeOld) {
        boolean re = updateById(userRoleDepartment);
        if (re && isRefreshUserAuthority) {
            List<Department> listIncludeChildren = new ArrayList<>();
            // 取所有的子部门
            departmentService.getAllChild(listIncludeChildren, userRoleDepartment.getDeptCode());
            for (Department department : listIncludeChildren) {
                // 更新子部门下的人员权限
                List<DeptUser> list = deptUserService.listByDeptCode(department.getCode());
                for (DeptUser deptUser : list) {
                    if (userRoleDepartment.getInclude()) {
                        List listOfRole = userOfRoleService.listByUserName(deptUser.getUserName());
                        // 如果用户原来不属于该角色则创建
                        if (listOfRole.size() == 0) {
                            userOfRoleService.create(deptUser.getUserName(), userRoleDepartment.getRoleCode());
                        }
                    }
                    else {
                        // 如果由原来含子部门改为了不含子部门，则删除子部门中用户的角色
                        if (isIncludeOld && !userRoleDepartment.getInclude()) {
                            userOfRoleService.delOfUser(deptUser.getUserName());
                        }
                    }

                    // 刷新用户所拥有的权限
                    userAuthorityService.refreshUserAuthority(deptUser.getUserName());

                    userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                    userCache.refreshRoles(deptUser.getUserName());
                }
            }

            // 更新部门下的人员权限
            List<DeptUser> list = deptUserService.listByDeptCode(userRoleDepartment.getDeptCode());
            for (DeptUser deptUser : list) {
                // 刷新用户所拥有的权限
                userAuthorityService.refreshUserAuthority(deptUser.getUserName());

                userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                userCache.refreshRoles(deptUser.getUserName());
            }

            roleCache.refreshAll();
        }
        return re;
    }

    @Override
    public List<UserRoleDepartment> list(String op, String roleCode, String name) {
        String sql = "select r.* from department d, user_role_department r where r.role_code=" + StrUtil.sqlstr(roleCode) + " and r.dept_code=d.code";
        if ("search".equals(op)) {
            if (!"".equals(name)) {
                sql += " and d.name like " + StrUtil.sqlstr("%" + name + "%");
            }
        }
        sql += " order by orders desc";
        return userRoleDepartmentMapper.listBySql(sql);
    }

    /**
     * 找出部门所属的角色记录
     * @param deptCode
     * @return
     */
    @Override
    public List<UserRoleDepartment> listByDeptCode(String deptCode) {
        return userRoleDepartmentMapper.listByDeptCode(deptCode);
    }

    /**
     * 找出角色下的部门
     * @param roleCode
     * @return
     */
    @Override
    public List<UserRoleDepartment> listByRoleCode(String roleCode) {
        QueryWrapper qw = new QueryWrapper();
        qw.eq("role_code", roleCode);
        return list(qw);
    }

    /**
     * 找出所有含有子部门的记录
     * @return
     */
    @Override
    public List<UserRoleDepartment> listAllInclude() {
        return userRoleDepartmentMapper.listAllInclude();
    }

    @Override
    public boolean create(String roleCode, String[] deptCodes) {
        boolean re = false;
        for (String deptCode : deptCodes) {
            UserRoleDepartment userRoleDepartment = new UserRoleDepartment();
            userRoleDepartment.setRoleCode(roleCode);
            userRoleDepartment.setDeptCode(deptCode);
            userRoleDepartment.setCreateDate(LocalDateTime.now());
            userRoleDepartment.setCreator(authUtil.getUserName());
            userRoleDepartment.setInclude(false);
            re = userRoleDepartment.insert();

            if (userRoleDepartment.getInclude()) {
                // 取出其下的子部门
                List<Department> listIncludeChildren = new ArrayList<>();
                departmentService.getAllChild(listIncludeChildren, userRoleDepartment.getDeptCode());
                for (Department department : listIncludeChildren) {
                    // 更新部门下的人员权限
                    List<DeptUser> listDeptUser = deptUserService.listByDeptCode(department.getCode());
                    for (DeptUser deptUser : listDeptUser) {
                        // 置人员角色
                        List listOfRole = userOfRoleService.listByUserName(deptUser.getUserName());
                        if (listOfRole.size() == 0) {
                            // 如果用户不属于该角色，则为其创建角色
                            userOfRoleService.create(deptUser.getUserName(), roleCode);
                        }

                        // 刷新用户所拥有的权限
                        userAuthorityService.refreshUserAuthority(deptUser.getUserName());

                        userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                        userCache.refreshRoles(deptUser.getUserName());
                    }
                }
            }

            // 更新部门下的人员权限
            List<DeptUser> list = deptUserService.listByDeptCode(deptCode);
            for (DeptUser deptUser : list) {
                // 置人员角色
                List listOfRole = userOfRoleService.listByUserName(deptUser.getUserName());
                if (listOfRole.size() == 0) {
                    // 如果用户不属于该角色，则为其创建角色
                    userOfRoleService.create(deptUser.getUserName(), roleCode);
                }

                // 刷新用户所拥有的权限
                userAuthorityService.refreshUserAuthority(deptUser.getUserName());

                userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                userCache.refreshRoles(deptUser.getUserName());
            }
        }
        roleCache.refreshAll();
        return re;
    }

    @Override
    public boolean del(String[] ids) {
        boolean re = false;
        for (String strId : ids) {
            long id = StrUtil.toInt(strId, -1);

            UserRoleDepartment userRoleDepartment = getById(id);
            re = userRoleDepartmentMapper.deleteById(id)==1;

            if (userRoleDepartment.getInclude()) {
                // 取出其下的子部门
                List<Department> listIncludeChildren = new ArrayList<>();
                departmentService.getAllChild(listIncludeChildren, userRoleDepartment.getDeptCode());
                for (Department department : listIncludeChildren) {
                    // 更新部门下的人员权限
                    List<DeptUser> listDeptUser = deptUserService.listByDeptCode(department.getCode());
                    for (DeptUser deptUser : listDeptUser) {
                        // userOfRoleService.delOfUser(deptUser.getUserName());
                        userOfRoleService.del(deptUser.getUserName(), userRoleDepartment.getRoleCode());

                        // 刷新用户所拥有的权限
                        userAuthorityService.refreshUserAuthority(deptUser.getUserName());

                        userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                        userCache.refreshRoles(deptUser.getUserName());
                    }
                }
            }

            // 更新部门下的人员权限
            List<DeptUser> list = deptUserService.listByDeptCode(userRoleDepartment.getDeptCode());
            for (DeptUser deptUser : list) {
                // userOfRoleService.delOfUser(deptUser.getUserName());
                userOfRoleService.del(deptUser.getUserName(), userRoleDepartment.getRoleCode());

                // 刷新用户所拥有的权限
                userAuthorityService.refreshUserAuthority(deptUser.getUserName());

                userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                userCache.refreshRoles(deptUser.getUserName());
            }
        }
        roleCache.refreshAll();
        return re;
    }

    /**
     * 删除角色下的所有组织
     * @param roleCode
     * @return
     */
    @Override
    public boolean delByRoleCode(String roleCode) {
        // 取出组织下的所有人员
        // List<UserRoleDepartment> list = listByRoleCode(roleCode);

        QueryWrapper qw = new QueryWrapper();
        qw.eq("role_code", roleCode);
        boolean re = remove(qw);
        if (re) {
            /*for (UserRoleDepartment userRoleDepartment : list) {
                String deptCode = userRoleDepartment.getDeptCode();
                // 取出部门下的人员
                List<DeptUser> duList = deptUserService.listByDeptCode(deptCode);
                for (DeptUser deptUser : duList) {
                    userAuthorityCache.refreshUserAuthorities(deptUser.getUserName());
                }
            }*/
        }
        return re;
    }
}
