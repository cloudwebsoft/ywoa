package com.redmoon.forum;

import cn.js.fan.base.*;
import java.util.Vector;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.cache.jcs.RMCache;
import com.cloudwebsoft.framework.util.LogUtil;
import java.sql.SQLException;

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
public class ThreadTypeCache extends ObjectCache {
    public static final String prefix = "ThreadType_board_";

    public ThreadTypeCache(ThreadTypeDb threadTypeDb) {
        super(threadTypeDb);
    }

    public void refreshThreadTypesOfBoard(String boardCode) {
        try {
            RMCache.getInstance().remove(prefix + boardCode, group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("refreshThreadTypesOfBoard:" + e.getMessage());
        }
    }

    public Vector getThreadTypesOfBoard(String boardCode) {
        Vector v = new Vector();
        int[] ids = null;
        try {
            ids = (int[]) RMCache.getInstance().getFromGroup(prefix + boardCode,
                    group);
        }
        catch (Exception e) {
            LogUtil.getLog(getClass()).error("getThreadTypesOfBoard1:" + e.getMessage());
        }
        if (ids==null) {
            String sql = "select id from sq_thread_type where board_code=? order by display_order";
            JdbcTemplate jt = new JdbcTemplate();
            try {
                ResultIterator ri = jt.executeQuery(sql,
                        new Object[] {boardCode});
                if (ri.size()>0) {
                    ids = new int[ri.size()];
                    int i = 0;
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        int id = rr.getInt(1);
                        ids[i] = id;
                        i++;
                    }
                }
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error("getThreadTypesOfBoard2:" + e.getMessage());
            }
            if (ids!=null) {
                try {
                    RMCache.getInstance().putInGroup(prefix + boardCode, group,
                            ids);
                }
                catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getThreadTypesOfBoard3:" + e.getMessage());
                }
            }
        }
        if (ids!=null) {
            int len = ids.length;
            ThreadTypeMgr ttm = new ThreadTypeMgr();
            for (int i=0; i<len; i++) {
                v.addElement(ttm.getThreadTypeDb(ids[i]));
            }
        }
        return v;
    }

}
