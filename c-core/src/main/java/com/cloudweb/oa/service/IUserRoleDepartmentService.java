package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserRoleDepartment;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.entity.UserRolePost;

import java.util.List;

/**
 * <p>
 * 角色关联的部门 服务类
 * </p>
 *
 * @author fgf
 * @since 2022-03-13
 */
public interface IUserRoleDepartmentService extends IService<UserRoleDepartment> {

    List<UserRoleDepartment> list(String op, String roleCode, String name);

    boolean create(String roleCode, String[] deptCodes);

    boolean del(String[] ids);

    boolean update(UserRoleDepartment userRoleDepartment, boolean isFreshUserAuthority, boolean isIncludeOld);

    List<UserRoleDepartment> listByDeptCode(String deptCode);

    List<UserRoleDepartment> listAllInclude();

    boolean delByRoleCode(String roleCode);

    List<UserRoleDepartment> listByRoleCode(String roleCode);
}
