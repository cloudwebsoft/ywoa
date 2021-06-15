package com.redmoon.forum.music;

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
import cn.js.fan.util.ParamUtil;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.Config;
import com.redmoon.forum.plugin.score.Gold;

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
public class MusicFileMgr {
    public FileUpload fileUpload;

    public MusicFileMgr() {
    }

    public MusicFileDb getMusicFileDb(long id) {
        MusicFileDb isfd = new MusicFileDb();
        return isfd.getMusicFileDb(id);
    }

    public void create() {

    }

    public FileUpload doUpload(ServletContext application,
                                           HttpServletRequest request) throws
            ErrMsgException {
        fileUpload = new FileUpload();
        fileUpload.setMaxFileSize(Global.FileSize); // 每个文件最大30000K 即近300M
        String[] ext = new String[]{"wmv", "wav", "mp3"};
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
        String dirCode = fileUpload.getFieldValue("dirCode");
        
        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(Calendar.YEAR));
        String month = "" + (cal.get(Calendar.MONTH) + 1);
        String visualPath = year + "/" + month;
        
        if (fileUpload.getRet() == FileUpload.RET_SUCCESS) {
            String isWebedit = StrUtil.getNullStr(fileUpload.getFieldValue("isWebedit"));
            if (isWebedit.equals("true")) {
                String tempAttachFilePath = Global.getRealPath() +
                                            "upfile/" +
                                            MusicFileDb.baseMusicPath + "/" +
                                            visualPath +
                                            "/";
                fileUpload.setSavePath(tempAttachFilePath); //取得目录
                File f = new File(tempAttachFilePath);
                if (!f.isDirectory()) {
                    f.mkdirs();
                }

                fileUpload.writeFile(true);
                
                Vector v = fileUpload.getFiles();
                if (v.size() == 0)
                    throw new ErrMsgException("请上传文件！");
                Iterator ir = v.iterator();
                while (ir.hasNext()) {
                    FileInfo fi = (FileInfo) ir.next();
                    fi.getSize();
                    try {
                        MusicFileDb isfd = new MusicFileDb();
                        isfd.setDirCode(dirCode);
                        isfd.setExt(fi.getExt());
                        isfd.setSize(fi.getSize());
                        isfd.setDiskName(fi.getDiskName());
                        isfd.setVisualPath(visualPath);
                        isfd.setName(fi.getName());
                        isfd.setLink(false);
                        isfd.create(new JdbcTemplate());
                    } catch (ResKeyException e) {
                        throw new ErrMsgException(e.getMessage(request));
                    }
                }
            }
            else {
                String name = StrUtil.getNullStr(fileUpload.getFieldValue("name")).trim();
                if (name.equals("")) {
                    throw new ErrMsgException("文件名称不能为空！");
                }            	
	            boolean isLink = StrUtil.getNullStr(fileUpload.getFieldValue(
	                    "isLink")).equals("true");
	            if (isLink) {
	                String url = StrUtil.getNullStr(fileUpload.getFieldValue("url")).
	                         trim();
	                if (url.equals("")) {
	                    throw new ErrMsgException("当文件为链接型时，链接地址不能为空！");
	                }
	
	                try {
	                    MusicFileDb isfd = new MusicFileDb();
	                    isfd.setDirCode(dirCode);
	                    isfd.setName(name);
	                    isfd.setLink(isLink);
	                    isfd.setUrl(url);
	                    isfd.create(new JdbcTemplate());
	                } catch (ResKeyException e) {
	                    throw new ErrMsgException(e.getMessage(request));
	                }
	            }
	            else {
	                String tempAttachFilePath = Global.getRealPath() + "upfile/" +
	                                            MusicFileDb.baseMusicPath + "/" +
	                                            visualPath +
	                                            "/";
	                fileUpload.setSavePath(tempAttachFilePath); //取得目录
	                File f = new File(tempAttachFilePath);
	                if (!f.isDirectory()) {
	                    f.mkdirs();
	                }
	
	                fileUpload.writeFile(true);
	
	                Vector v = fileUpload.getFiles();
	                if (v.size()==0)
	                    throw new ErrMsgException("当不为链接型时，需上传文件！");
	                Iterator ir = v.iterator();
	                while (ir.hasNext()) {
	                    FileInfo fi = (FileInfo) ir.next();
	                    fi.getSize();
	                    try {
	                        MusicFileDb isfd = new MusicFileDb();
	                        isfd.setDirCode(dirCode);
	                        isfd.setExt(fi.getExt());
	                        isfd.setSize(fi.getSize());
	                        isfd.setDiskName(fi.getDiskName());
	                        isfd.setVisualPath(visualPath);
	                        isfd.setName(name);
	                        isfd.setLink(isLink);
	                        isfd.create(new JdbcTemplate());
	                    } catch (ResKeyException e) {
	                        throw new ErrMsgException(e.getMessage(request));
	                    }
	                }
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
        MusicFileDb mfd = new MusicFileDb();
        mfd = getMusicFileDb(id);
        if (mfd == null || !mfd.isLoaded()) {
            throw new ErrMsgException("文件未找到！");
        }
        boolean re = false;
        re = mfd.del(new JdbcTemplate());
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
        String url = ParamUtil.get(request, "url");
        if (url.equals(""))
            throw new ErrMsgException("链接不能为空！");
        MusicFileDb doc = new MusicFileDb();
        doc = getMusicFileDb(id);
        if (doc == null || !doc.isLoaded()) {
            throw new ErrMsgException("文件未找到！");
        }
        boolean re = false;
        doc.setName(name);
        doc.setUrl(url);
        re = doc.save(new JdbcTemplate());
        return re;
    }
}
