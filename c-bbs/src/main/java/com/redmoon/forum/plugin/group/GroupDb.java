package com.redmoon.forum.plugin.group;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ErrMsgException;
import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import com.redmoon.kit.util.FileUpload;
import java.io.IOException;
import cn.js.fan.util.ParamConfig;
import cn.js.fan.util.CheckErrException;
import java.util.Calendar;
import java.io.File;
import java.util.Vector;
import com.redmoon.forum.util.ForumFileUpload;
import cn.js.fan.web.Global;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.forum.util.ForumFileUtil;
import cn.js.fan.util.StrUtil;
import cn.js.fan.module.nav.LinkDb;
import java.util.Iterator;
import com.redmoon.forum.plugin.group.photo.PhotoDb;

/**
 * <p>Title: </p>
 *
 * <p>Description: </p>
 *
 * <p>Copyright: Copyright (c) 2007</p>
 *
 * <p>Company: </p>
 *
 * @author not attributable
 * @version 1.0
 */
public class GroupDb extends QObjectDb {
	static final long serialVersionUID = 11;
	
    public String logoBasePath = "forum/plugin/group/logo";

    public GroupDb() {
    }

    @Override
    public boolean create(JdbcTemplate jt, ParamChecker paramChecker) throws
            ResKeyException, ErrMsgException {
        int max = GroupConfig.getInstance().getIntProperty("max_group_create");
        if (getCreateCount(paramChecker.getString("creator"))>max) {
            throw new ErrMsgException("您创建的圈子超过了允许的最大数目" + max + "!");
        }
        boolean re = super.create(jt, paramChecker);
        if (re) {
            refreshList(getString("catalog_code"));

            if (re) {
                GroupUserDb gud = new GroupUserDb();
                gud.create(jt, new Object[] {new Long(getLong("id")),getString("creator"),new java.util.Date(),new Integer(1),new Integer(1),""});
            }
        }
        return re;
    }

    public String writeLogo(ForumFileUpload fu) {
        if (fu.getRet() == ForumFileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
                if (!fi.getFieldName().equals("logo")) {
                    fi = null;
                }
            }
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String pt = (cal.get(Calendar.YEAR)) + "/" + (cal.get(Calendar.MONTH) + 1);
                vpath = logoBasePath + "/" + pt + "/";
                String filepath = Global.getRealPath() + "upfile/" + vpath;
                // 置本地路径
                fu.setSavePath(filepath);
                // 置远程路径
                fu.setRemoteBasePath(vpath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return pt + "/" + fi.getDiskName();
            }
        }
        return "";
    }


    public String writeBanner(ForumFileUpload fu) {
        if (fu.getRet() == ForumFileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
                if (!fi.getFieldName().equals("banner")) {
                    fi = null;
                    if (v.size()==2) {
                        fi = (FileInfo)v.get(1);
                    }
                }
            }
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String pt = (cal.get(Calendar.YEAR)) + "/" + (cal.get(Calendar.MONTH) + 1);
                vpath = logoBasePath + "/" + pt + "/";
                String filepath = Global.getRealPath() + "upfile/" + vpath;
                // 置本地路径
                fu.setSavePath(filepath);
                // 置远程路径
                fu.setRemoteBasePath(vpath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return pt + "/" + fi.getDiskName();
            }
        }
        return "";
    }

    public boolean save(ServletContext application, HttpServletRequest request,
                        QObjectDb qObjectDb, String formCode) throws
            ErrMsgException {
        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        ForumFileUpload fu = new ForumFileUpload();
        fu.setValidExtname(new String[] {"jpg", "gif", "bmp", "png"});

        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        ParamConfig pc = new ParamConfig(qObjectDb.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fu);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        String logo = writeLogo(fu);
        String banner = writeBanner(fu);
        boolean re = false;
        try {
            if (!logo.equals("")) {
                delLogo();
                pck.setValue("logo", "logo", logo);
            }
            else {
                pck.setValue("logo", "logo", StrUtil.getNullStr(getString("logo")));
            }
            if (!banner.equals("")) {
                delBanner();
                pck.setValue("banner", "banner", banner);
            }
            else {
                pck.setValue("banner", "banner", StrUtil.getNullStr(getString("banner")));
            }
            JdbcTemplate jt = new JdbcTemplate();
            re = qObjectDb.save(jt, pck);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        // 删除相片
        PhotoDb pd = new PhotoDb();
        pd.delPhotoOfGroup(getLong("id"));

        // 删除活动
        GroupActivityDb gad = new GroupActivityDb();
        gad.delGroupActivityOfGroup(getLong("id"));

        // 删除贴子
        GroupThreadDb gtd = new GroupThreadDb();
        gtd.delThreadOfGroup(getLong("id"));

        // 删除链接
        LinkDb ld = new LinkDb();
        String sql = ld.getListSql(GroupUnit.code, "" + getLong("id"));
        Iterator ir = ld.list(sql).iterator();
        while (ir.hasNext()) {
            ld = (LinkDb)ir.next();
            ld.del(jt);
        }

        // 删除用户
        GroupUserDb gu = new GroupUserDb();
        gu.delUserOfGroup(getLong("id"));

        boolean re = super.del(jt);
        if (re) {
            delLogo();
        }
        return re;
    }

    public void delLogo() throws ResKeyException {
        // 删除icon
        String logo = StrUtil.getNullString(getString("logo"));
        if (!logo.equals("")) {
            com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                ForumFileUtil ffu = new ForumFileUtil();
                ffu.delFtpFile(logoBasePath + "/" + logo);
            } else {
                File f = new File(Global.realPath + "upfile/" + logoBasePath + "/" + logo);
                f.delete();
            }
        }
    }

    public void delBanner() throws ResKeyException {
        // 删除icon
        String banner = StrUtil.getNullString(getString("banner"));
        if (!banner.equals("")) {
            com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                ForumFileUtil ffu = new ForumFileUtil();
                ffu.delFtpFile(logoBasePath + "/" + banner);
            } else {
                File f = new File(Global.realPath + "upfile/" + logoBasePath + "/" + banner);
                f.delete();
            }
        }
    }

    public int getCreateCount(String userName) {
        String sql = getTable().getSql("listmine").replaceAll("\\?",
                StrUtil.sqlstr(userName));
        return list(sql).size();
    }

    public int getAttendCount(String userName) {
        String sql = getTable().getSql("listattend").replaceAll("\\?",
                StrUtil.sqlstr(userName));
        return list(sql).size();
    }

    public String getLogoUrl(HttpServletRequest request) {
        if (getString("logo")==null || getString("logo").equals(""))
            return "";
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String basePath = request.getContextPath() + "/upfile/" +
                                    logoBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            basePath = cfg.getRemoteBaseUrl();
            basePath += logoBasePath + "/";
        }
        return basePath + getString("logo");
    }

    public String getBannerUrl(HttpServletRequest request) {
        if (getString("banner")==null || getString("banner").equals(""))
            return "";
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String basePath = request.getContextPath() + "/upfile/" +
                                    logoBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            basePath = cfg.getRemoteBaseUrl();
            basePath += logoBasePath + "/";
        }
        return basePath + getString("banner");
    }
}
