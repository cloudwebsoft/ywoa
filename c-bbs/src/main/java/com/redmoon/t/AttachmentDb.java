package com.redmoon.t;

import java.io.File;
import java.util.Vector;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.web.Global;

import com.cloudwebsoft.framework.base.QObjectDb;

public class AttachmentDb extends QObjectDb {
	public boolean del() throws ResKeyException {
		super.del();
		String path = getString("path");
		Config cfg = Config.getInstance();
		String attPath = cfg.getProperty("t.attachmentPath");
		String p = Global.getRealPath() + attPath + "/" + path;
		File f = new File(p);
		return f.delete();
	}
	
	public Vector getAttachmentDbs(long msgId) {
		String sql = "select id from " + getTable().getName() + " where msg_id=?";
		return list(sql, new Object[]{new Long(msgId)});
	}
}
