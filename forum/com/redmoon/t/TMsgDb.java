package com.redmoon.t;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;
import cn.js.fan.db.*;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class TMsgDb extends QObjectDb {
	public TMsgDb getTMsgDb(long id) {
		return (TMsgDb)getQObjectDb(new Long(id));
	}
	
	public boolean del() throws ResKeyException {
		AttachmentDb att = new AttachmentDb();
		Iterator ir = att.getAttachmentDbs(getLong("id")).iterator();
		while (ir.hasNext()) {
			att = (AttachmentDb)ir.next();
			att.del();
		}
		boolean re = super.del();
		if (re) {
			Indexer indexer = new Indexer();
			indexer.delDocument(getLong("id"));
		}
		return re;
		
	}
	
	/**
	 * 取出微博发表的最后一条信息的ID
	 * @param tid
	 * @return
	 */
	public long getLastMsgIdOfT(long tid) {
		String sql = "select id from " + getTable().getName() + " where t_id=? order by create_date desc";
		Iterator ir;
		try {
			ir = listResult(sql, new Object[]{new Long(tid)}, 1, 1).getResult().iterator();
			if (ir.hasNext()) {
				TMsgDb tmd = (TMsgDb)ir.next();
				return tmd.getLong("id");
			}			
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;
	}
	
 
    /**
     * 用于Lunce中填充索引
     * @param beginDate long
     * @param endDate long
     * @return Vector
     */
    public Vector list(java.util.Date beginDate, java.util.Date endDate) {
        Vector v = new Vector();
        String sql = "";
        String strBDate = DateUtil.format(beginDate, "yyyy-MM-dd");
        String strEDate = DateUtil.format(endDate, "yyyy-MM-dd");
        if(beginDate != null && endDate != null){
            sql = "select id from " + getTable().getName() + " where create_date>=" + SQLFilter.getDateStr(strBDate, "yyyy-MM-dd") +
                  " and create_date<=" + SQLFilter.getDateStr(strEDate, "yyyy-MM-dd") ;
        } else if (beginDate == null && endDate != null) {
            sql = "select id from  " + getTable().getName() + " where create_date<=" + SQLFilter.getDateStr(strEDate, "yyyy-MM-dd");
        } else if (beginDate!=null && endDate==null) {
            sql = "select id from  " + getTable().getName() + " where create_date>=" + SQLFilter.getDateStr(strBDate, "yyyy-MM-dd");
        }
        else {
            sql = "select id from  " + getTable().getName();
        }
        JdbcTemplate jt = new JdbcTemplate();
        try {
        	ResultIterator ri = jt.executeQuery(sql);
        	while (ri.hasNext()) {
        		ResultRecord rr = (ResultRecord)ri.next();
        		v.addElement(getTMsgDb(rr.getLong(1)));        		
        	}
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("list(long beginDate, long endDate): " + e.getMessage());
        }
        return v;
    }	
}
