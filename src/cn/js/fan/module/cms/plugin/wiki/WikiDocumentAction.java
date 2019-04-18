package cn.js.fan.module.cms.plugin.wiki;

import java.util.Date;

import com.redmoon.forum.person.UserPropDb;
import com.redmoon.oa.fileark.Document;
import com.redmoon.oa.fileark.DocumentMgr;
import com.redmoon.oa.fileark.LeafPriv;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;
import com.redmoon.oa.fileark.CMSMultiFileUploadBean;

import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ResKeyException;

import javax.servlet.ServletContext;
import org.apache.log4j.Logger;

import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

import cn.js.fan.util.StrUtil;
import cn.js.fan.web.SkinUtil;

import javax.servlet.http.HttpServletRequest;

public class WikiDocumentAction implements IPluginDocumentAction {
    Logger logger = Logger.getLogger(this.getClass().getName());

    public WikiDocumentAction() {
    	
    }

    public boolean create(HttpServletRequest request, Document doc) {
    	// @task:不支持文章型的目录节点
        WikiDocumentDb wkdd = new WikiDocumentDb();
        wkdd.setDocId(doc.getId());
        
		wkdd.setLastEditDate(new java.util.Date());
		wkdd.setLastEditUser(doc.getNick()); 
		
        boolean re = wkdd.create();
        if (re) {
			WikiDocUpdateDb wdud = new WikiDocUpdateDb();
			Config cfg = Config.getInstance();
			String strScore = cfg.getProperty("defaultCreateScore");
			double score = StrUtil.toDouble(strScore, 0.0);
			
			String userName = doc.getNick();
			if (com.redmoon.forum.Privilege.isUserLogin(request)) {
				userName = com.redmoon.forum.Privilege.getUser(request);
			}
			else {
				com.redmoon.forum.person.UserDb user = new com.redmoon.forum.person.UserDb();
				user = user.getUserDbByNick(doc.getNick());
				
				// System.out.println(getClass() + " user=" + user + " doc.getNick()=" + doc.getNick());
				
				if (user!=null)
					userName = user.getName();
			}
			
			int checkStatus = WikiDocUpdateDb.CHECK_STATUS_WAIT;
			if (doc.getExamine()==Document.EXAMINE_PASS)
				checkStatus = WikiDocUpdateDb.CHECK_STATUS_PASSED;
			
			//insert into cms_wiki_doc_update (user_name,doc_id,check_status,edit_date,score) values(?,?,?,?,?)</create>
			try {
				re = wdud.create(new JdbcTemplate(), new Object[]{userName, new Integer(doc.getId()), new Integer(checkStatus), new java.util.Date(), new Double(score), new Integer(1), "", doc.getDirCode()});
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				e.printStackTrace();
			}
        }
        return re;
    }

    public boolean create(ServletContext application, HttpServletRequest request,
                          CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        WikiDocumentDb wkdd = new WikiDocumentDb();
        wkdd.setDocId(doc.getId());
        
		wkdd.setLastEditDate(new java.util.Date());
		wkdd.setLastEditUser(doc.getNick());        
        boolean re = wkdd.create();
        if (re) {
			WikiDocUpdateDb wdud = new WikiDocUpdateDb();
			Config cfg = Config.getInstance();
			String strScore = cfg.getProperty("defaultCreateScore");
			double score = StrUtil.toDouble(strScore, 0.0);
			
			String userName = doc.getNick();
			if (com.redmoon.forum.Privilege.isUserLogin(request)) {
				userName = com.redmoon.forum.Privilege.getUser(request);
			}
			else {
				com.redmoon.forum.person.UserDb user = new com.redmoon.forum.person.UserDb();
				user = user.getUserDbByNick(doc.getNick());
				if (user!=null)
					userName = user.getName();
			}
			
			int checkStatus = WikiDocUpdateDb.CHECK_STATUS_WAIT;
			if (doc.getExamine()==Document.EXAMINE_PASS)
				checkStatus = WikiDocUpdateDb.CHECK_STATUS_PASSED;
			
			//insert into cms_wiki_doc_update (user_name,doc_id,check_status,edit_date,score) values(?,?,?,?,?)</create>
			try {
				re = wdud.create(new JdbcTemplate(), new Object[]{userName, new Integer(doc.getId()), new Integer(checkStatus), new java.util.Date(), new Double(score), new Integer(1), "", doc.getDirCode()});
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				e.printStackTrace();
			}
        }
        return re;
    }

    public boolean update(ServletContext application,HttpServletRequest request,
                   CMSMultiFileUploadBean mfu, Document doc) throws
            ErrMsgException {
        /*
        WikiDocumentDb idd = new WikiDocumentDb();
        idd = idd.getWikiDocumentDb(doc.getId());
        return idd.save();
         */
        
        return true;
    }
    
	public int edit(ServletContext application, HttpServletRequest request)
			throws ErrMsgException {
		com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
		if (!pvg.isUserPrivValid(request, "read")) {
			throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
		}
		
		DocumentMgr dm = new DocumentMgr();
		CMSMultiFileUploadBean mfu = dm.doUpload(application, request);
		String strdoc_id = StrUtil.getNullStr(mfu.getFieldValue("id"));
		int doc_id = Integer.parseInt(strdoc_id);
		Document doc = new Document();
		doc = doc.getDocument(doc_id);
		
		WikiDocumentDb wdd = new WikiDocumentDb();
		wdd = wdd.getWikiDocumentDb(doc_id);
		if (wdd.getStatus()==WikiDocumentDb.STATUS_LOCKED) {
			throw new ErrMsgException("文章已被锁定，不能被编辑！");
		}

		String reason = StrUtil.getNullStr(mfu.getFieldValue("reason"));
		String content = StrUtil.getNullStr(mfu.getFieldValue("htmlcode"));
		
		String userName = pvg.getUser(request);

		boolean re = doc.AddContentPage(application, mfu, content);
		// 有审核权限的人员发布的版本置为通过
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        Privilege privilege = new Privilege();
        int status = -1;
        if (re) {
        	status = WikiDocUpdateDb.CHECK_STATUS_WAIT;
            if (lp.canUserExamine(privilege.getUser(request))) {
            	status = WikiDocUpdateDb.CHECK_STATUS_PASSED;
            }

			WikiDocUpdateDb wdud = new WikiDocUpdateDb();
			//insert into cms_wiki_doc_update (user_name,doc_id,check_status,edit_date,score) values(?,?,?,?,?)</create>
			try {
				wdud.create(new JdbcTemplate(), new Object[]{userName, new Integer(doc_id), new Integer(status), new java.util.Date(), new Integer(0), new Integer(doc.getPageCount()), reason, doc.getDirCode()});
				
				wdd.setLastEditDate(new java.util.Date());
				wdd.setLastEditUser(userName);
				
				if (status==WikiDocUpdateDb.CHECK_STATUS_PASSED) {
					wdud = wdud.getWikiDocUpdateDb(doc_id, doc.getPageCount());
					wdd.setBestId(wdud.getLong("id"));
				}
				
				wdd.save();
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				LogUtil.getLog(getClass()).error(StrUtil.trace(e));
				e.printStackTrace();
			}
		}
		return status;
	}
	
	/**
	 * 注意：Document.delDocumentByDirCode中参数request传过来的为null
	 * 
	 */
    public boolean del(HttpServletRequest request, Document doc, boolean isToDustbin) throws ErrMsgException {
    	WikiDocumentDb wdb = new WikiDocumentDb();
    	wdb = wdb.getWikiDocumentDb(doc.getId());
    	boolean re = true;
    	if (!isToDustbin) {
    		re = wdb.del();
    	}
    	return re;
    }
    
    public boolean examine(HttpServletRequest request, Document doc, int status) throws ErrMsgException {
    	WikiDocUpdateDb wdud = new WikiDocUpdateDb();
    	wdud = wdud.getWikiDocUpdateDb(doc.getId(), 1);
    	if (wdud==null)
    		return false;
    	int updateStatus = WikiDocUpdateDb.CHECK_STATUS_PASSED;
    	if (status==Document.EXAMINE_NOTPASS)
    		updateStatus = WikiDocUpdateDb.CHECK_STATUS_NOTPASSED;
    	else if (status==Document.EXAMINE_NOT)
    		updateStatus = WikiDocUpdateDb.CHECK_STATUS_WAIT;
    	/*
    	else if (status==Document.EXAMINE_DRAFT)
    		updateStatus = WikiDocUpdateDb.CHECK_STATUS_WAIT;
    	*/
    	wdud.set("check_status", new Integer(updateStatus));
    	// 当不通过时,并不表除得分,因为在删除时，会同步删除得分
    	if (status==Document.EXAMINE_PASS) {
    		// 加分
			UserPropDb up = new UserPropDb();
			up = up.getUserPropDb(wdud.getString("user_name"));
			
			Config cfg = Config.getInstance();
			String strScore = cfg.getProperty("defaultCreateScore");
			double score = StrUtil.toDouble(strScore, 0.0);
			
			up.set("wiki_score", new Double(up.getDouble("wiki_score") + score));
			wdud.set("score", new Double(score));
			try {
				up.save();
			} catch (ResKeyException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}    		
    	}

    	try {
			return wdud.save();
		} catch (ResKeyException e) {
			throw new ErrMsgException(e.getMessage(request));
		}
    }
    
}
