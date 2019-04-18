package cn.js.fan.module.cms.plugin.wiki;

import java.util.Iterator;

import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.person.UserPropDb;

public class WikiDocUpdateDb extends QObjectDb {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	public static final int CHECK_STATUS_NOTPASSED = 0;
	public static final int CHECK_STATUS_WAIT = 1;
	public static final int CHECK_STATUS_PASSED = 2;
	public int checkStatus = CHECK_STATUS_WAIT;
	
	public static String getCheckStatusDesc(int status) {
		if (status==CHECK_STATUS_NOTPASSED)
			return "不通过";
		else if (status==CHECK_STATUS_WAIT)
			return "待审批";
		else if (status==CHECK_STATUS_PASSED)
			return "通过";
		else
			return "";
	}
	
	public void delOfDoc(int docId) {
		String sql = "select id from " + getTable().getName() + " where doc_id=?";
		Iterator ir = list(sql, new Object[]{new Integer(docId)}).iterator();
		while (ir.hasNext()) {
			WikiDocUpdateDb wdud = (WikiDocUpdateDb)ir.next();
			try {
				wdud.del();
				
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
			}
		}
	}
	
	public WikiDocUpdateDb getWikiDocUpdateDb(long id) {
		return (WikiDocUpdateDb)getQObjectDb(new Long(id));
	}	
	
	public WikiDocUpdateDb getWikiDocUpdateDb(int docId, int pageNum) {
		String sql = "select id from " + getTable().getName() + " where doc_id=? and page_num=?";
				
		Iterator ir = list(sql, new Object[]{new Integer(docId), new Integer(pageNum)}).iterator();
		if (ir.hasNext()) {
			return (WikiDocUpdateDb)ir.next();
		}
		return null;
	}
	
	public boolean del() throws ResKeyException {
		boolean re = super.del();
		
		// 减分
	    UserPropDb up = new UserPropDb();
		up = up.getUserPropDb(getString("user_name"));
		if (up!=null) {
			up.set("wiki_score", new Double(up.getDouble("wiki_score") - getDouble("score")));
			up.save();	
		}
		return re;
	}

}
