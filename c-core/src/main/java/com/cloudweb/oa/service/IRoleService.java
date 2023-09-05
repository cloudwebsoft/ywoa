package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Role;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.entity.User;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
public interface IRoleService extends IService<Role> {
    List<Role> getAll();

    Role getRole(String code);

    List<Role> getRolesOfUnit(String unitCode, boolean isWithSystem);

    List<Role> list(String searchUnitCode, String op, String what, String kind);

    List<Role> list(String searchUnitCode, String op, String what, String kind, int status);

    boolean update(Role role, boolean isRefreshUserAuthority);

    boolean copy(Role role);

    boolean create(String code, String desc, int isSystem, int orders, long diskQuota,
                   String unitCode, long msgSpaceQuota, int isDeptManager, String kind, Integer id);

    boolean del(Role role);

    List<Role> getAllRolesOfUser(String userName, boolean isWithSystem);

    List<User> getAllUserOfRole(String roleCode, boolean isWithDeptAndGroupAndPostUser);

    Role getRoleByDesc(String desc);

}
