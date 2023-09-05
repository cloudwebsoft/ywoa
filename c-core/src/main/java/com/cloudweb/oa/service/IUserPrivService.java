package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserPriv;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-04
 */
public interface IUserPrivService extends IService<UserPriv> {

    boolean delOfUser(String userName);

    void removeAllPrivOfUser(String userName);

    boolean setPrivs(String userName, String[] privs);

    UserPriv getUserPriv(String userName, String priv);

    List<UserPriv> listByPriv(String priv);

    boolean delUserPriv(String userName, String priv);

    boolean isUserPrivValid(String userName, String priv);

    List<UserPriv> listByUserName(String userName);

    boolean create(String userName, String priv);
}
