package cn.js.fan.module.cms.plugin.wiki;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.oa.fileark.Document;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.StrUtil;

public class WikiDocumentMgr {

	public void lockBatch(HttpServletRequest request) throws ErrMsgException {
		int status = ParamUtil.getInt(request, "status", WikiDocumentDb.STATUS_LOCKED);
		
		String strIds = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strIds, ",");
		int len = 0;
		if (ids!=null)
			len = ids.length;
		WikiDocumentDb wdd = new WikiDocumentDb();
		for (int i=0; i<len; i++) {
			wdd = wdd.getWikiDocumentDb(StrUtil.toInt(ids[i]));
			wdd.setStatus(status);
			wdd.save();
		}
	}
	
	/**
	 * 置顶
	 * @param request
	 */
	public void setLevel(HttpServletRequest request) throws ErrMsgException {
		int level = ParamUtil.getInt(request, "level", WikiDocumentDb.LEVEL_TOP);
		
		String strIds = ParamUtil.get(request, "ids");
		String[] ids = StrUtil.split(strIds, ",");
		int len = 0;
		if (ids!=null)
			len = ids.length;
		WikiDocumentDb wdd = new WikiDocumentDb();
		Document doc = new Document();
		for (int i=0; i<len; i++) {
			wdd = wdd.getWikiDocumentDb(StrUtil.toInt(ids[i]));
			wdd.setLevel(level);
			wdd.save();
			
			System.out.println(getClass() + " " + wdd.getDocId() + " " + level);
			
			doc = doc.getDocument(wdd.getDocId());
			doc.setLevel(level);
			doc.UpdateLevel();
			
		}		
	}
}
