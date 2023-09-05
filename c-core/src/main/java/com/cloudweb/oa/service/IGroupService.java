package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.Group;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.entity.User;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-02
 */
public interface IGroupService extends IService<Group> {
    Group getGroup(String code);

    List<Group> getAll();

    boolean del(String groupCode);

    boolean create(String code, String desc, Integer isDept, Integer isIncludeSubDept, String unitCode, String kind);

    boolean update(String code, String desc, String deptCode, Integer isDept, Integer isIncludeSubDept, String unitCode, String kind);

    List<Group> getGroupsOfUser(String userName, boolean isWithSystem);

    List<Group> listByIsDept(boolean isDept);

    List<Group> listByUnitCode(String unitCode);

    List<User> getAllUserOfGroup(String groupCode);

    List<Group> list(String searchUnitCode, String op, String what, String kind);

    List<Group> getAllGroupsOfUser(String userName);

}
