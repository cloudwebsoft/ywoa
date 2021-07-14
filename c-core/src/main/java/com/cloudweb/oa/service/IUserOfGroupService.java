package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserOfGroup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-03
 */
public interface IUserOfGroupService extends IService<UserOfGroup> {

    List<UserOfGroup> listByUserName(String userName);

    void removeAllGroupOfUser(String userName);

    boolean delOfUser(String userName);

    List<UserOfGroup> listByGroupCode(String groupCode);

    UserOfGroup getUserOfGroup(String userName, String groupCode);

    boolean create(String groupCode, String[] users);

    boolean del(String groupCode, String[] users);
}
