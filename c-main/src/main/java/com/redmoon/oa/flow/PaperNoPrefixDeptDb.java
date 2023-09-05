package com.redmoon.oa.flow;

import java.util.Iterator;
import java.util.Vector;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.oa.dept.DeptDb;
import com.redmoon.oa.dept.DeptUserDb;

public class PaperNoPrefixDeptDb extends QObjectDb {

	// public static final int IS_DELETED = 0;
	// public static final int IS_NOT_DELETED = 1;
	public PaperNoPrefixDeptDb() {
		super();
	}

	public PaperNoPrefixDeptDb getPaperNoPrefixDeptDb(int id) {
		return (PaperNoPrefixDeptDb) getQObjectDb(new Integer(id));
	}

	public Vector getDepts(long prefixId) {
		Vector v = new Vector();
		String sql = "select id from " + getTable().getName()
				+ " where prefix_id=" + prefixId;
		DeptDb dd = new DeptDb();
		Iterator ir = list(sql).iterator();
		while (ir.hasNext()) {
			PaperNoPrefixDeptDb pndd = (PaperNoPrefixDeptDb) ir.next();
			v.addElement(dd.getDeptDb(pndd.getString("dept_code")));
		}
		return v;
	}

	public void delOfPrefix(long prefixId) {
		String sql = "select id from " + getTable().getName()
				+ " where prefix_id=" + prefixId;
		Iterator ir = list(sql).iterator();
		try {
			while (ir.hasNext()) {
				PaperNoPrefixDeptDb pndd = (PaperNoPrefixDeptDb) ir.next();
				pndd.del();
			}
		} catch (ResKeyException e) {
			LogUtil.getLog(getClass()).error(e);
		}
	}

}
