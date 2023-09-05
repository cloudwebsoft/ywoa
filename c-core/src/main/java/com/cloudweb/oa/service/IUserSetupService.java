package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserSetup;
import com.baomidou.mybatisplus.extension.service.IService;
import com.cloudweb.oa.entity.User;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-10
 */
public interface IUserSetupService extends IService<UserSetup> {
    boolean create(String userName);

    UserSetup getUserSetup(String userName);

    void onUserCreated(User user, String newDeptCode, String operator);

    void onUserUpdated(User user, String newDeptCode, String operator);

    void onUserDeleted(User user, String operator);

    boolean updateByUserName(UserSetup userSetup);

    String getPortrait(User user);

    List<String> getMySubordinates(String userName);

    boolean setMyleaders(String userName, String leaders);

    boolean del(String userName);

    String getWallpaperPath(String userName);

    boolean clearToken(String token);

    String getPortraitForFront(User user);
}
