package com.cloudweb.oa.service;

import cn.js.fan.util.ErrMsgException;
import com.cloudweb.oa.entity.UserOfRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.io.IOException;
import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-23
 */
public interface IUserOfRoleService extends IService<UserOfRole> {

    List<UserOfRole> listByUserName(String userName);

    void removeAllRoleOfUser(String userName);

    boolean delOfUser(String userName);

    boolean setRoleOfUser(String userName, String[] roleCodes);

    List<UserOfRole> listByRoleCode(String roleCode);

    boolean create(String roleCode, String[] users);

    UserOfRole getUserOfRole(String userName, String roleCode);

    boolean delBatch(String roleCode, String[] users);

    void sortUser(String roleCode);

    boolean move(String roleCode, String userName, String direction) throws ErrMsgException;

    boolean moveTo(UserOfRole userOfRole, String targetUser, int pos);

    boolean delByRoleCode(String roleCode);

    boolean update(UserOfRole userOfRole);

    boolean isRoleOfDept(String userName, String roleCode, String deptCode);

    int create(UserOfRole userOfRole);

    boolean create(String userName, String roleCode);

    boolean del(String userName, String roleCode);
}
