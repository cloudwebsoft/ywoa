package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.GroupOfRole;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-17
 */
public interface IGroupOfRoleService extends IService<GroupOfRole> {
    List<GroupOfRole> listByGroupCode(String groupCode);

    List<GroupOfRole> listByRoleCode(String roleCode);

    boolean setGroupOfRole(String groupCode, String[] roleCode);
}
