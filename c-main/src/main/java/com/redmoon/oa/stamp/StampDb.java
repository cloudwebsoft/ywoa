package com.redmoon.oa.stamp;

import java.io.File;
import java.sql.*;
import java.util.Vector;

import javax.servlet.http.HttpServletRequest;

import cn.js.fan.db.*;
import cn.js.fan.util.ErrMsgException;
import cn.js.fan.util.StrUtil;
import com.cloudweb.oa.service.IFileService;
import com.cloudweb.oa.utils.SpringUtil;
import com.cloudwebsoft.framework.base.IObjectDb;
import com.cloudwebsoft.framework.base.ObjectDb;
import com.cloudwebsoft.framework.db.JdbcTemplate;
import com.cloudwebsoft.framework.util.LogUtil;
import com.redmoon.kit.util.FileInfo;
import com.redmoon.kit.util.FileUpload;
import com.redmoon.oa.db.SequenceManager;
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
public class StampDb extends ObjectDb {
    private int id;

    public static final String KIND_DEFAULT = "default";

    public static final String LINK_BASE_PATH = "stamp";

    public StampDb() {
        super();
    }

    public StampDb(int id) {
        this.id = id;
        init();
        load(new JdbcTemplate());
    }

    private String kind;

    public StampDb getStampDb(int id) {
        return (StampDb)getObjectDb(id);
    }

    public StampDb getStampDbByName(String name) {
        JdbcTemplate jt = new JdbcTemplate();
        String sql = "select id from " + tableName + " where title=?";
        ResultIterator ri;
        try {
            ri = jt.executeQuery(sql, new Object[] {name});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                return getStampDb(rr.getInt(1));
            }
        } catch (SQLException ex) {
            LogUtil.getLog(getClass()).error(ex);
        }

        return null;
    }

    @Override
    public IObjectDb getObjectRaw(PrimaryKey pk) {
        return new StampDb(pk.getIntValue());
    }

    public String getImageUrl() {
        return "upfile/" + LINK_BASE_PATH + "/" + image;
    }

    @Override
    public void initDB() {
        this.tableName = "oa_stamp";
        primaryKey = new PrimaryKey("id", PrimaryKey.TYPE_INT);
        objectCache = new StampCache(this);

        this.QUERY_DEL =
                "delete FROM " + tableName + " WHERE id=?";
        this.QUERY_CREATE =
                "INSERT INTO " + tableName + " (title,image,userNames,sort,kind,id,pwd,roleCodes) VALUES (?,?,?,?,?,?,?,?)";
        this.QUERY_LOAD =
                "SELECT title,image,userNames,sort,kind,pwd,roleCodes FROM " + tableName + " WHERE id=?";
        this.QUERY_SAVE =
                "UPDATE " + tableName + " SET title=?,image=?,userNames=?,sort=?,kind=?,pwd=?,roleCodes=? WHERE id=?";
        isInitFromConfigDB = false;
    }

    @Override
    public boolean save(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_SAVE, new Object[] {title, image, userNames, sort, kind, pwd, roleCodes, id})==1;
            if (re) {
                StampCache mc = new StampCache(this);
                primaryKey.setValue(id);
                mc.refreshSave(primaryKey);
                mc.refreshList(getVisualGroupName(kind, userNames));
                return true;
            }
            else {
                return false;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("save:" + e.getMessage());
        }
        return re;
    }

    public boolean canUserUse(String userName) {
    	StampPriv sp = new StampPriv();
    	sp.setStampId(id);
    	return sp.canUserSee(userName);
    }

    public boolean save(FileUpload fu) throws ErrMsgException {
        String newImage = writeImage(fu);
        if (!"".equals(newImage)) {
            // 如果上传了图片，则删除原来的图片
            delImage();
            image = newImage;
        }
        return save(new JdbcTemplate());
    }

    @Override
    public void load(JdbcTemplate jt) {
        // Based on the id in the object, get the message data from the database.
        try {
            ResultIterator ri = jt.executeQuery(this.QUERY_LOAD, new Object[] {id});
            if (ri.hasNext()) {
                ResultRecord rr = ri.next();
                this.title = rr.getString(1);
                this.image = StrUtil.getNullString(rr.getString(2));
                this.userNames = rr.getString(3);
                this.sort = rr.getInt(4);
                this.kind = rr.getString(5);
                pwd = StrUtil.getNullStr(rr.getString(6));
                roleCodes = StrUtil.getNullStr(rr.getString(7));
                primaryKey.setValue(id);
                loaded = true;
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error("load:" + e.getMessage());
        }
    }

    public static String getVisualGroupName(String kind, String userName) {
        return kind + "-" + userName;
    }

    @Override
    public boolean del(JdbcTemplate jt) {
        boolean re = false;
        try {
            re = jt.executeUpdate(this.QUERY_DEL, new Object[] {id})==1;
            if (re) {
                delImage();
                StampCache mc = new StampCache(this);
                mc.refreshDel(primaryKey, getVisualGroupName(kind, userNames));
            }
        } catch (SQLException e) {
            LogUtil.getLog(getClass()).error(e.getMessage());
        }
        return re;
    }

    public void delImage() {
        if (image != null && !"".equals(image)) {
            try {
                IFileService fileService = SpringUtil.getBean(IFileService.class);
                fileService.del("upfile/" + LINK_BASE_PATH, image);
            } catch (Exception e) {
                LogUtil.getLog(getClass()).info(e.getMessage());
            }
        }
    }

    public String writeImage(FileUpload fu) throws ErrMsgException {
        if (fu.getRet() == FileUpload.RET_SUCCESS) {
            Vector v = fu.getFiles();
            FileInfo fi = null;
            if (v.size() > 0) {
                fi = (FileInfo) v.get(0);
            }
            if (fi != null) {
                IFileService fileService = SpringUtil.getBean(IFileService.class);
                fileService.write(fi, "upfile/" + LINK_BASE_PATH);
                return fi.getDiskName();
            }
        }
        return "";
    }

    @Override
    public boolean create(JdbcTemplate jt) {
        boolean re = false;
        sort = getMaxOrders(userNames, kind);
        id = (int)SequenceManager.nextID(SequenceManager.OA_STAMP);
        try {
            re = jt.executeUpdate(this.QUERY_CREATE, new Object[] {title,
                                  image, userNames, sort + 1, kind,
                    id, pwd, roleCodes}) == 1;
            if (re) {
                StampCache mc = new StampCache(this);
                mc.refreshCreate(getVisualGroupName(kind, userNames));
            }
        }
        catch (SQLException e) {
            LogUtil.getLog(getClass()).error("create:" + e.getMessage());
        }
        return re;
    }

    public boolean create(FileUpload fu) throws ErrMsgException {
        image = writeImage(fu);
        if ("".equals(image)) {
            throw new ErrMsgException("请选择印章！");
        }
        return create(new JdbcTemplate());
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setUserNames(String userNames) {
        this.userNames = userNames;
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

    public String getUserNames() {
        return userNames;
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

    public void setPwd(String pwd) {
        this.pwd = pwd;
    }

    public void setRoleCodes(String roleCodes) {
        this.roleCodes = roleCodes;
    }

    public String getImage() {
        return image;
    }

    public String getPwd() {
        return pwd;
    }

    public String getRoleCodes() {
        return roleCodes;
    }

    public boolean move(String direction) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            if ("up".equals(direction)) {
                if (sort == 0) {
                    return true;
                }
                String sql = "select id from " + tableName + " where sort=? and userNames=? and kind=?";
                ResultSet rs;
                PreparedStatement ps = conn.prepareStatement(sql);
                ps.setInt(1, sort-1);
                ps.setString(2, userNames);
                ps.setString(3, kind);
                rs = conn.executePreQuery();
                while (rs.next()) {
                    int id = rs.getInt(1);
                    StampDb ldb = getStampDb(id);
                    ldb.setSort(ldb.getSort()+1);
                    ldb.save(new JdbcTemplate());
                }
                StampDb ldb = getStampDb(id);
                ldb.setSort(ldb.getSort()-1);
                ldb.save(new JdbcTemplate());
                re = true;
            } else {
                int maxorders = getMaxOrders(userNames, kind);
                if (sort == maxorders) {
                    return true;
                } else {
                    String sql = "select id from " + tableName + " where sort=? and userNames=? and kind=?";
                    ResultSet rs;
                    PreparedStatement ps = conn.prepareStatement(sql);
                    ps.setInt(1, sort+1);
                    ps.setString(2, userNames);
                    ps.setString(3, kind);
                    rs = conn.executePreQuery();
                    while (rs.next()) {
                        int id = rs.getInt(1);
                        StampDb ldb = getStampDb(id);
                        ldb.setSort(ldb.getSort()-1);
                        ldb.save(new JdbcTemplate());
                    }
                    StampDb ldb = getStampDb(id);
                    ldb.setSort(ldb.getSort()+1);
                    ldb.save(new JdbcTemplate());
                }
                re = true;
            }
        } catch (Exception e) {
            LogUtil.getLog(getClass()).error(StrUtil.trace(e));
        } finally {
            conn.close();
        }
        return re;
    }

    public int getMaxOrders(String userName, String kind) {
        Conn conn = new Conn(connname);
        ResultSet rs;
        int maxorders = -1;
        try {
            String GETMAXORDERS = "select max(sort) from oa_stamp where userNames=? and kind=?";
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
            LogUtil.getLog(getClass()).error("getMaxOrders:" + e.getMessage());
        } finally {
            conn.close();
        }
        return maxorders;
    }

    public String getListSql(String kind) {
        return "select id from " + getTableName() + " where kind=" + StrUtil.sqlstr(kind) + " order by sort";
    }

    private String title;
    private String image = "";
    private String userNames;
    private int sort;
    private String url;
    private String pwd;
    private String roleCodes;
}
