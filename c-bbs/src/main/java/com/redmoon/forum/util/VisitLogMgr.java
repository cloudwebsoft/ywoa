package com.redmoon.forum.util;

import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.forum.*;

/**
 * <p>Title: 记录贴子和版块访问</p>
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
public class VisitLogMgr {
    static Vector topicLog = new Vector();

    /**
     * 每隔MAX_COUNT次，记录到数据库中
     */
    static int MAX_COUNT = 5;

    public VisitLogMgr() {
        super();
    }

    public static boolean logBoardVisit(HttpServletRequest request, String boardcode, String userName) throws ResKeyException {
        Config cfg = Config.getInstance();
        if (!cfg.getBooleanProperty("forum.isLogBoardVisit")) {
            return false;
        }
        String ip = StrUtil.getIp(request);
        BoardVisitLogDb bvl = new BoardVisitLogDb();
        long id = SequenceMgr.nextID(SequenceMgr.SQ_BOARD_VISIT_LOG);
        return bvl.create(new JdbcTemplate(), new Object[] {new Long(id), userName, boardcode, new java.util.Date(), ip});
    }

    public static void logTopicVisit(HttpServletRequest request,
                                        MsgDb md, String userName) throws
            ResKeyException {
        Config cfg = Config.getInstance();
        if (!cfg.getBooleanProperty("forum.isLogTopicVisit")) {
            return;
        }
        String ip = StrUtil.getIp(request);
        Object[] obj = new Object[4];
        obj[0] = userName;
        obj[1] = new Long(md.getId());
        obj[2] = ip;
        obj[3] = new Integer(md.isBlog()?1:0);

        if (topicLog.size()>=MAX_COUNT) {
            VisitTopicLogDb bvl = new VisitTopicLogDb();
            IPStoreDb isd = new IPStoreDb();
            JdbcTemplate jt = new JdbcTemplate();
            Iterator ir = topicLog.iterator();
            try {
                while (ir.hasNext()) {
                    Object[] ary = (Object[])ir.next();
                    long id = SequenceMgr.nextID(SequenceMgr.CMS_DIR_VISIT_LOG);
                    String sql = "insert into " + bvl.getTable().getName() +
                                 " (id, user_name, topic_id, add_date, ip, ip_address,is_blog,boardcode) values (" +
                                 id +
                                 "," + StrUtil.sqlstr((String)ary[0]) + "," + ((Long)obj[1]).longValue() + "," +
                                 SQLFilter.
                                 getDateStr(DateUtil.format(new java.util.Date(),
                            "yyyy-MM-dd HH:mm:ss"),
                                            "yyyy-MM-dd HH:mm:ss") + "," +
                                 StrUtil.sqlstr((String)ary[2]) + "," + StrUtil.sqlstr(isd.getPosition((String)ary[2])) + "," + ((Integer)obj[3]).intValue() + "," + StrUtil.sqlstr(md.getboardcode()) + ")"; ;
                    jt.addBatch(sql);
                }
                jt.executeBatch();
            } catch (SQLException e) {
                LogUtil.getLog("com.redmoon.forum.util.VisitLogMgr:").error(StrUtil.trace(e));
            }
            finally {
                topicLog.removeAllElements();
                topicLog.add(obj);
            }
        }
        else {
            topicLog.add(obj);
        }
    }
}
