package com.cloudweb.oa.cache;

import cn.js.fan.db.ResultIterator;
import com.alibaba.fastjson.JSONObject;
import com.cloudweb.oa.base.ObjCache;
import com.cloudweb.oa.entity.SysConfig;
import com.cloudweb.oa.service.ISysConfigService;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.sql.SQLException;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

@Component
public class SQLCtlCache extends ObjCache {

    @Autowired
    ISysConfigService sysConfigService;

    private static final Lock lock = new ReentrantLock();

    public ResultIterator getResult(String sql) {
        return (ResultIterator)getObj(this, sql);
    }

    @Override
    public Lock getLock() {
        return lock;
    }

    @Override
    public String getPrimaryKey(Object obj) {
        ResultIterator ri = (ResultIterator)obj;
        return ri.getKey();
    }

    @Override
    public Object getEmptyObjWithPrimaryKey(String value) {
        ResultIterator ri = new ResultIterator();
        ri.setKey(value);
        return ri;
    }

    @Override
    public Object getObjRaw(String key) {
        JdbcTemplate jt = new JdbcTemplate();
        try {
            return jt.executeQuery(key);
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e);
        }
        return null;
    }
}
