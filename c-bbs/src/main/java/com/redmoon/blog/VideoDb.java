package com.redmoon.blog;

import com.cloudwebsoft.framework.base.QObjectDb;
import com.redmoon.kit.util.FileInfo;
import java.util.Calendar;
import com.redmoon.forum.util.ForumFileUpload;
import java.util.Vector;
import cn.js.fan.web.Global;
import javax.servlet.http.HttpServletRequest;
import java.io.File;
import com.redmoon.forum.Config;
import com.redmoon.forum.util.ForumFileUtil;
import com.cloudwebsoft.framework.util.LogUtil;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import cn.js.fan.util.ResKeyException;
import cn.js.fan.util.ParamChecker;
import cn.js.fan.util.ErrMsgException;
import com.redmoon.forum.person.UserDb;
import com.redmoon.forum.Privilege;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.StrUtil;
import java.util.Iterator;

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
public class VideoDb extends QObjectDb {
    public static final String videoBasePath = "blog/video";

    public VideoDb() {
    }

    public boolean create(JdbcTemplate jt, ParamChecker paramChecker) throws
            ResKeyException, ErrMsgException {
        boolean re = super.create(jt, paramChecker);
        if (re) {
            refreshList("" + getLong("blog_id"));
        }
        return re;
    }

    public String getVideoUrl(HttpServletRequest request) {
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String attachmentBasePath = request.getContextPath() + "/upfile/" +
                                    videoBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (getInt("is_remote")==1 && isFtpUsed) {
            attachmentBasePath = cfg.getRemoteBaseUrl();
            attachmentBasePath += videoBasePath + "/";
        }
        return attachmentBasePath + getString("video");
    }

    public String writeVideo(HttpServletRequest request, ForumFileUpload fu) throws ErrMsgException {
        // 检查磁盘空间是否允许写Attachment
        /*
        Vector v = fu.getFiles();
        int size = v.size();
        Iterator ir = v.iterator();
        long fileSize = 0;
        while (ir.hasNext()) {
            FileInfo fi = (FileInfo)ir.next();
            fileSize += fi.size;
        }
        Privilege privilege = new Privilege();
        if (!privilege.canUploadAttachment(request, fileSize))
            throw new ErrMsgException(SkinUtil.LoadString(request, "res.forum.MsgDb", "err_space_full"));
        */
        if (fu.getRet() == fu.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0)
                fi = (FileInfo) v.get(0);
            String vpath = "";
            if (fi != null) {
                // 置保存路径
                Calendar cal = Calendar.getInstance();
                String year = "" + (cal.get(cal.YEAR));
                String month = "" + (cal.get(cal.MONTH) + 1);
                vpath = videoBasePath + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + "upfile/" + vpath;
                // 置本地路径
                fu.setSavePath(filepath);
                // 置远程路径
                fu.setRemoteBasePath(vpath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);

                // 更新用户的磁盘已用空间
                // 更新博主（团队博客的创建者）的磁盘已用空间
                long blogId = StrUtil.toLong(fu.getFieldValue("blog_id"));
                UserConfigDb ucd = new UserConfigDb();
                ucd = ucd.getUserConfigDb(blogId);
                UserDb ud = new UserDb();
                ud = ud.getUser(ucd.getUserName());
                ud.setDiskSpaceUsed(ud.getDiskSpaceUsed() + fi.getSize());
                ud.save();

                return year + "/" + month + "/" + fi.getDiskName();
            }
        }
        return "";
    }

    public boolean del(JdbcTemplate jt) throws ResKeyException {
        boolean re = false;
        re = super.del(jt);
        if (re) {
            delVideo();
            refreshList("" + getLong("blog_id"));
        }
        return re;
    }

    public void delVideo() {
        String video = getString("video");
        if (video != null && !video.equals("")) {
            Config cfg = Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                if (getInt("is_remote")==1) {
                    ForumFileUtil ffu = new ForumFileUtil();
                    ffu.delFtpFile(videoBasePath + "/" + video);
                }
            } else {
                try {
                    File file = new File(Global.realPath + "upfile/" + videoBasePath + "/" + video);
                    file.delete();
                } catch (Exception e) {
                    LogUtil.getLog(getClass()).error("delVideo:" + e.getMessage());
                }
            }

            // 将磁盘空间返还
            UserConfigDb ucd = new UserConfigDb();
            ucd = ucd.getUserConfigDb(getLong("blog_id"));
            UserDb ud = new UserDb();
            ud = ud.getUser(ucd.getUserName());
            long u = ud.getDiskSpaceUsed() - getLong("file_size");
            // logger.info("del:u=" + u);
            if (u < 0)
                u = 0;
            ud.setDiskSpaceUsed(u);
            ud.save();
        }
    }
}
