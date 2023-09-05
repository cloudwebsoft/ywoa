package com.redmoon.oa.flow;

import java.io.*;
import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;

import com.cloudweb.oa.api.IObsService;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudweb.oa.utils.SysUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUploadExt;
import cn.js.fan.util.*;
import cn.js.fan.web.*;

import com.redmoon.oa.visual.FormDAO;
import com.redmoon.oa.pvg.Privilege;

public class DocumentMgr {

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
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }
        return mfu;
    }

    public boolean vote(HttpServletRequest request, int id) throws
            ErrMsgException {
        int votesel = ParamUtil.getInt(request, "votesel");

        Document doc = getDocument(id);
        return doc.vote(id, votesel);
    }

    /**
     * 往UEditor中插入图片
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

        com.redmoon.oa.Config cfg = com.redmoon.oa.Config.getInstance();
		/*
        String exts = cfg.get("flowFileExt").replaceAll("，", ",");
        if ("*".equals(exts)) {
            exts = "";
        }
        String[] ext = StrUtil.split(exts, ",");
        if (ext != null) {
            fu.setValidExtname(ext);
        }
		*/

        String[] ext = new String[] {"vob", "m4v", "flv", "jpg", "jpeg", "gif", "mp4", "png", "bmp", "swf", "mpg", "asf", "wma", "wmv", "avi", "mov", "mp3", "rm", "ra", "rmvb", "mid", "ram"};
        fu.setValidExtname(ext);

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret != FileUploadExt.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
            if (fu.getFiles().size()==0) {
                throw new ErrMsgException("请上传文件！");
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }

        String[] re = null;
        Vector<FileInfo> v = fu.getFiles();
        Iterator<FileInfo> ir = v.iterator();

        // String attachmentBasePath = Global.getFullRootPath(request) + "/";
        SysUtil sysUtil = SpringUtil.getBean(SysUtil.class);
        String attachmentBasePath = sysUtil.getRootPath() + "/showImg?path=";

        int flowId = StrUtil.toInt(fu.getFieldValue("flowId"), -1);
        if (flowId==-1) {
            flowId = ParamUtil.getInt(request, "flowId", -1);
        }

        // 流程中处理
        if (flowId > 0) {
            Calendar cal = Calendar.getInstance();
            String year = "" + (cal.get(Calendar.YEAR));
            String month = "" + (cal.get(Calendar.MONTH) + 1);
            String virtualpath = year + "/" + month;

            String attPath = cfg.get("file_flow");

            WorkflowDb wd = new WorkflowDb();
	        wd = wd.getWorkflowDb(flowId);
	        int docId = wd.getDocId();
	        Document doc = new Document();
	        doc = doc.getDocument(docId);
	        DocContent dc = doc.getDocContent(1);
	        int orders = dc.getAttachmentMaxOrders() + 1;
	        
	        if (ir.hasNext()) {
	            FileInfo fi = ir.next();
                IFileService fileService = SpringUtil.getBean(IFileService.class);
                fileService.write(fi, attPath + "/" + virtualpath);

	            // 记录于数据库
	            Attachment att = new Attachment();
	            att.setDiskName(fi.getDiskName());
	            att.setDocId(docId);
	            att.setName(fi.getName());
	            att.setDiskName(fi.getDiskName());
	            att.setOrders(orders);
	            att.setVisualPath(attPath + "/" + virtualpath);
	            att.setFieldName(fi.fieldName);
	            att.setPageNum(1);
	            att.setCreator(privilege.getUser(request));
	            att.setFlowId(flowId);
	            if (att.create()) {
	                re = new String[4];
	                re[0] = "" + att.getId();
	                re[1] = attachmentBasePath + att.getVisualPath() + "/" + att.getDiskName();
	                re[2] = fi.uploadSerialNo;
                    re[3] = att.getDiskName();
	            }
	        }
        } else {
            // 智能模块中处理
        	if (ir.hasNext()) {
                FileInfo fi = ir.next();
                re = new String[4];
                re[0] = "-1";
                re[2] = fi.uploadSerialNo;

                long id = ParamUtil.getLong(request, "id", -1);
                String formCode = ParamUtil.get(request, "formCode");
                if (!"".equals(formCode)) {
                    FormDb fd = new FormDb();
                    fd = fd.getFormDb(formCode);
                    FormDAO fdao = new FormDAO(fd);
                    String vpath = fdao.getVisualPath();

                    IFileService fileService = SpringUtil.getBean(IFileService.class);
                    fileService.write(fi, vpath);
                    re[3] = fi.getDiskName();

                    com.redmoon.oa.visual.Attachment att = new com.redmoon.oa.visual.Attachment();
                    if (id!=-1) {
                        att.setVisualId(id);
                    }
                    else {
                        att.setVisualId(com.redmoon.oa.visual.Attachment.TEMP_VISUAL_ID);
                    }
                    att.setName(fi.getName());
                    att.setDiskName(fi.getDiskName());
                    att.setVisualPath(vpath);
                    att.setFormCode(formCode);
                    att.setFieldName(fi.getFieldName());
                    att.setCreator(privilege.getUser(request));
                    att.setFileSize(fi.getSize());
                    if (att.create()) {
                        re[1] = attachmentBasePath + vpath + "/" + fi.getDiskName();
                    }
                }
        	}
        }

        return re;
    }    

    public int getId() {
        return id;
    }

    private int id;
}
