package com.redmoon.blog;

import java.sql.*;

import cn.js.fan.base.*;
import cn.js.fan.cache.jcs.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.util.file.*;
import cn.js.fan.web.*;
import com.cloudwebsoft.framework.template.*;
import com.redmoon.blog.photo.*;
import com.redmoon.forum.*;
import com.redmoon.forum.util.VisitTopicLogDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;

public class BlogCache extends ObjectCache implements ICacheMgr {
    final String NEWBLOGMSG = "NEW_BLOG_MSG";
    final String HOTBLOGMSG = "HOT_BLOG_MSG";
    final String POSTRANKBLOG = "POST_RANK_BLOG";
    final String NEWBLOG = "NEW_BLOG";
    final String NEWUPDATEBLOG = "NEW_UPDATE_BLOG";
    final String COMMENDBLOG = "COMMEND_BLOG";
    final String ALLBLOG = "ALL_BLOG";
    final String NEWBLOGPHOTOS = "NEW_BLOG_PHOTOS";
    final String REPLYRANKBLOG = "REPLY_RANK_BLOG";
 
    public BlogCache() {

    }

    public BlogCache(BlogDb blogDb) {
        super(blogDb);
    }

    /**
     * 刷新缓存及生成博客首页
     */
    public void refreshHomePage() {
        try {
            rmCache.invalidateGroup(group);

            Config cfg = Config.getInstance();
            if (cfg.getBooleanProperty("createHtmlWhenRefreshHome")) {
                String filePath = Global.realPath + "blog/template/index.htm";
                TemplateLoader tl = new TemplateLoader(null, filePath);
                FileUtil fu = new FileUtil();
                fu.WriteFile(Global.getRealPath() +
                             "blog/index.htm",
                             tl.toString(), "UTF-8");
            }
        } catch (Exception e) {
            logger.error("refreshHomePage:" + StrUtil.trace(e));
        }
    }

    /**
     * 取得前n条最新的照片的ID置于数组中
     * @param n int
     * @return int[]
     */
    public int[] getNewPhotos(int n) {
        // 根据sql语句得出计算总数的sql查询语句
        PhotoDb pd = new PhotoDb();
        String sql = "select id from " + pd.getTableName() +
                     " ORDER BY addDate desc";

        int[] v = null;
        try {
            v = (int[]) rmCache.getFromGroup(NEWBLOGPHOTOS, group);
        } catch (Exception e) {
            logger.error("getNewPhotos1:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                int c = conn.getRows();
                if (c<n)
                    n = c;
                v = new int[n];
                conn.setFetchSize(n);
                int id;
                int i = 0;
                if (rs != null) {
                    while (rs.next()) {
                        id = rs.getInt(1);
                        v[i] = id;
                        i++;
                    }
                }
            } catch (SQLException e) {
                logger.error("getNewPhotos2:" + e.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v != null && v.length > 0) {
                try {
                    rmCache.putInGroup(NEWBLOGPHOTOS, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 取得博客新发的n篇文章
     * @param n int
     * @return long[]
     */
    public long[] getNewBlogMsgs(int n) {
        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from sq_thread where check_status=" +
                     MsgDb.CHECK_STATUS_PASS +
                     " and isBlog=1 order by lydate desc";
        long[] v = new long[0];
        try {
            v = (long[]) rmCache.getFromGroup(NEWBLOGMSG, group);
        } catch (Exception e) {
            logger.error("getTopBlogs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new long[conn.getRows()];
                int id;
                int i = 0;
                while (rs.next()) {
                    id = rs.getInt(1);
                    v[i] = id;
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getTopMsgs:" + e.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(NEWBLOGMSG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 取得最近数天内最热的贴子n条
     * @param n int 数量
     * @param days int 天数
     * @return long[]
     */
    public long[] getHotBlogMsgs(int n, int days) {
        long[] v = new long[0];
        try {
            v = (long[]) rmCache.getFromGroup(HOTBLOGMSG, group);
        } catch (Exception e) {
            logger.error("getHotBlogMsgs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            VisitTopicLogDb bvld = new VisitTopicLogDb();

            java.util.Date d = DateUtil.addDate(new java.util.Date(), -days);

            String sql = "select count(*) as s, topic_id from " +
                         bvld.getTable().getName() +
                         " where is_blog=1 and add_date>=? group by topic_id order by s desc";
            ResultIterator ri = null;
            JdbcTemplate jt = new JdbcTemplate();
            try {
                ri = jt.executeQuery(sql, new Object[] {d}, 1, n);
                v = new long[ri.size()];
                int i = 0;
                while (ri.hasNext()) {
                    ResultRecord rr = (ResultRecord) ri.next();
                    v[i] = rr.getLong(2);
                    i++;
                }
            }
            catch (SQLException e) {
                LogUtil.getLog(getClass()).error(StrUtil.trace(e));
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(HOTBLOGMSG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 发表排行
     * @param n int
     * @return String[]
     */
    public long[] getPostRank(int n) {
        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from blog_user_config order by msgCount desc";
        long[] v = null;
        try {
            v = (long[]) rmCache.getFromGroup(POSTRANKBLOG, group);
        } catch (Exception e) {
            logger.error("getPostRank:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new long[conn.getRows()];
                int i = 0;
                while (rs.next()) {
                    v[i] = rs.getLong(1);
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getPostRank:" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(POSTRANKBLOG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 评论排行
     * @param n int
     * @return String[]
     */
    public long[] getReplyRank(int n) {
        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from blog_user_config order by replyCount desc";
        long[] v = null;
        try {
            v = (long[]) rmCache.getFromGroup(REPLYRANKBLOG, group);
        } catch (Exception e) {
            logger.error("getPostRank:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new long[conn.getRows()];
                int i = 0;
                while (rs.next()) {
                    v[i] = rs.getLong(1);
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getPostRank:" + e.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(REPLYRANKBLOG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }


    /**
     * 取得最新加入
     * @param n int
     * @return String[]
     */
    public long[] getNewBlogs(int n) {
        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from blog_user_config order by addDate desc";
        long[] v = null;
        try {
            v = (long[]) rmCache.getFromGroup(NEWBLOG, group);
        } catch (Exception e) {
            logger.error("getNewBlogs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new long[conn.getRows()];
                int i = 0;
                while (rs.next()) {
                    v[i] = rs.getLong(1);
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getNewBlogs:" + e.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(NEWBLOG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 最近更新的博客
     * @param n int
     * @return long[]
     */
    public long[] getNewUpdateBlogs(int n) {
        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from blog_user_config order by update_date desc";
        long[] v = null;
        try {
            v = (long[]) rmCache.getFromGroup(NEWUPDATEBLOG, group);
        } catch (Exception e) {
            logger.error("getNewUpdateBlogs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new long[conn.getRows()];
                int i = 0;
                while (rs.next()) {
                    v[i] = rs.getLong(1);
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getNewUpdateBlogs:" + e.getMessage());
            } finally {
                if (rs != null) {
                    try {
                        rs.close();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    rs = null;
                }
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(NEWUPDATEBLOG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 取得推荐blog
     * author mzlkr
     * @param n int
     * @return String[]
     */
    public String[] getCommendBlogs(int n) {
        String sql =
                "select userName from blog_user_config order by viewcount desc";
        String[] v = new String[0];
        try {
            v = (String[]) rmCache.getFromGroup(COMMENDBLOG, group);
        } catch (Exception e) {
            logger.error("getNewBlogs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new String[conn.getRows()];
                String userName;
                int i = 0;
                while (rs.next()) {
                    userName = rs.getString(1);
                    v[i] = userName;
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getNewBlogs:" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(NEWBLOG, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    /**
     * 取得博客类别中某一类文章的前n篇
     * @param n int 前n篇
     * @param kind String 博客类别（博客类别节点的父结点为root）
     * @return long[] 贴子ID数组
     */
    public long[] getBlogMsgsOfKind(int n, String kind) {
        long[] v = null;

        // 根据sql语句得出计算总数的sql查询语句
        String sql = "select id from sq_thread where check_status=" +
                     MsgDb.CHECK_STATUS_PASS +
                     " and isBlog=1 and blog_dir_code=" + StrUtil.sqlstr(kind) + " order by lydate desc";

        try {
            v = (long[]) rmCache.getFromGroup(sql, group);
        } catch (Exception e) {
            logger.error("getTopBlogs:" + e.getMessage());
        }

        // If already in cache, return the count.
        if (v != null) {
            return v;
        }
        // Otherwise, we have to load the count from the db.
        else {
            Conn conn = new Conn(connname);
            ResultSet rs = null;
            try {
                conn.setMaxRows(n);
                rs = conn.executeQuery(sql);
                conn.setFetchSize(n);
                v = new long[conn.getRows()];
                int id;
                int i = 0;
                while (rs.next()) {
                    id = rs.getInt(1);
                    v[i] = id;
                    i++;
                }
            } catch (SQLException e) {
                logger.error("getBlogMsgsOfKind:" + e.getMessage());
            } finally {
                if (conn != null) {
                    conn.close();
                    conn = null;
                }
            }
            // Add the thread count to cache
            if (v.length > 0) {
                try {
                    rmCache.putInGroup(sql, group, v);
                } catch (Exception e) {
                    logger.error(e.getMessage());
                }
            }
            return v;
        }
    }

    public void setGroup() {
        group = "BLOG_";
    }

    public void setGroupCount() {
        COUNT_GROUP_NAME = "BLOG_COUNT_";
    }

    public void regist() {
        if (!isRegisted) {
            rmCache.regist(this);
            isRegisted = true;

            // System.out.println(getClass() + " is registed.");
        }
    }

    /**
     * 定时刷新缓存
     */
    public void timer() {
        // 刷新全文检索
        curHomeRefreshLife--;
        if (curHomeRefreshLife <= 0) {
            refreshHomePage();
            curHomeRefreshLife = BLOG_HOME_REFRESH_INTERVAL;

            // System.out.println(getClass() + " has done on timer");
        }
    }

    public static long BLOG_HOME_REFRESH_INTERVAL = Config.getInstance().getIntProperty("homeRefreshInterval"); // 5分钟

    private long curHomeRefreshLife = BLOG_HOME_REFRESH_INTERVAL; // one hour

    static boolean isRegisted = false;

}
