package com.redmoon.oa.fileark;

import com.cloudwebsoft.framework.base.*;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.SQLException;
import cn.js.fan.db.PrimaryKey;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class DocPollOptionDb extends QObjectDb {
    public static final String OPTS = "opts_";

    public DocPollOptionDb() {
    }

    public DocPollOptionDb getDocPollOptionDb(int docId, int order) {
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setKeyValue("doc_id", new Integer(docId));
        pk.setKeyValue("orders", new Integer(order));
        DocPollOptionDb mpod = (DocPollOptionDb)getQObjectDb(pk.getKeys());
        return mpod;
    }

    public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
        boolean re = super.save(jt, params);
        long msgId = getLong("doc_id");
        // LogUtil.getLog(getClass()).info(getClass() + " save3 msgId=" + getLong("msg_id") + " orders=" + getInt("orders") + " primaryKey=" + primaryKey);
        try {
            RMCache.getInstance().remove(OPTS + msgId, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        return re;
    }

    public Vector getOptions(int docId) {
        String sql = "select doc_id,orders from " + table.getName() + " where doc_id=? order by orders asc";
        Vector v = null;
        try {
            v = (Vector)RMCache.getInstance().getFromGroup(OPTS + docId, cacheGroup);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getOptions:" + e.getMessage());
        }
        if (v==null) {
            v = list(new JdbcTemplate(), sql, new Object[] {new Integer(docId)});
            try {
                RMCache.getInstance().putInGroup(OPTS + docId, cacheGroup, v);
            }
            catch (Exception e) {
                LogUtil.getLog(getClass()).error("getOptions:" + e.getMessage());
            }
        }
        return v;
    }


}
