package com.redmoon.forum.miniplugin.index;

import java.sql.*;
import java.util.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.forum.*;
import org.apache.log4j.*;

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
public class NewEliteTop {
    final String NEW_MSG = "cwbbs_new_msg";
    final String ELITE_MSG = "cwbbs_elite_msg";
    final String TOP_MSG = "cwbbs_top_msg";
    final String group = "cwbbs_index_group";

    Logger logger = Logger.getLogger(NewEliteTop.class.getName());

    public static long lastRefreshTime = System.currentTimeMillis();

    public void refresh() {
        try {
            RMCache.getInstance().invalidateGroup(group);
        } catch (Exception e) {
            logger.error(e.getMessage());
        }
    }

    public Vector listNewMsg(int n) {
        if (System.currentTimeMillis() - lastRefreshTime > 600000) {
            refresh();
            lastRefreshTime = System.currentTimeMillis(); // 每隔10分钟刷新一次
        }

        Vector v = null;
        try {
            v = (Vector)RMCache.getInstance().getFromGroup(NEW_MSG, group);
        }
        catch (Exception e) {
            logger.error("listNewMsg1:" + e.getMessage());
        }
        if (v==null) {
        	MsgDb md = new MsgDb();
            v = md.getNewMsgs(n);
        }
        return v;
    }

    public Vector listEliteMsg(int n) {
        Vector v = null;
         try {
             v = (Vector)RMCache.getInstance().getFromGroup(ELITE_MSG, group);
         }
         catch (Exception e) {
             logger.error("listEliteMsg1:" + e.getMessage());
         }
         if (v==null) {
             v = new Vector();
             try {
                 JdbcTemplate jt = new JdbcTemplate(new DataSource());
                 String sql = "select id from sq_thread where iselite=1 order by lydate desc";
                 ResultIterator ri = jt.executeQuery(sql, 1, n);
                 if (ri.getTotal()>0) {
                     MsgMgr mm = new MsgMgr();
                     while (ri.hasNext()) {
                         ResultRecord rr = (ResultRecord) ri.next();
                         v.addElement(mm.getMsgDb((int) rr.getLong(1)));
                     }
                     try {
                         RMCache.getInstance().putInGroup(ELITE_MSG, group, v);
                     } catch (Exception e) {
                         logger.error("listEliteMsg2:" + e.getMessage());
                     }
                 }
             } catch (SQLException e) {
                 logger.error("listEliteMsg:" + e.getMessage());
             }
         }
        return v;
    }

    public Vector listTopMsg(int n) {
        Vector v = null;
        try {
            v = (Vector) RMCache.getInstance().getFromGroup(TOP_MSG, group);
        } catch (Exception e) {
            logger.error("listTopMsg1:" + e.getMessage());
        }
        if (v == null) {
            v = new Vector();
            try {
                JdbcTemplate jt = new JdbcTemplate(new DataSource());
                String sql = "select id from sq_thread where msg_level>=" + MsgDb.LEVEL_TOP_BOARD + " order by msg_level desc, lydate desc";
                ResultIterator ri = jt.executeQuery(sql, 1, n);
                if (ri.getTotal()>0) {
                    MsgMgr mm = new MsgMgr();
                    while (ri.hasNext()) {
                        ResultRecord rr = (ResultRecord) ri.next();
                        v.addElement(mm.getMsgDb(rr.getInt(1)));
                    }
                    try {
                        RMCache.getInstance().putInGroup(TOP_MSG, group, v);
                    } catch (Exception e) {
                        logger.error("listTopMsg2:" + e.getMessage());
                    }
                }
            } catch (SQLException e) {
                logger.error("listTopMsg3:" + e.getMessage());
            }
        }
        return v;
    }

}
