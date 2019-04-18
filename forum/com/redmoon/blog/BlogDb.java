package com.redmoon.blog;

import java.sql.*;
import java.util.*;
import java.util.Date;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.blog.photo.*;

/**
 *
 * <p>Title: 博客总的信息（推荐博客、推荐音乐、推荐照片等）</p>
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
public class BlogDb extends ObjectDb {
    public static final int ID = 1;

    private int id = ID;
    private int todayCount = 0;
    private int userCount = 0;
    private int topicCount = 0;
    private int postCount = 0;
    private int yestodayCount = 0;
    private long newBlogId;

    public BlogDb() {
        id = ID;
        init();
        load();
    }

    /**
     * 取得博客最新发表的n条贴子
     * @return long[]
     */
    public long[] getNewBlogMsgs(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getNewBlogMsgs(n);
    }

    public long[] getHotBlogMsgs(int n, int days) {
        BlogCache fc = new BlogCache(this);
        return fc.getHotBlogMsgs(n, days);
    }

    public long[] getPostRank(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getPostRank(n);
    }

    public long[] getReplyRank(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getReplyRank(n);
    }

    public long[] getNewBlogs(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getNewBlogs(n);
    }

    /**
     * 取得最近更新的博客
     * @param n int
     * @return long[]
     */
    public long[] getNewUpdateBlogs(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getNewUpdateBlogs(n);
    }

    /**
     * 推荐Blog - mzlkr
     * @param n int
     * @return String[]
     */
    public String[] getCommendBlogs(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getCommendBlogs(n);
    }

    public int[] getNewPhotos(int n) {
        BlogCache fc = new BlogCache(this);
        return fc.getNewPhotos(n);
    }

    /**
     * 取得博客类别中某一类文章的前n篇
     * @param n int 前n篇
     * @param kind String 博客类别（博客类别节点的父结点为root）
     * @return long[] 贴子ID数组
     */
    public long[] getBlogMsgsOfKind(int n, String kind) {
        BlogCache fc = new BlogCache(this);
        return fc.getBlogMsgsOfKind(n, kind);
    }

    public ObjectDb getObjectDb(Object primaryKeyValue) {
        BlogCache fc = new BlogCache(this);
        PrimaryKey pk = (PrimaryKey)primaryKey.clone();
        pk.setValue(primaryKeyValue);
        return (BlogDb)fc.getObjectDb(pk);
    }

    public static BlogDb getInstance() {
        // 使用static blogDb，不能集群
        BlogDb blogDb = new BlogDb();
        blogDb = blogDb.getBlogDb();
        return blogDb;
    }

    public BlogDb getBlogDb() {
        return (BlogDb)getObjectDb(new Integer(ID));
    }

    public boolean del() {
        return false;
    }

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new BlogDb();
    }

    public void setQueryCreate() {
    }

    public void setQuerySave() {
        this.QUERY_SAVE =
            "update blog set blogCount=?,newBlogId=?, todayCount=?, topicCount=?, postCount=?, yestodayCount=?, todayDate=?,star=?,homeClasses=?,recommandBlogs=?,recommandPhoto=?,recommandMusic=?,recommandVideo=? where id=?";
    }

    public void setQueryDel() {
    }

    public void setQueryLoad() {
        this.QUERY_LOAD =
            "select blogCount, newBlogId, todayCount, topicCount, postCount, yestodayCount, todayDate,star,homeClasses,recommandBlogs,recommandPhoto,recommandMusic,recommandVideo from blog where id=?";
    }

    public void setQueryList() {
    }

    public boolean save() {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setInt(1, blogCount);
            ps.setLong(2, newBlogId);
            ps.setInt(3, todayCount);
            ps.setInt(4, topicCount);
            ps.setInt(5, postCount);
            ps.setInt(6, yestodayCount);
            ps.setString(7, DateUtil.toLongString(todayDate));
            ps.setString(8, star);
            ps.setString(9, homeClasses);
            ps.setString(10, recommandBlogs);
            ps.setString(11, recommandPhoto);
            ps.setString(12, recommandMusic);
            ps.setString(13, recommandVideo);
            ps.setInt(14, id);
            rowcount = conn.executePreUpdate();
            BlogCache uc = new BlogCache(this);
            primaryKey.setValue(new Integer(this.id));
            uc.refreshSave(primaryKey);
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            ps.setInt(1, id);
            primaryKey.setValue(new Integer(id));
            rs = conn.executePreQuery();
            if (rs.next()) {
                blogCount = rs.getInt(1);
                newBlogId = rs.getLong(2);
                todayCount = rs.getInt(3);
                topicCount = rs.getInt(4);
                postCount = rs.getInt(5);
                yestodayCount = rs.getInt(6);
                todayDate = DateUtil.parse(rs.getString(7));
                star = StrUtil.getNullString(rs.getString(8));
                homeClasses = rs.getString(9);
                recommandBlogs = StrUtil.getNullStr(rs.getString(10));
                recommandPhoto = StrUtil.getNullStr(rs.getString(11));
                recommandMusic = StrUtil.getNullStr(rs.getString(12));
                recommandVideo = StrUtil.getNullStr(rs.getString(13));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
        finally {
            if (conn!=null) {
                conn.close();
                conn = null;
            }
        }
    }

    public Object[] getObjectBlock(String query, int startIndex) {
        return null;
    }

    public void setPrimaryKey() {
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTodayCount(int todayCount) {
        this.todayCount = todayCount;
    }

    public void setUserCount(int userCount) {
        this.userCount = userCount;
    }

    public void setTopicCount(int topicCount) {
        this.topicCount = topicCount;
    }

    public void setPostCount(int postCount) {
        this.postCount = postCount;
    }

    public void setYestodayCount(int yestodayCount) {
        this.yestodayCount = yestodayCount;
    }

    public int getTodayCount() {
        return todayCount;
    }

    public int getUserCount() {
        return userCount;
    }

    public int getTopicCount() {
        return topicCount;
    }

    public int getPostCount() {
        return postCount;
    }

    public int getYestodayCount() {
        return yestodayCount;
    }

    public Date getTodayDate() {
        return todayDate;
    }

    public int getBlogCount() {
        return blogCount;
    }

    public long getNewBlogId() {
        return newBlogId;
    }

    public String getStar() {
        return star;
    }

    public String getHomeClasses() {
        return homeClasses;
    }

    public String getRecommandBlogs() {
        return recommandBlogs;
    }

    public String getRecommandMusic() {
        return recommandMusic;
    }

    public String getRecommandPhoto() {
        return recommandPhoto;
    }

    public String getRecommandVideo() {
        return recommandVideo;
    }

    /**
     * 更新统计信息
     * @param isAddNew boolean 是否为发新贴而不是回贴
     */
    public void setStatics(boolean isAddNew) {
        // 从数据库中取出今天日期
        Calendar todaydb = Calendar.getInstance();
        if (todayDate!=null)
            todaydb.setTime(todayDate);
        Calendar today = Calendar.getInstance();
        // 如果today_date字段中为当前日期，则today_count加1
        if (DateUtil.datediff(todaydb, today) == 0) {
            setTodayCount(todayCount + 1);
        } else { // 如果字段日期与本日不一致，则说明是本日第一贴
            setYestodayCount(todayCount);
            todayCount = 1;
            todayDate = today.getTime();
        }
        if (isAddNew)
            setTopicCount(topicCount + 1);
        setPostCount(postCount + 1);
        save();
    }

    // 重新载入
    public void reload() {
        BlogCache uc = new BlogCache(this);
        primaryKey.setValue(new Integer(this.id));
        uc.refreshSave(primaryKey);
    }

    public void setBlogCount(int blogCount) {
        this.blogCount = blogCount;
    }

    public void setNewBlogId(long newBlogId) {
        this.newBlogId = newBlogId;
    }

    public void setStar(String star) {
        this.star = star;
    }

    public void setHomeClasses(String homeClasses) {
        this.homeClasses = homeClasses;
    }

    public void setRecommandBlogs(String recommandBlogs) {
        this.recommandBlogs = recommandBlogs;
    }

    public void setRecommandVideo(String recommandVideo) {
        this.recommandVideo = recommandVideo;
    }

    public void setRecommandPhoto(String recommandPhoto) {
        this.recommandPhoto = recommandPhoto;
    }

    public void setRecommandMusic(String recommandMusic) {
        this.recommandMusic = recommandMusic;
    }

    /**
     * 暂无用处
     * @return Vector
     */
    public Vector getAllHomeClasses() {
        Vector v = new Vector();
        if (homeClasses!=null && !homeClasses.equals("")) {
            Leaf lf = new Leaf();
            homeClasses = homeClasses.replaceAll("，", ",");
            String[] ids = homeClasses.split("\\,");
            int len = ids.length;
            for (int i = 0; i < len; i++) {
                String str = ids[i].trim();
                // System.out.print("getAllHomeClasses str=" + str);
                if (lf==null)
                    lf = new Leaf();
                lf = lf.getLeaf(str);
                if (lf != null && lf.isLoaded())
                    v.addElement(lf);
            }
        }
        return v;
    }

    /**
     * 取得所有的推荐博客
     * @return Vector
     */
    public Vector getAllRecommandBlogs() {
        Vector v = new Vector();
        if (recommandBlogs!=null && !recommandBlogs.equals("")) {
            UserConfigDb ucd = new UserConfigDb();
            recommandBlogs = recommandBlogs.replaceAll("，", ",");
            String[] ids = recommandBlogs.split("\\,");
            int len = ids.length;
            for (int i = 0; i < len; i++) {
                String id = ids[i].trim();
                    ucd = ucd.getUserConfigDb(StrUtil.toInt(id));
                    if (ucd != null && ucd.isLoaded())
                        v.addElement(ucd);
                    else
                        ucd = new UserConfigDb();
            }
        }
        return v;
    }

    public Vector getAllRecommandPhoto() {
        Vector v = new Vector();
        if (recommandPhoto!=null && !recommandPhoto.equals("")) {
            PhotoDb pd = new PhotoDb();
            recommandPhoto = recommandPhoto.replaceAll("，", ",");
            String[] ids = recommandPhoto.split("\\,");
            int len = ids.length;
            for (int i = 0; i < len; i++) {
                String id = ids[i].trim();
                pd = (PhotoDb)pd.getPhotoDb(Long.parseLong(id));
                if (pd != null && pd.isLoaded())
                    v.addElement(pd);
                else
                    pd = new PhotoDb();
            }
        }
        return v;
    }


    public Vector getAllRecommandMusic() {
        Vector v = new Vector();
        if (recommandMusic!=null && !recommandMusic.equals("")) {
            MusicDb md = new MusicDb();
            recommandMusic = recommandMusic.replaceAll("，", ",");
            String[] ids = recommandMusic.split("\\,");
            int len = ids.length;
            for (int i = 0; i < len; i++) {
                String id = ids[i].trim();
                md = (MusicDb)md.getQObjectDb(new Long(Long.parseLong(id)));
                if (md != null && md.isLoaded())
                    v.addElement(md);
                else
                    md = new MusicDb();
            }
        }
        return v;
    }

    public Vector getAllRecommandVideo() {
        Vector v = new Vector();
        if (recommandVideo!=null && !recommandVideo.equals("")) {
            VideoDb vd = new VideoDb();
            recommandVideo = recommandVideo.replaceAll("，", ",");
            String[] ids = recommandVideo.split("\\,");
            int len = ids.length;
            for (int i = 0; i < len; i++) {
                String id = ids[i].trim();
                vd = (VideoDb)vd.getQObjectDb(new Long(Long.parseLong(id)));
                if (vd != null && vd.isLoaded())
                    v.addElement(vd);
                else
                    vd = new VideoDb();
            }
        }
        return v;
    }


    private Date todayDate;
    private int blogCount;
    private String star;
    private String homeClasses;
    private String recommandBlogs;
    private String recommandPhoto;
    private String recommandMusic;
    private String recommandVideo;
}
