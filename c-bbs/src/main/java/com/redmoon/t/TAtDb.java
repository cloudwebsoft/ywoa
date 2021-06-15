package com.redmoon.t;

import java.sql.SQLException;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

public class TAtDb extends QObjectDb {

	private static final long serialVersionUID = 1L;

	public TAtDb getTAtDb(String userName) {
		TAtDb tat = (TAtDb)getQObjectDb(userName);
		if (tat==null) {
			init(userName);
			return (TAtDb)getQObjectDb(userName);
		}
		else
			return tat;
	}
	
	public boolean init(String userName) {
		boolean re = false;
		JdbcTemplate jt = new JdbcTemplate();
		try {
			re = jt.executeUpdate(getTable().getQueryCreate(), new Object[]{userName, ""})==1;
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return re;
	}
	
	public boolean doAt(String userName, long msgId) {
		boolean re = false;
		TAtDb tat = getTAtDb(userName);
		String ids = StrUtil.getNullStr(tat.getString("msg_ids"));
		// 最多记录500字
		if (ids.length()>=500) {
			ids = ids.substring(0, 500);
			int p = ids.lastIndexOf(",");
			if (p!=-1) {
				ids = ids.substring(0, p);
			}
		}
		if (ids.equals(""))
			ids = "" + msgId;
		else
			ids = msgId + "," + ids;
		tat.set("msg_ids", ids);
		try {
			re = tat.save();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return re;
	}
}
