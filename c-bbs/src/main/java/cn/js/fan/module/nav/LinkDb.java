package cn.js.fan.module.nav;

import java.io.*;
import java.sql.*;
import java.util.*;

import javax.servlet.http.*;

import cn.js.fan.db.*;
import cn.js.fan.util.*;
import cn.js.fan.web.*;
import com.redmoon.forum.*;
import com.redmoon.forum.Config;
import com.redmoon.forum.util.*;
import com.redmoon.kit.util.*;
import com.cloudwebsoft.framework.base.ObjectDb;
import com.cloudwebsoft.framework.base.IObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;

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

    public static final String KIND_DEFAULT = "default";
    public static final String KIND_SHOP = "shop"; // 商铺
    public static final String KIND_CMS_SUB_SITE = "subsite"; // 子站点

    public static final String USER_SYSTEM = "system";

    public static final String linkBasePath = "forum/link";

    public LinkDb() {
        super();
    }

    public LinkDb(int id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    private String kind;

    public LinkDb getLinkDb(int id) {
        return (LinkDb)getObjectDb(new Integer(id));
    }

    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new LinkDb(pk.getIntValue());
    }

    public String getImageUrl(HttpServletRequest request) {
        com.redmoon.forum.Config cfg = com.redmoon.forum.Config.getInstance();
        String rootPath;
        // 考虑到当有网闸映射时，内外网的IP可能不一致，如果用Global.getRootPath就会带来问题
        // 判别是否为空，是为了避免自动生成静态首页时，request为空导致异常
        if (request!=null)
            rootPath = request.getContextPath();
        else
            rootPath = Global.getRootPath();
        String attachmentBasePath = rootPath + "/upfile/" +
                                    linkBasePath + "/";
        boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
        if (isFtpUsed) {
            attachmentBasePath = cfg.getRemoteBaseUrl();
            attachmentBasePath += linkBasePath + "/";
        }
        return attachmentBasePath + image;
    }

    public void initDB() {
        this.tableName = "link";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new LinkCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT INTO " + tableName + " (url,title,image,userName,sort,kind,id) VALUES (?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT url,title,image,userName,sort,kind FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET url=?,title=?,image=?,userName=?,sort=?,kind=? WHERE id=?";
        isInitFromConfigDB = false;
    }

    public boolean save(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_SAVE, new Object[] {url, title, image, userName, new Integer(sort), kind, new Integer(id)})==1;
            if (re) {
                LinkCache mc = new LinkCache(this);
                primaryKey.setValue(new Integer(id));
                mc.refreshSave(primaryKey);
                mc.refreshList(getVisualGroupName(kind, userName));
                return true;
            }
            else
                return false;
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        }
        return re;
    }

    public boolean save(ForumFileUpload fu) throws ErrMsgException {
        // 删除图片
        delImage();
        image = writeImage(fu);
        return save(new JdbcTemplate());
    }

    public void load(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        try {
            ResultIterator ri = jt.executeQuery(this.QUERY_LOAD, new Object[] {new Integer(id)});
            if (ri.hasNext()) {
                ResultRecord rr = (ResultRecord)ri.next();
                this.url = rr.getString(1);
                this.title = rr.getString(2);
                this.image = StrUtil.getNullString(rr.getString(3));
                this.userName = rr.getString(4);
                this.sort = rr.getInt(5);
                this.kind = rr.getString(6);
                primaryKey.setValue(new Integer(id));
                loaded = true;
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        }
    }

    public static String getVisualGroupName(String kind, String userName) {
        return kind + "-" + userName;
    }

    public boolean del(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_DEL, new Object[] {new Integer(id)})==1;
            if (re) {
                delImage();
                LinkCache mc = new LinkCache(this);
                mc.refreshDel(primaryKey, getVisualGroupName(kind, userName));
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        }
        return re;
    }

    public void delImage() {
        if (image != null && !image.equals("")) {
            Config cfg = Config.getInstance();
            boolean isFtpUsed = cfg.getBooleanProperty("forum.ftpUsed");
            if (isFtpUsed) {
                    ForumFileUtil ffu = new ForumFileUtil();
                    ffu.delFtpFile(linkBasePath + "/" + image);
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

    public boolean create(JdbcTemplate jt) {
        boolean re = false;
        sort = getMaxOrders(userName, kind);
        id = (int)SequenceMgr.nextID(SequenceMgr.SQ_LINK);
        try {
            re = jt.executeUpdate(this.QUERY_CREATE, new Object[] {url, title,
                                  image, userName, new Integer(sort + 1), kind,
                                  new Integer(id)}) == 1;
            if (re) {
                LinkCache mc = new LinkCache(this);
                mc.refreshCreate(getVisualGroupName(kind, userName));
            }
        }
        catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        }
        return re;
    }

    public boolean create(ForumFileUpload fu) throws ErrMsgException {
        image = writeImage(fu);
        return create(new JdbcTemplate());
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserName(String userName) {
        this.userName = userName;
    }

    public void setSort(int sort) {
        this.sort = sort;
    }

    public void setKind(String kind) {
        this.kind = kind;
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

    public String getUserName() {
        return userName;
    }

    public int getSort() {
        return sort;
    }

    public String getKind() {
        return kind;
    }

    public String getUrl() {
        return url;
    }

    public void setImage(String image) {
        this.image = image;
    }

    public String getImage() {
        return image;
    }

    public boolean move(String direction) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if (direction.equals("up")) {
                if (sort == 0)
                    return true;
                String sql = "select id from " + tableName + " where sort=? and userName=? and kind=?";;
                ResultSet rs = null;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, sort-1);
                ps.setString(2, userName);
                ps.setString(3, kind);
                rs = conn.executePreQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    LinkDb ldb = getLinkDb(id);
                    ldb.setSort(ldb.getSort()+1);
                    ldb.save(new JdbcTemplate());
                }
                //sql = "update link set sort=sort+1 where sort=" +
                //             (sort - 1) + ";";
                //sql += "update link set sort=sort-1 where id=" +
                //        id;
                //conn.executeUpdate(sql);
                LinkDb ldb = getLinkDb(id);
                ldb.setSort(ldb.getSort()-1);
                ldb.save(new JdbcTemplate());
                re = true;
            } else {
                int maxorders = getMaxOrders(userName, kind);
                if (sort == maxorders) {
                    return true;
                } else {
                    String sql = "select id from " + tableName + " where sort=? and userName=? and kind=?";
                    ResultSet rs = null;
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, sort+1);
                    ps.setString(2, userName);
                    ps.setString(3, kind);
                    rs = conn.executePreQuery();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        LinkDb ldb = getLinkDb(id);
                        ldb.setSort(ldb.getSort()-1);
                        ldb.save(new JdbcTemplate());
                    }
                    //sql = "update link set sort=sort+1 where sort=" +
                    //             (sort - 1) + ";";
                    //sql += "update link set sort=sort-1 where id=" +
                    //        id;
                    //conn.executeUpdate(sql);
                    LinkDb ldb = getLinkDb(id);
                    ldb.setSort(ldb.getSort()+1);
                    ldb.save(new JdbcTemplate());
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

    public int getMaxOrders(String userName, String kind) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        int maxorders = -1;
        try {
            String GETMAXORDERS = "select max(sort) from link where userName=? and kind=?";
            PreparedStatement pstmt = conn.prepareStatement(GETMAXORDERS);
            pstmt.setString(1, userName);
            pstmt.setString(2, kind);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    maxorders = rs.getInt(1);
                }
            }
        } catch (SQLException e) {
            logger.error("getMaxOrders:" + e.getMessage());
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

    public String getListSql(String kind, String userName) {
        return "select id from " + getTableName() + " where kind=" + StrUtil.sqlstr(kind) + " and userName=" + StrUtil.sqlstr(userName) + " order by sort";
    }

    private String title;
    private String image = "";
    private String userName;
    private int sort;
    private String url;
}
