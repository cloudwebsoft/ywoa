package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserDesktopSetup;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-12
 */
public interface IUserDesktopSetupService extends IService<UserDesktopSetup> {

    boolean delDesktopOfPortal(long portalId);

    void initDesktopOfUser(long portalIdSys, long portalId, String userName);

    boolean delOfUser(String userName);
}
