package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.OaSlideMenuGroup;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-12
 */
public interface IOaSlideMenuGroupService extends IService<OaSlideMenuGroup> {
    void init(String userName);

    boolean delOfUser(String userName);
}
