package com.redmoon.t;

import java.util.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;

import com.cloudwebsoft.framework.base.*;
import com.cloudwebsoft.framework.db.*;
import java.sql.SQLException;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.SequenceMgr;

/**
 * <p>Title:标签 </p>
 *
 * <p>Description:标签的管理 </p>
 *
 * <p>Copyright: Copyright (c) 2006</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class TagDb extends QObjectDb {
    public static final String USER_NAME_SYSTEM = "sys";

    public TagDb() {
    }

    public boolean create(String tagName, String userName) {
        boolean re = false;
        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = jt.executeUpdate(table.getQueryCreate(),
                            new Object[] {new Long(SequenceMgr.
                                                   nextID(SequenceMgr.SQ_T_TAG)),
                            tagName, new java.util.Date(), new Integer(0), userName})==1;
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        // 删除对应的贴子标签
        TagMsgDb tmd = new TagMsgDb();
        tmd.delForTag(getLong("id"));
        return super.del(jt);
    }

    public Vector getTopTags(int count) {
        String sql = "select id from " + table.getName() + " order by orders desc, count desc";
        try {
			return listResult(sql, 1, count).getResult();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return new Vector();
    }

    public Vector getTagsOfUser(String userName) {
        TagMsgDb tmd = new TagMsgDb();
        String sql = "select distinct tag_id from " + tmd.getTable().getName() + " m, " + table.getName() + " t where m.tag_id=t.id and t.is_system=0 and m.user_name=" + StrUtil.sqlstr(userName);
        // System.out.println(getClass() + " sql=" + sql);
        return list(sql);
    }

    public TagDb getTagDb(long id) {
        return (TagDb)getQObjectDb(new Long(id));
    }

    public TagDb getTagDbByName(String name) {
        String sql = "select id from " + table.getName() + " where name=?";
        JdbcTemplate jt = new JdbcTemplate();
        try {
            ResultIterator ri = jt.executeQuery(sql, new Object[] {name});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord) ri.next();
                return getTagDb(rr.getLong(1));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("getTagDbByName:" + e.getMessage());
        }
        return null;
    }


}
