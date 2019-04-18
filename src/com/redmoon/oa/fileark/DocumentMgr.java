package com.redmoon.oa.fileark;

import java.io.File;
import java.io.IOException;
import java.util.Calendar;
import java.util.Date;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;

import org.apache.log4j.Logger;

import bsh.EvalError;
import bsh.Interpreter;
import cn.js.fan.base.IPrivilege;
import cn.js.fan.util.DateUtil;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.ParamUtil;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import cn.js.fan.web.Global;
import cn.js.fan.web.SkinUtil;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.forum.person.userservice;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileUploadExt;
import com.redmoon.oa.fileark.plugin.PluginMgr;
import com.redmoon.oa.fileark.plugin.PluginUnit;
import com.redmoon.oa.fileark.plugin.base.IPluginDocumentAction;
import com.redmoon.oa.message.MessageDb;
import com.redmoon.oa.pvg.Privilege;
import com.redmoon.oa.util.BeanShellUtil;

public class DocumentMgr {
    Logger logger = Logger.getLogger(DocumentMgr.class.getName());

    public DocumentMgr() {
    }

    public Document getDocument(int id) {
        Document doc = new Document();
        return doc.getDocument(id);
    }

    public Document getDocument(HttpServletRequest request, int id,
                                IPrivilege privilege) throws
            ErrMsgException {
        boolean isValid = false;
        Document doc = getDocument(id);
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserSee(privilege.getUser(request))) {
            return doc;
        }

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);
        return getDocument(id);
    }

    /**
     * 当directory的结点code的类型为文章时，取其文章，如果文章不存在，则创建文章
     * @param request HttpServletRequest
     * @param code String
     * @param privilege IPrivilege
     * @return Document
     * @throws ErrMsgException
     */
    public Document getDocumentByCode(HttpServletRequest request, String code,
                                      IPrivilege privilege) throws
            ErrMsgException {
        boolean isValid = false;

        LeafPriv lp = new LeafPriv(code);
        if (lp.canUserSee(privilege.getUser(request)))
            isValid = true;

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);
        Document doc = new Document();
        int id = doc.getIDOrCreateByCode(code, privilege.getUser(request));
        return getDocument(id);
    }

    public CMSMultiFileUploadBean doUpload(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = new CMSMultiFileUploadBean();
        mfu.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        // String[] ext = {"htm", "gif", "bmp", "jpg", "png", "rar", "doc", "hs", "ppt", "rar", "zip", "jar"};
        // mfu.setValidExtname(ext);
        
		// 20170814 fgf
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("filearkFileExt").replaceAll("，", ",");
		if (exts.equals("*")) {
			exts = "";
		}
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null)
			mfu.setValidExtname(ext);
		
        int ret = 0;
        // logger.info("ret=" + ret);
        try {
        	// mfu.setDebug(true);
            ret = mfu.doUpload(application, request);
            
            if (ret == -3) {
                throw new ErrMsgException(mfu.getErrMessage());
            }
            if (ret == -4) {
                throw new ErrMsgException(mfu.getErrMessage());
            }
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        return mfu;
    }

    public void passExamineBatch(HttpServletRequest request) throws
            ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids == null)
            return;
        String userNames = null;
        String[] users = null;
        String titles = null;
        String[] myTitles = null;
        String isSendMsg = ParamUtil.get(request, "sendMessage");
        if(isSendMsg.equals("1")){
        	userNames = ParamUtil.get(request, "userNames");
            users = StrUtil.split(userNames, ",");
            
            titles = ParamUtil.get(request, "titles");
            myTitles = StrUtil.split(titles, ",");
        }
        
        int len = ids.length;
        Document doc = null;

        Privilege privilege = new Privilege();
        for (int i = 0; i < len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (lp.canUserExamine(privilege.getUser(request))) {
                if(doc.UpdateExamine(Document.EXAMINE_PASS)){
                	if(isSendMsg.equals("1")){
                		MessageDb md = new MessageDb();
                    	md.sendSysMsg(users[i], "文件："+myTitles[i]+" 审核通过", "审核通过");
                	}
                }
            }
            else {
                throw new ErrMsgException(Privilege.MSG_INVALID);
            }
        }
    }

    public void resumeBatch(HttpServletRequest request) throws ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;

        int len = ids.length;
        Document doc = null;
        for (int i=0; i<len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            doc.UpdateExamine(Document.EXAMINE_PASS);
        }
    }

    public boolean resume(HttpServletRequest request, int id) throws ErrMsgException {
        Document doc = getDocument(id);
        return doc.UpdateExamine(Document.EXAMINE_PASS);
    }

    public void clearDustbin(HttpServletRequest request) throws ErrMsgException {
        Document doc = new Document();
        doc.clearDustbin();
    }
    
    public void delBatch(HttpServletRequest request, boolean isDustbin) throws ErrMsgException {
        Privilege privilege = new Privilege();
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return;
        int len = ids.length;
        for (int i=0; i<len; i++) {
            // doc = getDocument(Integer.parseInt(ids[i]));
            // doc.del();
            del(request, Integer.parseInt(ids[i]), privilege, isDustbin);
        }
    }

    public boolean Operate(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);
        
        fileUpload = mfu;
        
        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));
        
        // 编辑时如果当前文档所在目录为不在前台显示,则dir_code为空
        if (dir_code == null || dir_code.equals("")) {
        	dir_code = ParamUtil.get(request, "dir_code");
        	mfu.setFieldValue("dir_code", dir_code);
        }

        dirCode = dir_code;

        // logger.info("op=" + op);
        boolean isValid = false;
        if (op.equals("contribute")) { // || privilege.isValid(request)) { //isAdmin(user, pwdmd5)) {
            isValid = true;
        }
        else {
            LeafPriv lp = new LeafPriv();
            lp.setDirCode(dir_code);
            if (op.equals("edit")) {
                if (lp.canUserModify(privilege.getUser(request)))
                    isValid = true;
            }
            else {
                if (lp.canUserAppend(privilege.getUser(request)))
                    isValid = true;
            }
        }
        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);

        Document doc = new Document();
        if (op.equals("edit")) {
            String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
            if (!StrUtil.isNumeric(idstr))
                throw new ErrMsgException("标识id=" + idstr + "非法，必须为数字！");
            id = Integer.parseInt(idstr);
            doc = doc.getDocument(id);
            document = doc;

            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserModify(privilege.getUser(request)))
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
            boolean re = doc.Update(application, mfu);
            if (re) {
                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                if (pu != null) {
                    IPluginDocumentAction ipda = pu.getDocumentAction();
                    re = ipda.update(application, request, mfu, doc);
                }
            }
            return re;
        } else {
            LeafPriv lp = new LeafPriv(dir_code);
            if (!lp.canUserAppend(privilege.getUser(request)))
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
            boolean re = doc.create(application, mfu, privilege.getUser(request));
            document = doc;
            id = doc.getID();
            if (re) {
                PluginMgr pm = new PluginMgr();
                PluginUnit pu = pm.getPluginUnitOfDir(dir_code);
                if (pu!=null) {
                    IPluginDocumentAction ipda= pu.getDocumentAction();
                    doc = doc.getDocument(doc.getID());
                    re = ipda.create(application, request, mfu, doc);
                }
                
				Vector documents = new Vector<Document>();
				documents.addElement(doc);  
				launchScriptOnAdd(request, privilege.getUser(request), documents);
            }
            return re;
        }
    }
    
    public void launchScriptOnAdd(HttpServletRequest request, String userName, Vector<Document> documents) {
    	Leaf lfRoot = new Leaf();
    	lfRoot = lfRoot.getLeaf(Leaf.ROOTCODE);
		String script = lfRoot.getScript(Leaf.SCRIPTS_ADD);
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();
				// 赋值用户
				sb.append("userName=\"" + userName + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("request", request);
				bsh.set("documents", documents);

				bsh.eval(script);
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }
    
    public void launchScriptBeforeDel(HttpServletRequest request, String userName, Vector<Document> documents) {
    	Leaf lfRoot = new Leaf();
    	lfRoot = lfRoot.getLeaf(Leaf.ROOTCODE);
		String script = lfRoot.getScript(Leaf.SCRIPTS_DEL);
		if (script != null && !script.equals("")) {
			Interpreter bsh = new Interpreter();
			try {
				StringBuffer sb = new StringBuffer();
				// 赋值用户
				sb.append("userName=\"" + userName + "\";");
				bsh.eval(BeanShellUtil.escape(sb.toString()));

				bsh.set("request", request);
				bsh.set("documents", documents);

				bsh.eval(script);
			} catch (EvalError e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    }    

    public boolean del(HttpServletRequest request, int id, IPrivilege privilege, boolean isDustbin) throws
            ErrMsgException {
        Document doc = new Document();
        doc = getDocument(id);
        if (doc==null || !doc.isLoaded()) {
            throw new ErrMsgException("文件 " + id + " 不存在！");
        }
        LeafPriv lp = new LeafPriv(doc.getDirCode());
        if (lp.canUserDel(privilege.getUser(request))) {
        	boolean re = false;
            if (isDustbin) {
                re = doc.UpdateExamine(Document.EXAMINE_DUSTBIN);
            }
            else {        	
            	Vector<Document> documents = new Vector<Document>();
            	documents.addElement(doc);
            	launchScriptBeforeDel(request,privilege.getUser(request),documents);    
            	
            	re = doc.del();
            }
            if (re) {
	            PluginMgr pm = new PluginMgr();
	            PluginUnit pu = pm.getPluginUnitOfDir(doc.getDirCode());
	            // LogUtil.getLog(getClass()).info("del:" + pu.getCode());
	            if (pu != null) {
	                IPluginDocumentAction ipda = pu.getDocumentAction();
	                re = ipda.del(request, doc, isDustbin);
	            }
	            
	            // 删除日志
	            DocLogDb dld = new DocLogDb();
	            dld.delOfDoc(doc.getId());
	            DocAttachmentLogDb dad = new DocAttachmentLogDb();
	            dad.delOfDoc(doc.getId());
            }
            return re;
        }
        else
            throw new ErrMsgException(Privilege.MSG_INVALID);
    }

    public boolean UpdateSummary(ServletContext application, HttpServletRequest request,
                                 IPrivilege privilege) throws
            ErrMsgException {

            CMSMultiFileUploadBean mfu = doUpload(application, request);
            int id = 0;
            try {
                id = Integer.parseInt(mfu.getFieldValue("id"));
            }
            catch (Exception e) {
                throw new ErrMsgException("id 非法！");
            }
            Document doc = new Document();
            doc = getDocument(id);

            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (!lp.canUserModify(privilege.getUser(request))) {
                throw new ErrMsgException(privilege.MSG_INVALID);
            }

            return doc.UpdateSummary(application, mfu);
    }

    public boolean increaseHit(HttpServletRequest request, int id,
                               IPrivilege privilege) throws
            ErrMsgException {
        Document doc = getDocument(id);
        boolean re = doc.increaseHit();
        return re;
    }

    public boolean UpdateIsHome(HttpServletRequest request, int id,
                                IPrivilege privilege) throws
            ErrMsgException {

        Document doc = new Document();
        String v = ParamUtil.get(request, "value");
        doc.setID(id);
        boolean re = doc.UpdateIsHome(v.equals("y") ? true : false);
        return re;

    }

    public boolean vote(HttpServletRequest request, int id) throws
            ErrMsgException {
               Privilege privilege = new Privilege();
        if (!privilege.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));
        String[] opts = ParamUtil.getParameters(request, "votesel");
        if (opts==null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_vote_none"));

        String name = privilege.getUser(request);


        DocPollDb mpd = new DocPollDb();
        mpd = (DocPollDb)mpd.getQObjectDb(new Integer(id));

        Date d = mpd.getDate("expire_date");

        // 检查是否已过期
        if (d!=null) {
            if (DateUtil.compare(d, new java.util.Date()) != 1)
                throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request,"res.forum.MsgDb",
                        "err_vote_expire"),
                                          new Object[] {DateUtil.format(d, "yyyy-MM-dd")}));
        }

        int len = opts.length;
        int max_choice = mpd.getInt("max_choice");
        if (len > max_choice) {
            throw new ErrMsgException(StrUtil.format(SkinUtil.LoadString(request,"res.forum.MsgDb",
                    "err_vote_max_count"),
                                          new Object[] {"" + max_choice}));
        }

        // 检查用户是否已投过票
        DocPollOptionDb mpod = new DocPollOptionDb();
        Vector v = mpd.getOptions(id);
        int optLen = v.size();
        for (int i=0; i<optLen; i++) {
            DocPollOptionDb mo = mpod.getDocPollOptionDb(id, i);
            String vote_user = StrUtil.getNullString(mo.getString("vote_user"));
            String[] ary = StrUtil.split(vote_user, ",");
            // System.out.println(getClass() + " ary=" + ary);
            if (ary!=null) {
                int len2 = ary.length;
                for (int k=0; k<len2; k++) {
                    if (ary[k].equals(name))
                        throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_vote_repeat"));
                }
            }
        }

        boolean re = true;
        for (int i=0; i<len; i++) {
            DocPollOptionDb mo = mpod.getDocPollOptionDb(id, StrUtil.toInt(opts[i]));
            mo.set("vote_count", new Integer(mo.getInt("vote_count") + 1));
            String vote_user = StrUtil.getNullString(mo.getString("vote_user"));
            if (vote_user.equals(""))
                vote_user = name;
            else
                vote_user += "," + name;
            mo.set("vote_user", vote_user);
            try {
                re = mo.save();
            }
            catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }

        return re;
    }

    public boolean OperatePage(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);
        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

        boolean isValid = false;

        LeafPriv lp = new LeafPriv();
        lp.setDirCode(dir_code);
        if (op.equals("add")) {
            if (lp.canUserAppend(privilege.getUser(request)))
                isValid = true;
        }
        if (op.equals("edit")) {
            if (lp.canUserModify(privilege.getUser(request)))
                isValid = true;
        }

        if (!isValid)
            throw new ErrMsgException(Privilege.MSG_INVALID);

        String strdoc_id = StrUtil.getNullStr(mfu.getFieldValue("id"));
        int doc_id = Integer.parseInt(strdoc_id);
        Document doc = new Document();
        doc = doc.getDocument(doc_id);

        // logger.info("filepath=" + mfu.getFieldValue("filepath"));

        if (op.equals("add")) {
            String content = StrUtil.getNullStr(mfu.getFieldValue(
                    "htmlcode"));
            return doc.AddContentPage(application, mfu, content);
        }

        if (op.equals("edit")) {
            // return doc.EditContentPage(content, pageNum);
            return doc.EditContentPage(application, mfu);
        }

        return false;
    }


    /**
     * 往文章中插入图片
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return String[] 图片的ID数组
     * @throws ErrMsgException
     */
    public String[] uploadImg(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        // if (!privilege.isUserLogin(request))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));

        FileUploadExt fu = new FileUploadExt();
        
        fu.setMaxFileSize(Global.FileSize);

        String[] ext = new String[] {"flv", "jpg", "jpeg", "gif", "png", "bmp", "swf", "mpg", "asf", "wma", "wmv", "avi", "mov", "mp3", "rm", "ra", "rmvb", "mid", "ram"};
        if (ext!=null)
            fu.setValidExtname(ext);

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret!=FileUploadExt.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
            if (fu.getFiles().size()==0)
                throw new ErrMsgException("请上传文件！");
        } catch (IOException e) {
            logger.error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }

        Calendar cal = Calendar.getInstance();
		String year = "" + (cal.get(Calendar.YEAR));
		String month = "" + (cal.get(Calendar.MONTH) + 1);
		String virtualpath = year + "/" + month;

		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String attPath = cfg.get("file_folder");

        String filepath = Global.getRealPath() + attPath + "/" +
                          virtualpath + "/";

        File f = new File(filepath);
        if (!f.isDirectory()) {
            f.mkdirs();
        }

        fu.setSavePath(filepath); // 设置保存的目录
        // logger.info(filepath);
        String[] re = null;
        Vector v = fu.getFiles();
        Iterator ir = v.iterator();
        int orders = 0;

        String attachmentBasePath = "/";

        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // 保存至磁盘相应路径
            String fname = FileUpload.getRandName() + "." +
                           fi.getExt();
            // 记录于数据库
            com.redmoon.oa.fileark.Attachment att = new Attachment();
            att.setDiskName(fi.getDiskName());
            // logger.info(fpath);
            att.setDocId(Attachment.TEMP_DOC_ID);
            att.setName(fi.getName());
            att.setDiskName(fname);
            att.setOrders(orders);
            att.setVisualPath(attPath + "/" + virtualpath);
            att.setUploadDate(new java.util.Date());
            att.setSize(fi.getSize());
            att.setExt(StrUtil.getFileExt(fi.getName()));
            att.setEmbedded(true);
            String module = ParamUtil.get(request, "module");
            if (module.equals("notice")) {
            	att.setPageNum(1);
            }
            if (att.create()) {
                fi.write(filepath, fname);
                re = new String[3];
                re[0] = "" + att.getId();

                re[1] = attachmentBasePath + att.getVisualPath() + "/" + att.getDiskName();
                re[2] = fi.uploadSerialNo;
            }
        }

        return re;
    }
    
    /**
     * 创建Office文件
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean uploadDocument(ServletContext application,
                                  HttpServletRequest request) throws
            ErrMsgException {
        String[] extnames = {"doc", "docx", "xls", "xlsx"};
        FileUpload TheBean = new FileUpload();
        TheBean.setValidExtname(extnames); // 设置可上传的文件类型
        TheBean.setMaxFileSize(Global.FileSize); // 最大35000K
        int ret = 0;
        try {
            ret = TheBean.doUpload(application, request);
            if (ret!=FileUploadExt.RET_SUCCESS) {
                throw new ErrMsgException(TheBean.getErrMessage(request));
            }
        } catch (Exception e) {
            logger.error("uploadDocument:" + e.getMessage());
        }
        if (ret == 1) {
            Document doc = new Document();
            return doc.uploadDocument(TheBean);
        } else
            return false;
    }   
    
    /**
     * webedit快速上传
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean upload(ServletContext application, HttpServletRequest request)
	throws ErrMsgException {
    	DocumentFileUploadBean mfu = new DocumentFileUploadBean();
		mfu.setMaxFileSize(Global.FileSize); // 35000 // 最大35000K

		// 20170814 fgf 原来用的是网盘的扩展名配置
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("filearkFileExt").replaceAll("，", ",");
		if (exts.equals("*")) {
			exts = "";
		}
		String[] ext = StrUtil.split(exts, ",");
		if (ext != null)
			mfu.setValidExtname(ext);

		int ret = 0;
		try {
			ret = mfu.doUpload(application, request);
			if (ret != FileUpload.RET_SUCCESS) {
				if(ret == -4){
					throw new ErrMsgException("操作失败，扩展名非法");
				}else if(ret == -3){
					throw new ErrMsgException("操作失败，单个文件大小超过了预设的最大值");
				}else if(ret == -2){
					throw new ErrMsgException("操作失败，文件总的大小超过了预设的最大值");
				}else{
					throw new ErrMsgException(mfu.getErrMessage());
				}
			}
		} catch (Exception e) {
			//LogUtil.getLog(getClass().getName()).error(
				//	"upload:" + e.getMessage());
			throw new ErrMsgException(e.getMessage());
		}

		Vector v = new Vector<Document>();

		if (ret == FileUpload.RET_SUCCESS) {
			String dirCode = ParamUtil.get(request, "dirCode");
			//String dirCode = mfu.getFieldValue("dirCode");
			Privilege privilege = new Privilege();
			
			
			String userName = privilege.getUser(request);

			Leaf lf = new Leaf();
			lf = lf.getLeaf(dirCode);
			
			if (lf==null)
				throw new ErrMsgException("目录不存在!");

			String visualPath = StrUtil.getNullString(mfu
					.getFieldValue("filepath"));

			String FilePath = cfg.get("file_folder");
			if (!visualPath.equals(""))
				FilePath += "/" + visualPath;

			String attSavePath = Global.getRealPath() + FilePath + "/";

			mfu.setSavePath(attSavePath); // 取得目录

			LogUtil.getLog(getClass()).info(
					"attSavePath=" + attSavePath);

			File f = new File(attSavePath);
			if (!f.isDirectory()) {
				f.mkdirs();
			}
			// 检查上传的文件大小有没有超出磁盘空间
			Vector attachs = mfu.getFiles();
			if (attachs.size()==0)
				attachs = mfu.getAttachments();

			// 检查是不是按目录上传
			String upDirCode = "";
			String uploadDirName = StrUtil.getNullStr(mfu.getFieldValue("uploadDirName"));
			if (!uploadDirName.equals("")) {
				// 取得uploadDirName中的目录名称，检测是否已存在，如不存在，则创建该目录
				int index = uploadDirName.lastIndexOf("\\");
				String upDirName = uploadDirName.substring(index + 1);
				// 检查是否存在同名目录
				boolean isExist = false;
				Iterator irlf = lf.getChildren().iterator();
				while (irlf.hasNext()) {
					Leaf child = (Leaf) irlf.next();
					if (child.getName().equals(upDirName)) {
						upDirCode = child.getCode();
						isExist = true;
						break;
					}
				}
				// 如果不存在同名目录，则创建
				if (!isExist) {
					// 创建uploadDirName节点及物理目录
					Leaf lfCh = new Leaf();
					lfCh.setName(upDirName);
					upDirCode = FileUpload.getRandName();
					lfCh.setCode(upDirCode);
					lfCh.setParentCode(lf.getCode());
					lfCh.setDescription(lf.getCode());
					lfCh.setType(Leaf.TYPE_LIST);
					//lfCh.setSystem(true);
					boolean flag = lf.AddChild(lfCh);
					if(!flag){
						throw new ErrMsgException("操作失败,文件名太长!");
						//return flag;
					}
					String savePath = Global.getRealPath()
							+ cfg.get("file_folder") + "/"
							+ lfCh.getFilePath() + "/";
					// 检查物理目录是否存在，如果不存在，则创建
					// System.out.println(getClass() + savePath +
					// " cfg.get(\"file_netdisk\")=" +
					// cfg.get("file_netdisk"));
					f = new File(savePath);
					if (!f.isDirectory()) {
						f.mkdirs();
					}
				}
			}

			LogUtil.getLog(getClass()).info(
					"uploadDirName=" + uploadDirName + " att size="
							+ attachs.size() + " file size=" + mfu.getFiles().size());

			Iterator ir = attachs.iterator();
			while (ir.hasNext()) {
				FileInfo fi = (FileInfo) ir.next();

				String myVisualPath = visualPath;
				String savePath = mfu.getSavePath();
				String curDirCode = dirCode;

				LogUtil.getLog(getClass()).info(
						"fi.clientPath=" + fi.clientPath + " uploadDirName="
								+ uploadDirName + " savePath=" + savePath);

				if (!uploadDirName.equals("")) {
					// 为该文件在数据库及磁盘创建相应目录					
					// 检查目录是否包含于客户端路径中
					int p = fi.clientPath.indexOf(uploadDirName);
					if (p != -1) {
						// 在循环中，lf的child_count会变化，缓存会被刷新，因此在这里要重新获取
						lf = lf.getLeaf(upDirCode);
						
						curDirCode = lf.getCode();

						//myVisualPath = lf.getFilePath();
						Calendar cal = Calendar.getInstance();
						String year = "" + (cal.get(Calendar.YEAR));
						String month = "" + (cal.get(Calendar.MONTH) + 1);
						myVisualPath = year + "/" + month;
						
						savePath = Global.getRealPath()
								+ cfg.get("file_folder") + "/"
								+ myVisualPath + "/";

						// 取得upoadDirName后的路径
						String path = fi.clientPath.substring(p
								+ uploadDirName.length() + 1);
						
						// 检查path在树形结构上是否已存在，如果不存在，则创建目录节点，并创建物理目录
						String[] ary = path.split("\\\\");				

						Leaf plf = lf;
						Leaf lfCh = null;
						
						LogUtil.getLog(getClass()).info(
								"path=" + path + " ary.len=" + ary.length + " plf.getName=" + plf.getName());		
						
						// 数组中最后一位是文件名，因此不用处理
						for (int i = 0; i < ary.length - 1; i++) {
							// 检查在孩子节点中是否存在
							boolean isFound = false;
							Iterator irLf = plf.getChildren().iterator();
							while (irLf.hasNext()) {
								Leaf lf2 = (Leaf) irLf.next();
								LogUtil.getLog(getClass()).info(
										"lf2.getName()=" + lf2.getName()
												+ " ary[i]=" + ary[i]);
								if (lf2.getName().equals(ary[i])) {
									isFound = true;
									lfCh = lf2;
									plf = lf2;
									break;
								}
							}
							LogUtil.getLog(getClass()).info(
									"isFound=" + isFound);
							if (!isFound) {
								// 创建节点及物理目录
								lfCh = new Leaf();
								lfCh.setName(ary[i]);
								lfCh.setCode(FileUpload.getRandName());
								lfCh.setParentCode(plf.getCode());
								lfCh.setDescription(ary[i]);
								lfCh.setType(Leaf.TYPE_LIST);
								//lfCh.setSystem(true);
								boolean flag = plf.AddChild(lfCh);
								if(!flag){
									throw new ErrMsgException("操作失败,文件名太长!");
									//return flag;
								}
								lfCh = lfCh.getLeaf(lfCh.getCode());
								plf = lfCh;
							}
							curDirCode = lfCh.getCode();
						}

						if (lfCh != null) {
							//myVisualPath = lfCh.getFilePath();
							cal = Calendar.getInstance();
							year = "" + (cal.get(Calendar.YEAR));
							month = "" + (cal.get(Calendar.MONTH) + 1);
							myVisualPath = year + "/" + month;

							savePath = Global.getRealPath()
									+ cfg.get("file_folder") + "/"
									+ myVisualPath + "/";
							
							LogUtil.getLog(getClass()).info("savePath=" + savePath + " curDirCode=" + curDirCode);							
							// 检查物理目录是否存在，如果不存在，则创建
							f = new File(savePath);
							if (!f.isDirectory()) {
								f.mkdirs();
							}
						}
					}
				}

				if(uploadDirName.equals("")){
					//savePath += "/" + lf.getName()+"/";
					Calendar cal = Calendar.getInstance();
					String year = "" + (cal.get(Calendar.YEAR));
					String month = "" + (cal.get(Calendar.MONTH) + 1);
					String visual_Path = year + "/" + month;
					
					savePath = Global.getRealPath() + cfg.get("file_folder")
							+ "/" + visual_Path + "/";
				}
				

				fi.write(savePath, true);
				
				if(!uploadDirName.equals("")){
						
					//保存文档
					Document document = new Document();
					int docId = document.create(fi.getName(),curDirCode,curDirCode,userName);
					v.addElement(document);
					
					Attachment att = new Attachment();
					
					String name = fi.getName();
					String[] nameArr = name.split("\\.");
					
					String toghterName1 = "";
					for(int i=0;i<=nameArr.length-2;i++){
						if(i == (nameArr.length-2)){
							toghterName1 += nameArr[i];
						}else{
							toghterName1 += nameArr[i]+".";
						}
					}
					
					int num = att.findAttachNum(toghterName1, savePath,1,nameArr[nameArr.length-1]);
					//if( num != 0){
					//	name = nameArr[0]+"("+num+")"+"."+nameArr[1];
					//}
					if( num != 0){
						String toghterName = "";
						for(int i=0;i<=nameArr.length-2;i++){
							if(i == (nameArr.length-2)){
								toghterName += nameArr[i];
							}else{
								toghterName += nameArr[i]+".";
							}
						}
						name = toghterName+"("+num+")"+"."+nameArr[nameArr.length-1];
					}
					
					att.setName(name);
					att.setDiskName(fi.getDiskName());
					att.setVisualPath(cfg.get("file_folder") + "/"+myVisualPath);
					att.setSize(fi.getSize());
					att.setExt(fi.getExt());
					att.setDocId(docId);
					att.setFullPath(savePath+fi.getDiskName());
					att.setPageNum(1);
					att.setOrders(1);
					att.setDownloadCount(0);
					att.setUploadDate(new Date());
					att.setEmbedded(false);
					att.create();
					
				}
				

				if(uploadDirName.equals("")){
					//Iterator ir1 = attachs.iterator();
					//while(ir1.hasNext()){
					//	FileInfo fi1 = (FileInfo) ir1.next();
					//保存文档
					Document document = new Document();
					int docId = document.create(fi.getName(),dirCode,dirCode,userName);
					v.addElement(document);

					Attachment att = new Attachment();
					
					String name = fi.getName();
					String[] nameArr = name.split("\\.");
					String toghterName1 = "";
					for(int i=0;i<=nameArr.length-2;i++){
						if(i == (nameArr.length-2)){
							toghterName1 += nameArr[i];
						}else{
							toghterName1 += nameArr[i]+".";
						}
					}
					int num = att.findAttachNum(toghterName1, savePath,1,nameArr[nameArr.length-1]);
					if( num != 0){
						String toghterName = "";
						for(int i=0;i<=nameArr.length-2;i++){
							if(i == (nameArr.length-2)){
								toghterName += nameArr[i];
							}else{
								toghterName += nameArr[i]+".";
							}
						}
						name = toghterName+"("+num+")"+"."+nameArr[nameArr.length-1];
					}
					//if( num != 0){
					//	name = nameArr[0]+"("+num+")"+"."+nameArr[1];
					//}
					lf = new Leaf(dirCode);
					Calendar cal = Calendar.getInstance();
					String year = "" + (cal.get(Calendar.YEAR));
					String month = "" + (cal.get(Calendar.MONTH) + 1);
					myVisualPath = year + "/" + month;
					String visualPath1 = cfg.get("file_folder") + "/"+ myVisualPath ;
					att.setName(name);
					att.setDiskName(fi.getDiskName());
					att.setVisualPath(visualPath1);
					att.setSize(fi.getSize());
					att.setExt(fi.getExt());
					att.setDocId(docId);
					att.setFullPath(savePath+fi.getDiskName());
					att.setPageNum(1);
					att.setOrders(1);
					att.setDownloadCount(0);
					att.setUploadDate(new Date());
					att.setEmbedded(false);
					att.create();
					//}
				}
			}
			launchScriptOnAdd(request, privilege.getUser(request), v);

			return true;
		} else
			return false;
    	
    }
    
    /**
     * swfupload上传文件
     * @param application
     * @param request
     * @return
     * @throws ErrMsgException
     */
    public boolean swfUploadDocument(ServletContext application,
    		HttpServletRequest request) throws
    		ErrMsgException {
    	boolean flag = false;
		String contentType = request.getContentType();
		if (contentType.indexOf("multipart/form-data") == -1) {
			throw new IllegalStateException(
					"The content type of request is not multipart/form-data");
		}
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		String exts = cfg.get("filearkFileExt").replaceAll("，", ",");
		if (exts.equals("*")) {
			exts = "";
		}
		FileUpload fu = new FileUpload();
		String[] extAry = StrUtil.split(exts, ",");
		if (extAry != null) {
			fu.setValidExtname(extAry);
		}
		fu.setMaxFileSize(Global.FileSize); 
		int ret = -1;
		try {
			ret = fu.doUpload(application, request);
		} catch (IOException e) {
			throw new ErrMsgException(e.getMessage());
		}

		flag = writeFile(request, fu);
		return flag;
    	
    }    
    
	public boolean writeFile(HttpServletRequest request, FileUpload fu)
			throws ErrMsgException {
		String userName = ParamUtil.get(request, "userName");
		Privilege privilege = new Privilege();
		if (userName == null || userName.equals("")) {
			userName = privilege.getUser(request);
		}
		boolean flag = true;
		String dirCode = ParamUtil.get(request, "dirCode");
		Leaf leaf = new Leaf(dirCode);
		
		com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
		//String myVisualPath = leaf.getName();
		Calendar cal = Calendar.getInstance();
		String year = "" + (cal.get(Calendar.YEAR));
		String month = "" + (cal.get(Calendar.MONTH) + 1);
		String myVisualPath = year + "/" + month;
		
		String savePath = Global.getRealPath() + cfg.get("file_folder")
				+ "/" + myVisualPath + "/";
		
		// 如果没有物理文件夹创建
		File f = new File(savePath);
		if (!f.isDirectory()) {
			f.mkdirs();
		}

		if (fu.getRet() == FileUpload.RET_SUCCESS) {
			Vector<Document> documents = new Vector<Document>();
			Vector v = fu.getFiles();
			Iterator ir = v.iterator();
			// 置路径
			fu.setSavePath(savePath);
			if (ir.hasNext()) {
				FileInfo fi = (FileInfo) ir.next();
				
				//保存文档
				Document document = new Document();
				int docId = document.create(fi.getName(),dirCode,dirCode, userName);
				documents.addElement(document);
				
				// 使用随机名称写入磁盘
				fi.write(fu.getSavePath(), true);
				Attachment att = new Attachment();
				
				String name = fi.getName();
				String[] nameArr = name.split("\\.");
				String toghterName1 = "";
				for(int i=0;i<=nameArr.length-2;i++){
					if(i == (nameArr.length-2)){
						toghterName1 += nameArr[i];
					}else{
						toghterName1 += nameArr[i]+".";
					}
				}
				int num = att.findAttachNum(toghterName1, cfg.get("file_folder")+ "/" +myVisualPath,1,nameArr[nameArr.length-1]);
				if( num != 0){
					String toghterName = "";
					for(int i=0;i<=nameArr.length-2;i++){
						if(i == (nameArr.length-2)){
							toghterName += nameArr[i];
						}else{
							toghterName += nameArr[i]+".";
						}
					}
					name = toghterName+"("+num+")"+"."+nameArr[nameArr.length-1];
				}
				
				att.setName(name);
				att.setDiskName(fi.getDiskName());
				cal = Calendar.getInstance();
				year = "" + (cal.get(Calendar.YEAR));
				month = "" + (cal.get(Calendar.MONTH) + 1);
				String visual_Path = year + "/" + month;
				att.setVisualPath(cfg.get("file_folder")+ "/" +visual_Path);
				att.setSize(fi.getSize());
				att.setExt(fi.getExt());
				att.setDocId(docId);
				att.setFullPath(fu.getSavePath() + fi.getDiskName());
				att.setPageNum(1);
				att.setOrders(1);
				att.setDownloadCount(0);
				att.setUploadDate(new Date());
				att.setEmbedded(false);
				flag = att.create();
			}

			launchScriptOnAdd(request, privilege.getUser(request), documents);

		} else {
			flag = false;
		}

		return flag;
	}
    
    public int getId() {
        return id;
    }
    
    public FileUpload getFileUpload() {
        return fileUpload;
    }

    public String getDirCode() {
        return dirCode;
    }
    
    public Document getDocument() {
    	return document;
    }

    private FileUpload fileUpload;

    private String dirCode;

    private int id;
    
    /**
     * 用以记录创建的文档
     */
    private Document document;
    
}
