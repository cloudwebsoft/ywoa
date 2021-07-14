package com.redmoon.forum;

import com.cloudwebsoft.framework.base.*;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.SQLException;
import cn.js.fan.db.PrimaryKey;

/**
 * <p>Title: 投票贴中的投票选项</p>
 *
 * <p>Description:对投票贴中的选项进行保存、记录投票者和其投票项 </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class MsgPollOptionDb extends QObjectDb {
    public static final String OPTS = "opts_";

    public MsgPollOptionDb() {
    }

    public MsgPollOptionDb getMsgPollOptionDb(long msgId, int order) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("msg_id", new Long(msgId));
        pk.setKeyValue("orders", new Integer(order));
        MsgPollOptionDb mpod = (MsgPollOptionDb)getQObjectDb(pk.getKeys());
        return mpod;
    }

    public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
        // System.out.println(getClass() + " save2 msgId=" + getLong("msg_id") + " orders=" + getInt("orders") + " primaryKey=" + primaryKey);

        boolean re = super.save(jt, params);
        long msgId = getLong("msg_id");
        // System.out.println(getClass() + " save3 msgId=" + getLong("msg_id") + " orders=" + getInt("orders") + " primaryKey=" + primaryKey);
        try {
            RMCache.getInstance().remove(OPTS + msgId, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getOptionsOfMsg1:" + e.getMessage());
        }
        return re;
    }

    /**
     * 取得投票选项
     * @param msgId long 贴子ID
     * @return Vector
     */
    public Vector getOptions(long msgId) {
        String sql = "select msg_id,orders from " + table.getName() + " where msg_id=? order by orders asc";
        Vector v = null;
        try {
            v = (Vector)RMCache.getInstance().getFromGroup(OPTS + msgId, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getOptionsOfMsg1:" + e.getMessage());
        }
        if (v==null) {
            v = list(new JdbcTemplate(), sql, new Object[] {new Long(msgId)});
            try {
                RMCache.getInstance().putInGroup(OPTS + msgId, cacheGroup, v);
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("getOptionsOfMsg2:" + e.getMessage());
            }
        }
        return v;
    }


}
