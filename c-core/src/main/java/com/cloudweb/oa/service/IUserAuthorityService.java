package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.UserAuthority;
import com.baomidou.mybatisplus.extension.service.IService;

import java.util.List;

/**
 * <p>
 * 用户权限表 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-01-27
 */
public interface IUserAuthorityService extends IService<UserAuthority> {

    void refreshUserAuthority(String userName);

    List<String> getUserAuthorities(String userName);

    void delOfUser(String userName);
}
