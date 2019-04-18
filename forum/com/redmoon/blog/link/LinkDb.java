package com.redmoon.blog.link;

import java.io.*;
import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;
import com.redmoon.forum.Config;
import javax.servlet.http.HttpServletRequest;
import cn.js.fan.web.Global;
import com.redmoon.forum.SequenceMgr;

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
public class LinkDb extends ObjectDb {
    private int id;

    public static final String linkBasePath = "blog/link";

    public LinkDb() {
        super();
    }

    public LinkDb(int id) {
        this.id = id;
        init();
        load();
    }

    public LinkDb getLinkDb(int id) {
        return (LinkDb)getObjectDb(new Integer(id));
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new LinkDb(pk.getIntValue());
    }

    public void initDB() {
        this.tableName = "blog_link";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new LinkCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT INTO " + tableName + " (url,title,image,blog_id,sort,is_remote,id) VALUES (?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT url,title,image,blog_id,sort,is_remote FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET url=?,title=?,image=?,blog_id=?,sort=?,is_remote=? WHERE id=?";
        isInitFromConfigDB = false;
    }

    public boolean save() throws ErrMsgException {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_SAVE);
            pstmt.setString(1, url);
            pstmt.setString(2, title);
            pstmt.setString(3, image);
            pstmt.setLong(4, blogId);
            pstmt.setInt(5, sort);

            Config cfg = Config.getInstance();
            remote = cfg.getBooleanProperty("forum.ftpUsed");

            pstmt.setInt(6, remote?1:0);
            pstmt.setInt(7, id);
            if (conn.executePreUpdate() == 1) {
                LinkCache mc = new LinkCache(this);
                primaryKey.setValue(new Integer(id));
                mc.refreshSave(primaryKey);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("更新链接时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean save(ForumFileUpload fu) throws ErrMsgException {
        // 删除图片
        delImage();
        image = writeImage(fu);
        return save();
    }

    public void load() {
        // Based on the id in the object, get the message data from the database.
        Conn conn = new Conn(connname);
        PreparedStatement pstmt = null;
        ResultSet rs = null;
        try {
            pstmt = conn.prepareStatement(this.QUERY_LOAD);
            pstmt.setInt(1, id);
            //url,title,image,userName,sort,kind
            rs = conn.executePreQuery();
            if (rs.next()) {
                this.url = rs.getString(1);
                this.title = rs.getString(2);
                this.image = rs.getString(3);
                this.blogId = rs.getLong(4);
                this.sort = rs.getInt(5);
                this.remote = rs.getInt(6)==1;
                primaryKey.setValue(new Integer(id));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() throws ErrMsgException {
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_DEL);
            pstmt.setInt(1, id);
            if (conn.executePreUpdate()==1) {
                delImage();

                LinkCache mc = new LinkCache(this);
                mc.refreshDel(primaryKey);
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error(e.getMessage());
            throw new ErrMsgException("删除出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public String getImageUrl(HttpServletRequest request) {
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String attachmentBasePath = request.getContextPath() + "/upfile/" +
                                    linkBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (remote && isFtpUsed) {
            attachmentBasePath = cfg.getRemoteBaseUrl();
            attachmentBasePath += linkBasePath + "/";
        }
        return attachmentBasePath + image;
    }

    public void delImage() {
        if (image != null && !image.equals("")) {
            Config cfg = Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                if (remote) {
                    ForumFileUtil ffu = new ForumFileUtil();
                    ffu.delFtpFile(linkBasePath + "/" + image);
                }
            } else {
                try {
                    File file = new File(Global.realPath + "upfile/" + linkBasePath + "/" + image);
                    file.delete();
                } catch (Exception e) {
                    logger.info(e.getMessage());
                }
            }
        }
    }

    public String writeImage(ForumFileUpload fu) {
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
                vpath = linkBasePath + "/" + year + "/" + month + "/";
                String filepath = Global.getRealPath() + "upfile/" + vpath;
                // 置本地路径
                fu.setSavePath(filepath);
                // 置远程路径
                fu.setRemoteBasePath(vpath);
                // 使用随机名称写入磁盘
                fu.writeFile(true);
                return year + "/" + month + "/" + fi.getDiskName();
            }
        }
        return "";
    }

    public boolean create(ForumFileUpload fu) throws ErrMsgException {
        Conn conn = null;
        sort = getMaxOrders(blogId);
        boolean re = false;
        try {
            conn = new Conn(connname);
            PreparedStatement pstmt = conn.prepareStatement(this.QUERY_CREATE);
            pstmt.setString(1, url);
            pstmt.setString(2, title);

            image = writeImage(fu);
            pstmt.setString(3, image);

            pstmt.setLong(4, blogId);
            pstmt.setInt(5, sort+1);

            Config cfg = Config.getInstance();
            remote = cfg.getBooleanProperty("forum.ftpUsed");

            pstmt.setInt(6, remote?1:0);

            id = (int)SequenceMgr.nextID(SequenceMgr.BLOG_LINK);
            pstmt.setInt(7, id);

            re = conn.executePreUpdate() == 1 ? true : false;
            if (re) {
                LinkCache mc = new LinkCache(this);
                mc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ErrMsgException("插入Link时出错！");
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public int getId() {
        return id;
    }

    public String getTitle() {
        return title;
    }

    public long getBlogId() {
        return blogId;
    }

    public int getSort() {
        return sort;
    }

    public String getUrl() {
        return url;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public void setRemote(boolean remote) {
        this.remote = remote;
    }

    public String getImage() {
        return image;
    }

    public boolean isRemote() {
        return remote;
    }

    public boolean move(String direction) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if (direction.equals("up")) {
                if (sort == 0)
                    return true;
                String sql = "select id from " + tableName + " where sort=? and blog_id=?";;
                ResultSet rs = null;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, sort-1);
                ps.setLong(2, blogId);
                rs = conn.executePreQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    LinkDb ldb = getLinkDb(id);
                    ldb.setSort(ldb.getSort()+1);
                    ldb.save();
                }
                //sql = "update link set sort=sort+1 where sort=" +
                //             (sort - 1) + ";";
                //sql += "update link set sort=sort-1 where id=" +
                //        id;
                //conn.executeUpdate(sql);
                LinkDb ldb = getLinkDb(id);
                ldb.setSort(ldb.getSort()-1);
                ldb.save();
                re = true;
            } else {
                int maxorders = getMaxOrders(blogId);
                if (sort == maxorders) {
                    return true;
                } else {
                    String sql = "select id from " + tableName + " where sort=? and blog_id=?";
                    ResultSet rs = null;
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, sort+1);
                    ps.setLong(2, blogId);
                    rs = conn.executePreQuery();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        LinkDb ldb = getLinkDb(id);
                        ldb.setSort(ldb.getSort()-1);
                        ldb.save();
                    }
                    //sql = "update link set sort=sort+1 where sort=" +
                    //             (sort - 1) + ";";
                    //sql += "update link set sort=sort-1 where id=" +
                    //        id;
                    //conn.executeUpdate(sql);
                    LinkDb ldb = getLinkDb(id);
                    ldb.setSort(ldb.getSort()+1);
                    ldb.save();
                    //String sql = "update link set sort=sort-1 where sort=" +
                    //             (sort + 1) + ";";
                    //sql += "update link set sort=sort+1 where id=" +
                    //        id;
                    //conn.executeUpdate(sql);
                }
                re = true;
            }
        } catch (Exception e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public int getMaxOrders(long blogId) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            String GETMAXORDERS = "select max(sort) from " + tableName + " where blog_id=?";
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setLong(1, blogId);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (rs != null) {
                try {
                    rs.close();
                } catch (Exception e) {}
                rs = null;
            }
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return maxorders;
    }

    private String title;
    private String image = "";
    private long blogId;
    private int sort;
    private String url;
    private boolean remote = false;
}
