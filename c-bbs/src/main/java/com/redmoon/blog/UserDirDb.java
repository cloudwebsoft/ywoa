package com.redmoon.blog;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.PrimaryKey;
import cn.js.fan.db.Conn;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Vector;
import cn.js.fan.web.SkinUtil;
import cn.js.fan.util.ResKeyException;
import java.util.Iterator;

public class UserDirDb extends ObjectDb {
    private long blogId;
    private String code;

    public static final String DEFAULT = "default"; // 默认所属的目录名称

    public UserDirDb() {
        super();
    }

    public UserDirDb(long blogId, String code) {
        this.blogId = blogId;
        this.code = code;
        init();
        load();
    }

    public static String getDefaultName() {
        return "我的文章";
    }

    /**
     * 取得catalogCode目录下所属的用户目录
     * @param catalogCode String
     * @return Vector
     */
    public Vector ListAllUserDir(String catalogCode) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        String sql = "select blog_id,code from " + tableName +
                     " where catalogCode=? order by addDate desc";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setString(1, catalogCode);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    long blogId = rs.getLong(1);
                    String code = rs.getString(2);
                    v.addElement(getUserDirDb(blogId, code));
                }
            }
        } catch (SQLException e) {
            logger.error("ListAllUserDir(String catalogCode): " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    /**
     * 取得目录下的所有文章数
     * @param dirCode String
     * @return int
     */
    public int getMsgCountOfDir(long blogId, String dirCode) {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            String sql = "select count(*) from sq_message where blog_id=? and isBlog=1 and blogUserDir=?";
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, blogId);
            ps.setString(2, dirCode);
            rs = conn.executePreQuery();
            if (rs.next()) {
                return rs.getInt(1);
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
        return 0;
    }

    public void delDirsOfBlog(long blogId) throws ResKeyException {
        Iterator ir = list(blogId).iterator();
        while (ir.hasNext()) {
            UserDirDb udd = (UserDirDb)ir.next();
            udd.del();
        }
    }

    public boolean del() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_DEL);
            ps.setLong(1, blogId);
            ps.setString(2, code);
            rowcount = conn.executePreUpdate();
            UserDirCache uc = new UserDirCache(this);
            primaryKey.setKeyValue("blog_id", new Long(blogId));
            primaryKey.setKeyValue("code", code);
            uc.refreshDel(primaryKey);
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount==1?true:false;
    }

    public int getObjectCount(String sql) {
        return 0;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new UserDirDb(pk.getKeyLongValue("blog_id"), pk.getKeyStrValue("code"));
    }

    public boolean create() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_CREATE);
            ps.setString(1, code);
            ps.setString(2, dirName);
            ps.setLong(3, blogId);
            ps.setString(4, color);
            ps.setString(5, "" + System.currentTimeMillis());
            rowcount = conn.executePreUpdate();
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public String toOptions(long blogId) {
        String str = "";
        Iterator ir = list(blogId).iterator();
        while (ir.hasNext()) {
            UserDirDb asd = (UserDirDb)ir.next();
            str += "<option value='" + asd.getCode() + "'>" + asd.getDirName() + "</option>";
        }
        return str;
    }

    public Vector list(long blogId) {
        Vector v = new Vector();
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        String sql = "select code from " + tableName +
                     " where blog_id=? order by sort asc";
        try {
            PreparedStatement ps = conn.prepareStatement(sql);
            ps.setLong(1, blogId);
            rs = conn.executePreQuery();
            if (rs != null) {
                while (rs.next()) {
                    String code = rs.getString(1);
                    v.addElement(getUserDirDb(blogId, code));
                }
            }
        } catch (SQLException e) {
            logger.error("list(blogId): " + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return v;
    }

    public boolean save() throws ResKeyException {
        int rowcount = 0;
        Conn conn = null;
        try {
            conn = new Conn(connname);
            PreparedStatement ps = conn.prepareStatement(this.QUERY_SAVE);
            ps.setString(1, dirName);
            ps.setString(2, color);
            ps.setLong(3, blogId);
            ps.setString(4, code);
            rowcount = conn.executePreUpdate();
            UserDirCache uc = new UserDirCache(this);
            primaryKey.setKeyValue("blog_id", new Long(blogId));
            primaryKey.setKeyValue("code", code);
            uc.refreshSave(primaryKey);
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
            throw new ResKeyException(new SkinUtil(), SkinUtil.ERR_DB);
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return rowcount>0? true:false;
    }

    public UserDirDb getUserDirDb(long blogId, String code) {
        primaryKey.setKeyValue("blog_id", new Long(blogId));
        primaryKey.setKeyValue("code", code);
        return (UserDirDb)getObjectDb(primaryKey.getKeys());
    }

    public void load() {
        ResultSet rs = null;
        Conn conn = new Conn(connname);
        try {
            PreparedStatement ps = conn.prepareStatement(this.QUERY_LOAD);
            primaryKey.setKeyValue("blog_id", new Long(blogId));
            primaryKey.setKeyValue("code", code);
            ps.setLong(1, blogId);
            ps.setString(2, code);
            rs = conn.executePreQuery();
            if (rs.next()) {
                dirName = rs.getString(1);
                color = rs.getString(2);
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

    public void setBlogId(long blogId) {
        this.blogId = blogId;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public void setDirName(String dirName) {
        this.dirName = dirName;
    }

    public void setColor(String color) {
        this.color = color;
    }

    public long getBlogId() {
        return blogId;
    }

    public String getCode() {
        return code;
    }

    public String getDirName() {
        return dirName;
    }

    public String getColor() {
        return color;
    }

    private String dirName;
    private String color = "";

}
