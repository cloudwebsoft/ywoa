package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.RoleDept;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-22
 */
public interface IRoleDeptService extends IService<RoleDept> {
    List<RoleDept> listByRoleCode(String roleCode);

    boolean setRoleDepts(String roleCode, String roleDesc, String deptCodes, String deptNames);

    boolean delByRoleCode(String roleCode);
}
