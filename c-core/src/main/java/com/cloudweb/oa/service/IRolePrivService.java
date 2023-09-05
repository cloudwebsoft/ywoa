package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.RolePriv;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
public interface IRolePrivService extends IService<RolePriv> {

    RolePriv getRolePriv(String roleCode, String priv);

    boolean isRolePrivValid(String roleCode, String priv);

    List<RolePriv> listByRoleCode(String roleCode);

    boolean setPrivs(String roleCode, String[] privs);

    boolean del(String roleCode, String priv);

    List<RolePriv> listByRolePriv(String priv);

    boolean delByRoleCode(String roleCode);

    void refreshRoleUserAuthority(String roleCode);

    boolean create(String roleCode, String priv);
}
