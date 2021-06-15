package com.redmoon.blog;

import java.io.*;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.forum.Config;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.forum.plugin.ScoreMgr;
import com.redmoon.forum.plugin.base.IPluginScore;
import com.redmoon.forum.plugin.ScoreUnit;
import com.redmoon.forum.plugin.score.Gold;
import com.redmoon.forum.person.UserDb;

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
public class VideoMgr {
    public VideoMgr() {
    }

    public boolean create(ServletContext application,
                          HttpServletRequest request) throws
            ErrMsgException {
        if (!Privilege.isUserLogin(request)) {
             throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        }

        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        ForumFileUpload fu = new ForumFileUpload();
        fu.setValidExtname(new String[] {"wmv", "mpg", "rmvb", "rm", "avi"});
        UserDb user = new UserDb();
        user = user.getUser(Privilege.getUser(request));
        fu.setMaxFileSize((int) ((Privilege.getDiskSpaceAllowed(user) -
                                  user.getDiskSpaceUsed()) / 1024));

        int ret = -1;
        try {
            ret = fu.doUpload(application, request, "utf-8");
        } catch (IOException e) {
            if (ret==FileUpload.RET_TOOLARGESINGLE) {
                throw new ErrMsgException(fu.getErrMessage(request) + "，您的剩余磁盘空间为：" + (Privilege.getDiskSpaceAllowed(user)-user.getDiskSpaceUsed())/1024 + " K");
            }
            else
                throw new ErrMsgException(e.getMessage());
        }
        if (ret!=FileUpload.RET_SUCCESS) {
            throw new ErrMsgException(fu.getErrMessage(request));
        }

        boolean re = false;

        VideoDb mud = new VideoDb();
        String formCode = "blog_video_create";
        ParamConfig pc = new ParamConfig(mud.getTable().
                                         getFormValidatorFile());
        ParamChecker pck = new ParamChecker(request, fu);
        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        // 检查权限
        if (!Privilege.canUserDo(request, pck.getLong("blog_id"), Privilege.PRIV_ALL)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        }
        String link = StrUtil.getNullStr(pck.getString("link"));

        String music = "";
        // System.out.println(getClass() + " link=" + link + " music=" + music);
        if (link.equals("")) {
            music = mud.writeVideo(request, fu);
            if (fu.getFiles().size()>0) {
                pck.setValue("file_size", "file_size", new Long(((FileInfo)fu.getFiles().elementAt(0)).getSize()));
            }
            else {
                pck.setValue("file_size", "file_size", new Long(0));
            }
        }
        else {
            pck.setValue("file_size", "file_size", new Long(0));
        }

        pck.setValue("video", "video",  music);
        Config cfg = Config.getInstance();
        boolean isRemote = cfg.getBooleanProperty("forum.ftpUsed");
        pck.setValue("is_remote", "is_remote", new Integer(isRemote?1:0));
        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = mud.create(jt, pck);
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean save(ServletContext application, HttpServletRequest request
                        ) throws
            ErrMsgException {
        if (!Privilege.isUserLogin(request)) {
             throw new ErrMsgException(SkinUtil.LoadString(request, "err_not_login"));
        }

        String contentType = request.getContentType();
        if (contentType.indexOf("multipart/form-data") == -1) {
            throw new IllegalStateException(
                    "The content type of request is not multipart/form-data");
        }

        ForumFileUpload fu = new ForumFileUpload();
        fu.setValidExtname(new String[] {"wmv", "mpg", "rmvb", "rm", "avi"});
        UserDb user = new UserDb();
        user = user.getUser(Privilege.getUser(request));
        fu.setMaxFileSize((int) ((Privilege.getDiskSpaceAllowed(user) -
                                  user.getDiskSpaceUsed()) / 1024)); // 以K为单位
        int ret = -1;
        try {
            ret = fu.doUpload(application, request);
        } catch (IOException e) {
            throw new ErrMsgException(e.getMessage());
        }

        if (ret!=FileUpload.RET_SUCCESS) {
            if (ret==FileUpload.RET_TOOLARGESINGLE) {
                throw new ErrMsgException(fu.getErrMessage(request) + "，您的剩余磁盘空间为：" + (Privilege.getDiskSpaceAllowed(user)-user.getDiskSpaceUsed())/1024 + " K");
            }
            else
                throw new ErrMsgException(fu.getErrMessage(request));
        }

        VideoDb mud = new VideoDb();
        String formCode = "blog_video_save";

        ParamConfig pc = new ParamConfig(mud.getTable().
                                         getFormValidatorFile()); // "form_rule.xml");
        ParamChecker pck = new ParamChecker(request, fu);

        try {
            pck.doCheck(pc.getFormRule(formCode)); // "regist"));
        } catch (CheckErrException e) {
            // 如果onError=exit，则会抛出异常
            throw new ErrMsgException(e.getMessage());
        }

        // 检查权限
        if (!Privilege.canUserDo(request, pck.getLong("blog_id"), Privilege.PRIV_ALL)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        }

        boolean re = false;

        long id = pck.getLong("id");
        mud = (VideoDb)mud.getQObjectDb(new Long(id));
        int oldSort = mud.getInt("sort");

        if (fu.getFiles().size()>0) {
            // 删除原来的video
            mud.delVideo();

            String video = mud.writeVideo(request, fu);
            pck.setValue("video", "video",  video);
            pck.setValue("file_size", "file_size", new Long(((FileInfo)fu.getFiles().elementAt(0)).getSize()));
        }
        else {
            pck.setValue("video", "video",  mud.getString("video"));
            pck.setValue("file_size", "file_size", new Long(0));
        }

        try {
            JdbcTemplate jt = new JdbcTemplate();
            re = mud.save(jt, pck);
            // 刷新列表缓存
            if (oldSort!=pck.getInt("sort"))
                mud.refreshList("" + mud.getLong("blog_id"));
        } catch (ResKeyException e) {
            throw new ErrMsgException(e.getMessage(request));
        }
        return re;
    }

    public boolean del(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        long id = ParamUtil.getLong(request, "id");
        long blogId = ParamUtil.getLong(request, "blogId");
        // 检查权限
        if (!Privilege.canUserDo(request, blogId, Privilege.PRIV_ALL)) {
            throw new ErrMsgException(SkinUtil.LoadString(request, SkinUtil.PVG_INVALID));
        }

        VideoDb mud = new VideoDb();
        mud = (VideoDb)mud.getQObjectDb(new Long(id));
        return mud.del(new JdbcTemplate());
    }

    public boolean dig(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        long videoId = ParamUtil.getLong(request, "videoId");
        VideoDb mud = new VideoDb();
        mud = (VideoDb)mud.getQObjectDb(new Long(videoId));
        long blogId = mud.getLong("blog_id");
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        if (!ucd.isVideoDig()) {
            throw new ErrMsgException("该博客视频不允许被评分！");
        }

        String userName = Privilege.getUser(request);

        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(Gold.code);

        com.redmoon.blog.Config cfg = com.redmoon.blog.Config.getInstance();
        int pay = cfg.getIntProperty("digVideoCost");

        int digScoreMax = cfg.getIntProperty("digVideoScoreMax");
        int digValue = ParamUtil.getInt(request, "digValue");

        if (digValue>digScoreMax) {
            throw new ErrMsgException("视频评分一次加分最高值不能超过" + digScoreMax);
        }
        boolean re = su.getScore().pay(userName, IPluginScore.SELLER_SYSTEM, pay);
        if (re) {
            // System.out.println(getClass() + " reward=" + reward + " md.getScore()=" + md.getScore());
            mud.set("score",new Integer(mud.getInt("score") + digValue));
            re = mud.save();
        }
        return re;
    }

    public boolean undig(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        long videoId = ParamUtil.getLong(request, "videoId");

        VideoDb vd = new VideoDb();
        vd = (VideoDb)vd.getQObjectDb(new Long(videoId));
        long blogId = vd.getLong("blog_id");
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        if (!ucd.isVideoDig()) {
            throw new ErrMsgException("该博客视频不允许被评分！");
        }

        String userName = Privilege.getUser(request);

        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(Gold.code);

        com.redmoon.blog.Config cfg = com.redmoon.blog.Config.getInstance();
        int pay = cfg.getIntProperty("undigVideoCost");

        int digScoreMax = cfg.getIntProperty("undigVideoScoreMax");
        int digValue = ParamUtil.getInt(request, "digValue");

        if (digValue>digScoreMax) {
            throw new ErrMsgException("视频评分一次扣分最高值不能超过" + digScoreMax);
        }
        boolean re = su.getScore().pay(userName, IPluginScore.SELLER_SYSTEM, pay);
        if (re) {
            // System.out.println(getClass() + " reward=" + reward + " md.getScore()=" + md.getScore());
            vd.set("score",new Integer(vd.getInt("score") - digValue));
            re = vd.save();
        }
        return re;
    }

}
