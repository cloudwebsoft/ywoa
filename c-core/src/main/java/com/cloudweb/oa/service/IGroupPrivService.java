package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.GroupPriv;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-15
 */
public interface IGroupPrivService extends IService<GroupPriv> {
    GroupPriv getGroupPriv(String groupCode, String priv);

    boolean isGroupPrivValid(String groupCode, String priv);

    List<GroupPriv> listByPriv(String priv);

    boolean delGroupPriv(String groupCode, String priv);

    boolean setPrivs(String groupCode, String[] privs);

    boolean create(String groupCode, String priv);
}
