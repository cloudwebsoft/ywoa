package com.redmoon.oa.help;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.base.*;

import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.kit.util.FileUploadExt;

import cn.js.fan.util.*;
import cn.js.fan.web.*;

import org.apache.log4j.*;
import com.redmoon.oa.pvg.Privilege;

import java.util.Calendar;
import java.util.Iterator;
import java.util.Vector;

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

    public void passExamineBatch(HttpServletRequest request) throws
            ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids == null)
            return;
        int len = ids.length;
        Document doc = null;

        Privilege privilege = new Privilege();
        for (int i = 0; i < len; i++) {
            doc = getDocument(Integer.parseInt(ids[i]));
            LeafPriv lp = new LeafPriv(doc.getDirCode());
            if (lp.canUserExamine(privilege.getUser(request))) {
                doc.UpdateExamine(Document.EXAMINE_PASS);
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
            return re;
        } else {
            LeafPriv lp = new LeafPriv(dir_code);
            if (!lp.canUserAppend(privilege.getUser(request)))
                throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));
            boolean re = doc.create(application, mfu, privilege.getUser(request));
            document = doc;
            id = doc.getID();
            return re;
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
            	re = doc.del();
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

        //String attachmentBasePath = request.getContextPath() + "/";
        String attachmentBasePath = Global.getRealPath();

        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // 保存至磁盘相应路径
            String fname = FileUpload.getRandName() + "." +
                           fi.getExt();
            // 记录于数据库
            Attachment att = new Attachment();
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
