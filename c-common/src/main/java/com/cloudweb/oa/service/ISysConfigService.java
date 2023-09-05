package com.cloudweb.oa.service;

import com.cloudweb.oa.entity.SysConfig;
import com.baomidou.mybatisplus.extension.service.IService;

/**
 * <p>
 * 系统配置 服务类
 * </p>
 *
 * @author fgf
 * @since 2022-04-28
 */
public interface ISysConfigService extends IService<SysConfig> {

    SysConfig getSysConfig(String name);

    boolean update(SysConfig sysConfig);
}
