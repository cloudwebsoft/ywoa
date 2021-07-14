package cn.js.fan.module.cms;

import cn.js.fan.util.ErrMsgException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import cn.js.fan.web.Global;
import javax.servlet.ServletContext;
import com.redmoon.kit.util.FileUpload;
import com.cloudwebsoft.framework.util.LogUtil;
import java.util.Vector;
import java.util.Iterator;
import com.redmoon.kit.util.FileInfo;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.StrUtil;
import java.io.File;
import java.util.Calendar;
import cn.js.fan.module.pvg.Privilege;
import cn.js.fan.util.ParamUtil;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2005</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class ImgStoreFileMgr {
    public FileUpload fileUpload;

    public ImgStoreFileMgr() {
    }

    public ImgStoreFileDb getImgStoreFileDb(long id) {
        ImgStoreFileDb isfd = new ImgStoreFileDb();
        return isfd.getImgStoreFileDb(id);
    }

    public void create() {

    }

    public FileUpload doUpload(ServletContext application,
                                           HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        Config cfg = new Config();
        String ext_img = cfg.getProperty("cms.ext_img");
        String[] ext = StrUtil.split(ext_img, ",");
        fileUpload.setValidExtname(ext);
        int ret = 0;
        try {
            ret = fileUpload.doUpload(application, request);
            if (ret == -3) {
                throw new ErrMsgException(fileUpload.getErrMessage(request));
            }
            if (ret == -4) {
                throw new ErrMsgException(fileUpload.getErrMessage(request));
            }
        } catch (IOException e) {
            LogUtil.getLog(getClass()).error("doUpload:" + e.getMessage());
        }
        return fileUpload;
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws ErrMsgException {
        doUpload(application, request);
        if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
            Calendar cal = Calendar.getInstance();
            String year = "" + (cal.get(cal.YEAR));
            String month = "" + (cal.get(cal.MONTH) + 1);
            cn.js.fan.module.cms.Config cfg = new cn.js.fan.module.cms.Config();
            String visualPath = cfg.getProperty("cms.file_img") + "/" + year + "/" + month;
            String tempAttachFilePath = Global.getRealPath() + visualPath +
                                        "/";
            fileUpload.setSavePath(tempAttachFilePath); //取得目录
            File f = new File(tempAttachFilePath);
            if (!f.isDirectory()) {
                f.mkdirs();
            }

            fileUpload.writeFile(true);

            String dirCode = fileUpload.getFieldValue("dirCode");
            Vector v = fileUpload.getFiles();
            Iterator ir = v.iterator();
            while (ir.hasNext()) {
                FileInfo fi = (FileInfo)ir.next();
                fi.getSize();
                try {
                    ImgStoreFileDb isfd = new ImgStoreFileDb();
                    isfd.setDirCode(dirCode);
                    isfd.setExt(fi.getExt());
                    isfd.setSize(fi.getSize());
                    isfd.setDiskName(fi.getDiskName());
                    isfd.setVisualPath(visualPath);
                    isfd.setName(fi.getName());
                    isfd.create(new JdbcTemplate());
                }
                catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
            }
            return true;
        }
        else
            return false;
    }

    /**
     * 将文件彻底删除或者删至回收站
     * @param request HttpServletRequest
     * @param id int
     * @param privilege IPrivilege
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean del(HttpServletRequest request, long id) throws
            ErrMsgException {
        ImgStoreFileDb doc = new ImgStoreFileDb();
        doc = getImgStoreFileDb(id);
        if (doc == null || !doc.isLoaded()) {
            throw new ErrMsgException("文件未找到！");
        }
        boolean re = false;
        re = doc.del(new JdbcTemplate());
        return re;
    }

    public boolean delBatch(HttpServletRequest request) throws ErrMsgException {
        String strids = ParamUtil.get(request, "ids");
        String[] ids = StrUtil.split(strids, ",");
        if (ids==null)
            return false;
        int len = ids.length;
        boolean re = false;
        for (int i=0; i<len; i++) {
            re = del(request, Long.parseLong(ids[i]));
        }
        return re;
    }

    public boolean rename(HttpServletRequest request) throws
            ErrMsgException {
        long id = ParamUtil.getLong(request, "id");
        String name = ParamUtil.get(request, "name");
        if (name.equals(""))
            throw new ErrMsgException("名称不能为空！");
        ImgStoreFileDb doc = new ImgStoreFileDb();
        doc = getImgStoreFileDb(id);
        if (doc == null || !doc.isLoaded()) {
            throw new ErrMsgException("文件未找到！");
        }
        boolean re = false;
        doc.setName(name);
        re = doc.save(new JdbcTemplate());
        return re;
    }
}
