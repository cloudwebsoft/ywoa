package com.redmoon.oa.flow;

import java.sql.SQLException;
import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.db.ResultIterator;
import cn.js.fan.db.ResultRecord;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.db.SequenceManager;

public class PaperNoDb extends QObjectDb {
	
	public PaperNoDb getPaperNoDb(int id) {
		return (PaperNoDb)getQObjectDb(new Integer(id));
	}
	
    public PaperNoDb getPaperNoDb(String prefixName, int year) {
    	PaperNoPrefixDb pnpd = new PaperNoPrefixDb();
    	pnpd = pnpd.getPaperNoPrefixDbByName(prefixName);
    	
    	if (pnpd==null) {
    		return null;
    	}
    	
    	String sql = "select id from " + getTable().getName() + " where prefix_id=" + pnpd.getInt("id") + " and cur_year=" + year;
    	Vector v = list(sql);
    	if (v.size()>0) {
    		Iterator ir = v.iterator();
    		if (ir.hasNext()) {
    			return (PaperNoDb)ir.next();
    		}
    	}
    	else {
    		// 创建
            try {
				create(new JdbcTemplate(), new Object[]{new Integer(pnpd.getInt("id")), new Integer(0), new Integer(year)});
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(e);
				return null;
			}
            return this;
    	}

		return null;
    }	

	public void delOfPrefix(long prefixId) {
		String sql = "select id from " + getTable().getName() + " where prefix_id=?";
		Iterator ir = list(sql, new Object[]{new Long(prefixId)}).iterator();
		while (ir.hasNext()) {
			PaperNoDb pnd = (PaperNoDb)ir.next();
			try {
				pnd.del();
			} catch (ResKeyException e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
	}
}
