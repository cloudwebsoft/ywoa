package com.redmoon.blog.photo;

import javax.servlet.*;
import javax.servlet.http.*;

import cn.js.fan.cache.jcs.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.blog.*;
import com.redmoon.blog.Config;
import com.redmoon.forum.Privilege;
import com.redmoon.forum.plugin.*;
import com.redmoon.forum.plugin.base.*;
import com.redmoon.forum.plugin.score.*;

import org.apache.log4j.*;
import java.util.Iterator;
import com.cloudwebsoft.framework.db.JdbcTemplate;

public class PhotoMgr {
    Logger logger = Logger.getLogger(PhotoMgr.class.getName());
    RMCache rmCache;

    public PhotoMgr() {
        rmCache = RMCache.getInstance();
    }

    public boolean add(ServletContext application, HttpServletRequest request) throws
            ErrMsgException {
        PhotoForm pf = new PhotoForm();
        pf.checkAdd(application, request);
        blogId = pf.getBlogId();

        PhotoDb pd = new PhotoDb();
        pd.setTitle(pf.getTitle());
        pd.setBlogId(pf.getBlogId());
        pd.setDirCode(pf.getDirCode());
        pd.setLocked(pf.isLocked());
        pd.setSort(pf.getSort());
        pd.setCatalog(pf.getCatalog());
        boolean re = pd.create(pf.fileUpload);
        if (re) {
            if (pf.getCatalog()!=0) {
                try {
                    PhotoCatalogDb pcd = new PhotoCatalogDb();
					pcd = (PhotoCatalogDb) pcd.getQObjectDb(new Long(pf.getCatalog()));
					pcd.set("photo_count", new Integer(pcd.getInt("photo_count") + 1));
					// System.out.println(getClass() + "
					// pcd.getInt(\"photo_count\")=" +
					// pcd.getInt("photo_count"));
					pcd.set("miniature", pd.getImageSmall());
					pcd.save();
                }
                catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
            }

        }
        return re;
    }

    /**
     * 删除照片
     * @param application ServletContext
     * @param request HttpServletRequest
     * @return boolean
     * @throws ErrMsgException
     */
    public boolean del(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        PhotoForm lf = new PhotoForm();
        lf.checkDel(request);

        PhotoDb ld = new PhotoDb();
        ld = ld.getPhotoDb(lf.getId());
        boolean re = ld.del(Global.getRealPath());
        if (re) {
            if (ld.getCatalog()!=0) {
                try {
                    PhotoCatalogDb pcd = new PhotoCatalogDb();
                    pcd = (PhotoCatalogDb) pcd.getQObjectDb(new Long(ld.
                            getCatalog()));
                    if (pcd!=null) {
                        pcd.set("photo_count",
                                new Integer(pcd.getInt("photo_count") - 1));
                        pcd.save();
                    }
                }
                catch (ResKeyException e) {
                    throw new ErrMsgException(e.getMessage(request));
                }
            }

            // 删除与之相关的评论
            PhotoCommentDb pcd = new PhotoCommentDb();
            Iterator ir = pcd.list(pcd.getTable().getQueryList(), new Object[]{new Long(ld.getId())}).iterator();
            try {
                while (ir.hasNext()) {
                    pcd = (PhotoCommentDb) ir.next();
                    pcd.del();
                }
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
            
        }
        return re;
    }

    public boolean modify(ServletContext application, HttpServletRequest request) throws ErrMsgException {
        PhotoForm pf = new PhotoForm();
        pf.checkModify(application, request);
        blogId = pf.getBlogId();

        PhotoDb pd = new PhotoDb();
        pd = pd.getPhotoDb(pf.getId());
        pd.setTitle(pf.getTitle());
        pd.setBlogId(pf.getBlogId());
        pd.setDirCode(pf.getDirCode());
        pd.setLocked(pf.isLocked());
        pd.setSort(pf.getSort());
        pd.setCatalog(pf.getCatalog());

        boolean re = pd.save(pf.getFileUpload());
        if (re) {
            try {
                PhotoCatalogDb pcd = new PhotoCatalogDb();
                pcd = (PhotoCatalogDb) pcd.getQObjectDb(new Long(pf.
                        getCatalog()));
                if (pcd!=null) {
                    pcd.set("miniature", pd.getImageSmall());
                    pcd.save();
                }
            } catch (ResKeyException e) {
                throw new ErrMsgException(e.getMessage(request));
            }
        }
        return re;
    }

    public PhotoDb getPhotoDb(int id) {
        PhotoDb ld = new PhotoDb();
        return ld.getPhotoDb(id);
    }

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public long getBlogId() {
        return blogId;
    }

    public boolean dig(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        long photoId = ParamUtil.getLong(request, "photoId");
        PhotoDb pd = new PhotoDb();
        pd = pd.getPhotoDb(photoId);
        long blogId = pd.getBlogId();
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        if (!ucd.isPhotoDig()) {
            throw new ErrMsgException("该博客照片不允许被评分！");
        }

        String userName = Privilege.getUser(request);

        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(Gold.code);

        Config dc = Config.getInstance();
        int pay = dc.getIntProperty("digPhotoCost");

        int digScoreMax = dc.getIntProperty("digPhotoScoreMax");
        int digValue = ParamUtil.getInt(request, "digValue");

        if (digValue>digScoreMax) {
            throw new ErrMsgException("照片评分一次加分最高值不能超过" + digScoreMax);
        }
        boolean re = su.getScore().pay(userName, IPluginScore.SELLER_SYSTEM, pay);
        if (re) {
            // System.out.println(getClass() + " reward=" + reward + " md.getScore()=" + md.getScore());
            pd.setScore(pd.getScore() + digValue);
            re = pd.save(new JdbcTemplate());
        }
        return re;
    }

    public boolean undig(HttpServletRequest request) throws ErrMsgException,ResKeyException {
        if (!Privilege.isUserLogin(request))
            throw new ErrMsgException("请先登录!");
        long photoId = ParamUtil.getLong(request, "photoId");

        PhotoDb pd = new PhotoDb();
        pd = pd.getPhotoDb(photoId);
        long blogId = pd.getBlogId();
        UserConfigDb ucd = new UserConfigDb();
        ucd = ucd.getUserConfigDb(blogId);
        if (!ucd.isPhotoDig()) {
            throw new ErrMsgException("该博客照片不允许被评分！");
        }

        String userName = Privilege.getUser(request);

        ScoreMgr sm = new ScoreMgr();
        ScoreUnit su = sm.getScoreUnit(Gold.code);

        Config dc = Config.getInstance();
        int pay = dc.getIntProperty("undigPhotoCost");

        int digScoreMax = dc.getIntProperty("undigPhotoScoreMax");
        int digValue = ParamUtil.getInt(request, "digValue");

        if (digValue>digScoreMax) {
            throw new ErrMsgException("照片评分一次加分最高值不能超过" + digScoreMax);
        }
        boolean re = su.getScore().pay(userName, IPluginScore.SELLER_SYSTEM, pay);
        if (re) {
            // System.out.println(getClass() + " reward=" + reward + " md.getScore()=" + md.getScore());
            pd.setScore(pd.getScore() - digValue);
            re = pd.save(new JdbcTemplate());
        }
        return re;
    }

    long blogId;
}
