package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserAdminDept;
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
public interface IUserAdminDeptService extends IService<UserAdminDept> {
    List<UserAdminDept> listByUserName(String userName);

    boolean setUserAdminDept(String userName, String deptCodes);
}
