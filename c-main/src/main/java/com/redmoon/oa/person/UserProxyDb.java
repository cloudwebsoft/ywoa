package com.redmoon.oa.person;

import com.cloudwebsoft.framework.base.QObjectDb;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class UserProxyDb extends QObjectDb {
    /**
     * 默认
     */
    public static final int TYPE_DEFAULT = 0;
    /**
     * 发起人所在部门
     */
    public static final int TYPE_DEPT = 1;

    public UserProxyDb() {
    }

    /**
     * 取得默认代理
     * @param userName String
     * @return UserProxyDb
     */
    public UserProxyDb getDefaultProxy(String userName) {
        String sql = "select id from user_proxy where user_name=? and proxy_type=?";
        UserProxyDb upd = new UserProxyDb();
        Iterator ir = upd.list(sql, new Object[]{userName, new Integer(TYPE_DEFAULT)}).iterator();
        if (ir.hasNext()) {
            return (UserProxyDb)ir.next();
        }
        return null;
    }
}
