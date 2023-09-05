package com.cloudweb.oa.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.cloudweb.oa.cache.SysConfigCache;
import com.cloudweb.oa.entity.SysConfig;
import com.cloudweb.oa.mapper.SysConfigMapper;
import com.cloudweb.oa.service.ISysConfigService;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

/**
 * <p>
 * 系统配置 服务实现类
 * </p>
 *
 * @author fgf
 * @since 2022-04-28
 */
@Service("sysConfigService")
public class SysConfigServiceImpl extends ServiceImpl<SysConfigMapper, SysConfig> implements ISysConfigService {

    @Autowired
    SysConfigCache sysConfigCache;

    @Override
    public SysConfig getSysConfig(String name) {
        QueryWrapper<SysConfig> qw = new QueryWrapper<>();
        qw.eq("name", name);
        return getOne(qw, false);
    }

    @Override
    public boolean update(SysConfig sysConfig) {
        QueryWrapper<SysConfig> qw = new QueryWrapper<>();
        qw.eq("name", sysConfig.getName());
        boolean re = update(sysConfig, qw);
        if (re) {
            sysConfigCache.refreshSave(sysConfig.getName());
        }
        return re;
    }
}
