package com.redmoon.forum.util;

import java.sql.SQLException;
import java.util.Vector;

import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.*;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.Leaf;
import com.redmoon.forum.MsgMgr;

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
public class VisitTopicLogDb extends QObjectDb {
    public VisitTopicLogDb() {
        super();
    }

    public Vector getMonthMaxVisited(int count) {
    	Vector v = new Vector();
    	
    	VisitTopicLogDb bvld = new VisitTopicLogDb();
    	ResultIterator ri = null;
    	MsgMgr mm = new MsgMgr();
    	
    	String sql = "select count(*) as cc, topic_id from " + bvld.getTable().getName() + " where boardcode<>" + StrUtil.sqlstr(Leaf.CODE_BLOG);
   		sql += " and add_date>=? group by topic_id order by cc desc";
    	
    	java.util.Date d = DateUtil.addDate(new java.util.Date(), -30);
    	JdbcTemplate jt = new JdbcTemplate();
    	try {
			ri = jt.executeQuery(sql, new Object[]{d}, 1, count);
			while (ri.hasNext()) {
				ResultRecord rr = (ResultRecord)ri.next();
				v.addElement(mm.getMsgDb(rr.getLong(2)));
			}
    	}
    	catch (SQLException e) {
    		LogUtil.getLog(getClass()).error(StrUtil.trace(e));
    	}
		return v;
    }
}
