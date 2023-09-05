package com.redmoon.oa.android;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

import javax.servlet.http.HttpServletRequest;
import java.util.Date;

public class SystemUpMgr {
	public SystemUpMgr() {

	}
	public boolean create(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
		SystemUpDb wd = new SystemUpDb();
		String name = "";
		String client = "";
		String num = ""; 	
		name = ParamUtil.get(request, "version_name");
		client = ParamUtil.get(request,"client");
		num = ParamUtil.get(request, "version_num");
		Date now = new Date();
		return wd.create(new JdbcTemplate(), new Object[] {num, name,
			now,client});
	}

	public boolean save(HttpServletRequest request) throws ErrMsgException,
            ResKeyException {
		SystemUpDb wd = new SystemUpDb();
		String name = "";
		String client = "";
		String num = ""; 				
		name = ParamUtil.get(request, "version_name");
		num = ParamUtil.get(request, "version_num");
        int id =  ParamUtil.getInt(request, "id");
    	client = ParamUtil.get(request,"client");
		wd=wd.getSystemUpDb(id);
		wd.set("version_num", num);
		wd.set("version_name", name);
		wd.set("client", client);
		return wd.save();	
	}

	public boolean del(int id) throws ErrMsgException {
		SystemUpDb rd = new SystemUpDb();
		boolean re = false;
		rd = rd.getSystemUpDb(id);
		try {
			re = rd.del();
		} catch (ResKeyException e) {
			// TODO Auto-generated catch block
			throw new ErrMsgException(e.getMessage());
		}
		return re;
	}

	public void delBatch(HttpServletRequest request) throws ErrMsgException {
		String strids = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strids, ",");
		if (ids == null)
			return;
		int len = ids.length;
		for (int i = 0; i < len; i++) {
			try {
				del(StrUtil.toInt(ids[i]));
			} catch (Exception e) {
				LogUtil.getLog(getClass()).error(e);
			}
		}
	}
}
