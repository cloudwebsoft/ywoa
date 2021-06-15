package com.redmoon.oa.idiofileark;

/**
 * <p>Title: </p>
 * <p>Description: </p>
 * <p>Copyright: Copyright (c) 2003</p>
 * <p>Company: </p>
 * @author bluewind
 * @version 1.0
 */
import java.io.*;
import java.util.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.oa.message.*;


public class IdiofilearkMgr {
    // public: connection parameters
    boolean debug = true;
    Privilege privilege;
    IdiofilearkDb ifd = new IdiofilearkDb();

    public IdiofilearkMgr() {
        privilege = new Privilege();
    }

    /**
     * 保存文件至自定义目录
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean addDoc(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean re = false;
        if (pvg.isUserLogin(request)) {
            re = ifd.addDoc(application, request, pvg.getUser(request));
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        return re;
    }

    public boolean modifyDoc(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        boolean re = false;
        if (pvg.isUserLogin(request)) {
            re = ifd.modifyDoc(application, request);
        } else
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        return re;
    }

    public boolean delDocs(HttpServletRequest request) throws ErrMsgException {
        com.redmoon.oa.pvg.Privilege pvg = new com.redmoon.oa.pvg.Privilege();
        if (!pvg.isUserLogin(request))
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        String[] ids = request.getParameterValues("ids");
        if (ids == null)
            throw new ErrMsgException(SkinUtil.LoadString(request, "err_id"));
        // 判斷ids中參數是否合法，防註入
        int len = ids.length;
        for (int i=0; i<len; i++) {
        	com.redmoon.oa.security.SecurityUtil.antiSQLInject(request, pvg, "ids", ids[i], getClass().getName());
        }
        if (!privilege.canManage(request, ids))
            throw new ErrMsgException(SkinUtil.LoadString(request, "pvg_invalid"));

        return ifd.del(ids);
    }

    public IdiofilearkDb getIdiofilearkDb(int id) throws ErrMsgException {
        return (IdiofilearkDb)ifd.getIdiofilearkDb(id);
    }

    public boolean delIdioAttachment(IdioAttachment att) throws ErrMsgException {
        int id = att.getMsgId();
        if (att.del()) {
            IdiofilearkDb md = getIdiofilearkDb(id);
            IdiofilearkCache uc = new IdiofilearkCache(md);
            uc.refreshSave(md.getPrimaryKey());
            return true;
        }
        else
            return false;
    }

    public IdiofilearkDb getIdiofilearkDb() {
        return ifd;
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

        FileUpload fu = new FileUpload();
        String[] ext = new String[] {"jpg", "gif", "png", "bmp", "swf"};
        if (ext!=null)
            fu.setValidExtname(ext);

        int ret = 0;
        try {
            ret = fu.doUpload(application, request);
            if (ret!=FileUpload.RET_SUCCESS) {
                throw new ErrMsgException(fu.getErrMessage(request));
            }
            if (fu.getFiles().size()==0)
                throw new ErrMsgException("请上传文件！");
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
            throw new ErrMsgException(e.getMessage());
        }

        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(Calendar.YEAR));
        String month = "" + (cal.get(Calendar.MONTH) + 1);
        String virtualpath = year + "/" + month;

        com.redmoon.oa.Config cfg = new com.redmoon.oa.Config();
        String attPath = cfg.get("file_message");

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

        //String IdioAttachmentBasePath = request.getContextPath() + "/";
        String IdioAttachmentBasePath = Global.getRealPath();

        if (ir.hasNext()) {
            FileInfo fi = (FileInfo) ir.next();
            // 保存至磁盘相应路径
            String fname = FileUpload.getRandName() + "." +
                           fi.getExt();
            // 记录于数据库
            IdioAttachment att = new IdioAttachment();
            att.setDiskName(fi.getDiskName());
            // logger.info(fpath);
            att.setMsgId(IdioAttachment.TEMP_MSG_ID);
            att.setName(fi.getName());
            att.setDiskName(fname);
            att.setOrders(orders);
            att.setVisualPath(attPath + "/" + virtualpath);
            att.setUploadDate(new java.util.Date());
            if (att.create()) {
                fi.write(filepath, fname);
                re = new String[2];
                re[0] = "" + att.getId();
                re[1] = IdioAttachmentBasePath + att.getVisualPath() + "/" + att.getDiskName();
            }
        }

        return re;
    }

    /**
     * 将短消息中的文件转存至个人文件柜中
     * @param msg IdiofilearkDb
     * @param transmitDirCode String
     * @return boolean
     */
    public boolean TransmitMsgToidiofileark(HttpServletRequest request) throws
            ErrMsgException {
        IdiofilearkDb md = new IdiofilearkDb();
        MessageMgr msgr = new MessageMgr();
        String[] tmpAttachIds = request.getParameterValues("ids");
        String dirCode = ParamUtil.get(request, "dir_code");
        int len = 0,ids;
        if (tmpAttachIds!=null)
        	len = tmpAttachIds.length;
        if (tmpAttachIds != null && tmpAttachIds.length > 0) {
            for (int i = 0; i < len; i++) {
                ids = Integer.parseInt(tmpAttachIds[i]);
                MessageDb msg = msgr.getMessageDb(ids);
                md.TransmitMsgToidiofileark(msg, dirCode);
                msg.del();
            }
        }

        return true;
    }


}
