package cn.js.fan.module.cms.site;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.util.StrUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import java.io.IOException;
import cn.js.fan.web.Global;
import java.util.Calendar;
import cn.js.fan.util.FTPUtil;
import java.io.File;
import cn.js.fan.util.file.FileUtil;
import cn.js.fan.module.cms.robot.RobotUtil;
import cn.js.fan.util.ParamUtil;
import java.util.Vector;
import java.sql.SQLException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ErrMsgException;

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
public class SiteDb extends QObjectDb {
    public static final String cssBasePath = "cms/site/css";

    public static final int STATUS_OPEN = 1;
    public static final int STATUS_CLOSE = 0;
    public static final int STATUS_FORBID = 2; // 强制关闭
    public static final int STATUS_NOT_CHECKED = 3; // 尚未审核通过

    // @task:doc_hot字段暂时无用

    public SiteDb() {
    }

    public String getVisualGuestbookCode() {
        return "cms_site_" + getString("code");
    }

    public boolean init(String code, String name, String owner, String kind) {
        boolean re = false;
        try {
            SiteTemplateDb std = new SiteTemplateDb();
            std = std.getDefaultSiteTemplateDb();
            re = create(new JdbcTemplate(), new Object[] {
                code, new Integer(0), new Integer(std.getInt("id")), name, owner, kind, new java.util.Date(), new Integer(1), new Integer(STATUS_OPEN)
            });
        } catch (ResKeyException e) {
            // LogUtil.getLog(getClass()).error("create:" + e.getMessage());
            throw new IllegalAccessError(e.getMessage());
        }
        return re;
    }

    public SiteDb getSiteDb(String code) {
        return (SiteDb)getQObjectDb(code);
        /*
        SiteDb sd = (SiteDb)getQObjectDb(code);
        if (sd==null) {
            init(code);
            sd = (SiteDb)getQObjectDb(code);
        }

        return sd;
        */
    }

    /**
     * 取得用户拥有的子站点
     * @return Vector
     */
    public Vector getSubsitesOfUser(String userName) {
        String sql = "select code from " + getTable().getName() + " where owner=?";
        return list(sql, new Object[] {userName});
    }


    /**
     * 取得用户拥有的及被赋予为管理员的站点
     * @param userName
     * @return
     */
    public Vector getSiteDbsManageredByUser(String userName) {
    	Vector vOwner = getSubsitesOfUser(userName);

    	SiteManagerDb smd = new SiteManagerDb();

    	Vector vManager = smd.getSiteDbsManageredByUser(userName);

    	// 去除重复的部分
    	Vector v = new Vector();
    	int olen = vOwner.size();
    	int mlen = vManager.size();

    	for (int i=0; i<mlen; i++) {
    		boolean isFound = false;
    		SiteDb m = (SiteDb)vManager.elementAt(i);
    		// LogUtil.getLog(getClass()).info("m=" + m + " i=" + i);
    		for (int j=0; j<olen; j++) {
    			SiteDb o = (SiteDb)vOwner.elementAt(j);
        		// LogUtil.getLog(getClass()).info("o=" + o + " j=" + j);
    			if (m.getString("code").equals(o.getString("code"))) {
    				isFound = true;
    				break;
    			}
    		}
    		if (!isFound) {
    			v.addElement(m);
    		}
    	}
    	v.addAll(vOwner);
    	return v;
    }

    public String getCSS(HttpServletRequest request) {
        if (!getBoolean("is_user_css")) {
            SiteTemplateDb td = new SiteTemplateDb();
            td = td.getSiteTemplateDb(getInt("skin"));
            if (td==null)
                return "";
            else
                return request.getContextPath() + "/skin/" + td.getString("code") + "/skin.css";
        }
        else {
            // 如果样式表不存在，则创建样式表文件
            return getUserDefinedCSSUrl(request);
        }
    }

    public boolean save(JdbcTemplate jt, Object[] params) throws SQLException {
        refreshList();
        return super.save(jt, params);
    }

    public String getCSSContent(HttpServletRequest request) {
        chkAndCreateCSSFile(request);

        String cssPath = StrUtil.getNullStr(getString("css_path"));

        String tmpRemoteFilePath = "";
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            String basePath = cfg.getProperty("forum.ftpUrl");

            // 从远程下载图像至本地
            String remoteFileName = RobotUtil.gatherFile(basePath + "/" +
                    cssBasePath + "/" +
                    cssPath, "utf-8", Global.realPath +
                    "upfile" + "/");

            if (!remoteFileName.equals("")) {
                try {
                    tmpRemoteFilePath = Global.realPath +
                                        "upfile" + "/" +
                                        remoteFileName;
                    String s = FileUtil.ReadFile(tmpRemoteFilePath);
                    return s;
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("getCSSContent1:" +
                                 StrUtil.trace(e));
                }
                finally {
                    File f = new File(tmpRemoteFilePath);
                    f.delete();
                }
            }
        }
        else {
            try {
                String p = Global.realPath + "upfile/" + cssBasePath + "/" + cssPath;
                return FileUtil.ReadFile(p);
            }
            catch (IOException e) {
                LogUtil.getLog(getClass()).error("getCSSContent2:" + e.getMessage());
            }
        }
        return "";
    }

    public String getUserDefinedCSSUrl(HttpServletRequest request) {
        chkAndCreateCSSFile(request);
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String basePath = request.getContextPath() + "/upfile/" +
                                    cssBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            basePath = cfg.getRemoteBaseUrl();
            basePath += cssBasePath + "/";
        }
        return basePath + getString("css_path");
    }

    /**
     * 检查用户自定义CSS是否已存在，如果不存在，则创建
     */
    public void chkAndCreateCSSFile(HttpServletRequest request) {
        // 不为空表示已存在
        if (!StrUtil.getNullStr(getString("css_path")).equals(""))
            return;
        String p = createDefaultCSSFile(request);
        if (!p.equals("")) {
            set("css_path", p);
            try {
                save();
            }
            catch (ResKeyException e) {
                LogUtil.getLog(getClass()).error("chkAndCreateCSSFile:" + e.getMessage(request));
            }
        }
    }

    /**
     * 创建初始默认模板，根据用户当前选择的皮肤
     * @param request HttpServletRequest
     * @return boolean
     */
    public String createDefaultCSSFile(HttpServletRequest request) {
        String path = cssBasePath;
        Calendar cal = Calendar.getInstance();
        String year = "" + (cal.get(cal.YEAR));
        String month = "" + (cal.get(cal.MONTH) + 1);

        String cssPath = StrUtil.getNullStr(getString("css_path"));
        if (cssPath.equals(""))
            path += "/" + year + "/" + month + "/" + getString("code") + ".css";
        else
            path += "/" + cssPath;

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        // 建立tmp文件，过滤样本CSS中的路径
        FileUtil fu = new FileUtil();
        String tmpFileRealPath = Global.realPath + "upfile/tmp_cms_site_css_" + getString("code") + ".css";
        SiteTemplateDb td = new SiteTemplateDb();
        td = td.getSiteTemplateDb(getInt("skin"));
        try {
            String css = "";
            css = fu.ReadFile(Global.realPath + "skin/" + td.getString("code") + "/skin.css");
            css = css.replaceAll("images/", request.getContextPath() + "/skin/" + td.getString("code") + "/images/");
            fu.WriteFileUTF8(tmpFileRealPath, css);
        }
        catch (IOException e) {
            LogUtil.getLog(getClass()).error("chkAndCreateCSSFile:" + e.getMessage());
        }

        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            try {
                boolean retFtp = ftp.connect(cfg.getProperty(
                        "forum.ftpServer"),
                                             cfg.getIntProperty("forum.ftpPort"),
                                             cfg.getProperty("forum.ftpUser"),
                                             cfg.getProperty("forum.ftpPwd"), true);
                if (!retFtp) {
                    LogUtil.getLog(getClass()).error("writeFile:" +
                            ftp.getReplyMessage());
                } else {
                    try {
                        ftp.storeFile(path,
                                      tmpFileRealPath);
                    } catch (IOException e1) {
                        LogUtil.getLog(getClass()).error(
                                "AddNewWE: storeFile - " +
                                e1.getMessage());
                    }
                }
            } finally {
                ftp.close();
            }
        } else {
            String fullPath = Global.realPath + "upfile/" + path;
            // 检查目录是否已存在
            int p = fullPath.lastIndexOf("/");
            String pa = fullPath.substring(0, p);
            // System.out.println(getClass() + " pa=" + pa);
            File f = new File(pa);
            if (!f.isDirectory())
                f.mkdirs();
            fu.CopyFile(tmpFileRealPath,
                        fullPath);
            // System.out.println(getClass() + " " + Global.realPath + "upfile/" + path);
        }
        // 删除临时文件
        File f = new File(tmpFileRealPath);
        f.delete();
        return year + "/" + month + "/" + getString("code") + ".css";
    }

    /**
     * 修改CSS文件
     * @param request
     */
    public void modifyCSSFile(HttpServletRequest request) {
        String content = ParamUtil.get(request, "content");
        content = cn.js.fan.security.AntiXSS.antiXSS(content);

        String basePath = cssBasePath;

        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");

        String cssPath = StrUtil.getNullStr(getString("css_path"));

        FTPUtil ftp = new FTPUtil();
        if (isFtpUsed) {
            try {
                // 建立tmp文件
                FileUtil fu = new FileUtil();
                String tmpFileRealPath = Global.realPath + "upfile/tmp_cms_site_" + getString("code") +
                                         ".css";
                fu.WriteFileUTF8(tmpFileRealPath, content);

                boolean retFtp = ftp.connect(cfg.getProperty(
                        "forum.ftpServer"),
                                             cfg.getIntProperty("forum.ftpPort"),
                                             cfg.getProperty("forum.ftpUser"),
                                             cfg.getProperty("forum.ftpPwd"), true);
                if (!retFtp) {
                    LogUtil.getLog(getClass()).error("writeFile:" +
                            ftp.getReplyMessage());
                } else {
                    try {
                        ftp.storeFile(basePath + "/" + cssPath,
                                      tmpFileRealPath);
                    } catch (IOException e1) {
                        LogUtil.getLog(getClass()).error(
                                "modifyCSSFile: storeFile - " +
                                e1.getMessage());
                    }
                }
                // 删除临时文件
                File f = new File(tmpFileRealPath);
                f.delete();
            } finally {
                ftp.close();
            }
        } else {
            FileUtil.WriteFileUTF8(Global.realPath + "upfile/" + basePath + "/" + cssPath, content);
        }
    }

}
