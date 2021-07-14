package com.redmoon.forum.person;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import cn.js.fan.util.ResKeyException;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserPropDb extends QObjectDb {
    public UserPropDb() {
    }

    public boolean init(String userName) {
        boolean re = false;
        try {
            re = create(new JdbcTemplate(), new Object[] {
                userName, new Integer(0), new Integer(0), ""
            });
        }
        catch (ResKeyException e) {
            LogUtil.getLog(getClass()).error("init:" + e.getMessage());
        }
        return re;
    }

    public UserPropDb getUserPropDb(String userName) {
        UserPropDb up = (UserPropDb)getQObjectDb(userName);
        // 考虑到升级的需要，如此处理，并不在用户注册的时候，自动为其添加User Prop记录，而是当用到时自动创建
        if (up==null) {
            init(userName);
            return (UserPropDb)getQObjectDb(userName);
        }
        else
            return up;
    }
}
