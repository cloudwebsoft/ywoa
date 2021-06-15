package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.OaPortal;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 服务类
 * </p>
 *
 * @author fgf
 * @since 2020-02-12
 */
public interface IOaPortalService extends IService<OaPortal> {

    void init(String userName);

    void delOfUser(String userName);
}
