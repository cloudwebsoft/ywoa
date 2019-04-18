package com.redmoon.oa.flow;

import java.io.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;

import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileUploadExt;
import com.redmoon.oa.fileark.plugin.*;
import com.redmoon.oa.fileark.plugin.base.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import org.apache.log4j.*;
import com.redmoon.oa.pvg.Privilege;

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
        int ret = 0;
        // logger.info("ret=" + ret);
        try {
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

    public boolean Operate(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);
        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

        // logger.info("op=" + op);
        boolean isValid = false;
        if (op.equals("contribute")) { // || privilege.isValid(request)) { //isAdmin(user, pwdmd5)) {
            isValid = true;
        }

        // if (!isValid)
        //    throw new ErrMsgException(Privilege.MSG_INVALID);

        Document doc = new Document();
        if (op.equals("edit")) {
            String idstr = StrUtil.getNullString(mfu.getFieldValue("id"));
            if (!StrUtil.isNumeric(idstr))
                throw new ErrMsgException("标识id=" + idstr + "非法，必须为数字！");
            id = Integer.parseInt(idstr);
            doc = doc.getDocument(id);

            // if (!lp.canUserModify(privilege.getUser(request)))
            //    throw new ErrMsgException("对不起，您没有权限！");
            boolean re = doc.Update(application, mfu);
            return re;
        } else {
            // if (!lp.canUserAppend(privilege.getUser(request)))
            //    throw new ErrMsgException("对不起，您没有权限！");
            boolean re = doc.create(application, mfu, privilege.getUser(request));
            return re;
        }
    }

    public boolean del(HttpServletRequest request, int id, IPrivilege privilege) throws
            ErrMsgException {
        Document doc = new Document();
        doc = getDocument(id);
        return doc.del();
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
        int votesel = ParamUtil.getInt(request, "votesel");

        Document doc = getDocument(id);
        boolean re = doc.vote(id, votesel);
        return re;
    }

    public boolean OperatePage(ServletContext application,
                           HttpServletRequest request, IPrivilege privilege) throws
            ErrMsgException {
        CMSMultiFileUploadBean mfu = doUpload(application, request);
        String op = StrUtil.getNullStr(mfu.getFieldValue("op"));
        String dir_code = StrUtil.getNullStr(mfu.getFieldValue("dir_code"));

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
     * 往CKEditor中插入图片
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return String[] 图片的ID数组
     * @throws ErrMsgException
     */
    public String[] uploadMedia(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
    	Privilege privilege = new Privilege();
        // if (!privilege.isUserLogin(request))
        //    throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.ERR_NOT_LOGIN));

        FileUploadExt fu = new FileUploadExt();
        
        fu.setMaxFileSize(Global.FileSize);

        String[] ext = new String[] {"flv", "jpg", "gif", "png", "bmp", "swf", "mpg", "asf", "wma", "wmv", "avi", "mov", "mp3", "rm", "ra", "rmvb", "mid", "ram"};
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
        String attPath = cfg.get("file_flow");  
        
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

        String attachmentBasePath = request.getContextPath() + "/";
        
        int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), -1);
        if (flowId==-1) {
            flowId = ParamUtil.getInt(request, "flowId", -1);
        }
        if (flowId > 0) {
	        WorkflowDb wd = new WorkflowDb();
	        wd = wd.getWorkflowDb(flowId);
	        int docId = wd.getDocId();
	        Document doc = new Document();
	        doc = doc.getDocument(docId);
	        DocContent dc = doc.getDocContent(1);
	        int orders = dc.getAttachmentMaxOrders() + 1;
	        
	        if (ir.hasNext()) {
	            FileInfo fi = (FileInfo) ir.next();
	            // 保存至磁盘相应路径
	            String fname = FileUpload.getRandName() + "." +
	                           fi.getExt();
	            // 记录于数据库
	            Attachment att = new Attachment();
	            att.setDiskName(fi.getDiskName());
	            // logger.info(fpath);
	            att.setDocId(docId);
	            att.setName(fi.getName());
	            att.setDiskName(fname);
	            att.setOrders(orders);
	            att.setVisualPath(attPath + "/" + virtualpath);
	            att.setFieldName(fi.fieldName);
	            att.setPageNum(1);
	            att.setCreator(privilege.getUser(request));  
	            att.setFullPath(filepath + fname);
	            if (att.create()) {
	                fi.write(filepath, fname);
	                re = new String[4];
	                re[0] = "" + att.getId();
	                re[1] = attachmentBasePath + att.getVisualPath() + "/" + att.getDiskName();
	                re[2] = fi.uploadSerialNo;
                    re[3] = att.getDiskName();
	            }
	        }
        } else {
        	if (ir.hasNext()) {
        		FileInfo fi = (FileInfo) ir.next();
	            // 保存至磁盘相应路径
	            String fname = FileUpload.getRandName() + "." +
	                           fi.getExt();
	            fi.write(filepath, fname);
                re = new String[4];
				re[0] = "-1";
				re[1] = attachmentBasePath + attPath + "/" + virtualpath + "/"
						+ fname;
				re[2] = fi.uploadSerialNo;
                re[3] = fname;
        	}
        }

        return re;
    }    

    public int getId() {
        return id;
    }

    private int id;
}
