package cn.js.fan.module.cms.plugin.wiki;

import javax.servlet.http.HttpServletRequest;

import com.redmoon.forum.person.UserPropDb;

import com.redmoon.oa.fileark.DocContent;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.DocumentMgr;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;
import com.redmoon.oa.fileark.CMSMultiFileUploadBean;
import cn.js.fan.util.*;

public class WikiDocUpdateMgr {

		public boolean mark(HttpServletRequest request) throws ResKeyException {
			String idsStr = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(idsStr, ",");
			double score = ParamUtil.getDouble(request, "score", 0);
			int len = 0;
			if (ary!=null)
				len = ary.length;
			WikiDocUpdateDb wdud2 = new WikiDocUpdateDb();
		    UserPropDb up2 = new UserPropDb();
			for (int i=0; i<len; i++) {
				long id = StrUtil.toLong(ary[i]);
				WikiDocUpdateDb wd = (WikiDocUpdateDb)wdud2.getQObjectDb(new Long(id));
				wd.set("score", new Double(score));
				wd.save();
				// 加分
				UserPropDb up = up2.getUserPropDb(wd.getString("user_name"));
				up.set("wiki_score", new Double(up.getDouble("wiki_score") + score));
				up.save();
			}
			return true;		
		}
		
		public boolean pass(HttpServletRequest request) throws ResKeyException {
			String idsStr = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(idsStr, ",");
			int checkStatus = ParamUtil.getInt(request, "checkStatus", WikiDocUpdateDb.CHECK_STATUS_WAIT);
			int len = 0;
			if (ary!=null)
				len = ary.length;
			
			Config cfg = Config.getInstance();
			String strCreateScore = cfg.getProperty("defaultCreateScore");
			double createScore = StrUtil.toDouble(strCreateScore, 0.0);			
			String strEditScore = cfg.getProperty("defaultEditScore");
			double editScore = StrUtil.toDouble(strEditScore, 0.0);
			
			WikiDocUpdateDb wdud2 = new WikiDocUpdateDb();
			for (int i=0; i<len; i++) {
				long id = StrUtil.toLong(ary[i]);
				WikiDocUpdateDb wd = (WikiDocUpdateDb)wdud2.getQObjectDb(new Long(id));
				wd.set("check_status", new Integer(checkStatus));
				if (wd.getInt("page_num")==1)
					wd.set("score", createScore);
				else
					wd.set("score", new Double(editScore));
				boolean re = wd.save();
				if (re) {
					if (checkStatus == WikiDocUpdateDb.CHECK_STATUS_PASSED) {
						WikiDocumentDb wdd = new WikiDocumentDb();
						wdd = wdd.getWikiDocumentDb(wd.getInt("doc_id"));
						wdd.setBestId(wd.getLong("id"));
						wdd.save();
					}
				}
			}
			return true;
		}
		
		public boolean del(HttpServletRequest request) throws ErrMsgException {
			String idsStr = ParamUtil.get(request, "ids");
			String[] ary = StrUtil.split(idsStr, ",");
			int len = 0;
			if (ary!=null)
				len = ary.length;
			WikiDocUpdateDb wdud2 = new WikiDocUpdateDb();
			WikiDocumentDb wdd = new WikiDocumentDb();
			Document doc = new Document();
			DocContent dc = new DocContent();
			for (int i=0; i<len; i++) {
				long id = StrUtil.toLong(ary[i]);
				WikiDocUpdateDb wdud = (WikiDocUpdateDb)wdud2.getQObjectDb(new Long(id));
				
				// 如果该文章只有一页则删除文章
				doc = doc.getDocument(wdud.getInt("doc_id"));
				if (doc.getPageCount()==1) {
					doc.del();
					continue;
				}
				
				int docId = wdud.getInt("doc_id");
				
				// 这里还需删除文章中相应的页
				dc = dc.getDocContent(docId, wdud.getInt("page_num"));
				dc.del();

				try {
					wdud.del();					
				} catch (ResKeyException e) {
					// TODO Auto-generated catch block
					throw new ErrMsgException(e.getMessage(request));
				}

				// 重置最好页码
				wdd = wdd.getWikiDocumentDb(docId);
				if (wdd.getBestId()==id) {
					wdd.initBestPageNum();
				}				
			}
			return true;
		}		
		
}
