package com.cloudweb.oa.cache;

import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.SysConfig;
import com.cloudweb.oa.service.ISysConfigService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SysConfigCache extends ObjCache {

    @Autowired
    ISysConfigService sysConfigService;

    private static final Lock lock = new ReentrantLock();

    public SysConfig getSysConfig(String name) {
        return (SysConfig)getObj(this, name);
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        SysConfig sysConfig = (SysConfig)obj;
        return sysConfig.getName();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        SysConfig sysConfig = new SysConfig();
        sysConfig.setName(value);
        return sysConfig;
    }

    @Override
    public Object getObjRaw(String key) {
        return sysConfigService.getSysConfig(key);
    }
}
