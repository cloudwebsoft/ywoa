package com.redmoon.oa.idiofileark;

import cn.js.fan.base.ObjectCache;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import com.redmoon.oa.person.UserMgr;
import com.redmoon.oa.person.UserDb;

public class IdiofilearkCache extends ObjectCache {

    public IdiofilearkCache(IdiofilearkDb IdiofilearkDb) {
        super(IdiofilearkDb);
    }

}
