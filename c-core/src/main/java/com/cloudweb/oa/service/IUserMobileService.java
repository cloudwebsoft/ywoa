package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserMobile;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 *  服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-19
 */
public interface IUserMobileService extends IService<UserMobile> {
    boolean delByUserName(String userName);
}
