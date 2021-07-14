package cn.js.fan.module.cms.ui;

import java.sql.*;
import java.util.*;

import cn.js.fan.base.*;
import cn.js.fan.db.*;
import cn.js.fan.util.*;
import com.cloudwebsoft.framework.db.*;
import com.redmoon.forum.*;
import com.cloudwebsoft.framework.util.LogUtil;

public class DesktopItemDb extends ObjectDb {
    public static String SYSTEM_CODE_CMS = "0";
    public static String SYSTEM_CODE_FORUM = "1";
    public static String SYSTEM_CODE_BLOG = "2";

    public DesktopItemDb() {
        init();
    }

    public DesktopItemDb(int id) {
        init();
        this.id = id;
        load();
    }

    public void initDB() {
        tableName = "sq_desktop_setup";
        primaryKey = new PrimaryKey("ID", PrimaryKey.TYPE_INT);
        objectCache = new DesktopItemCache(this);
        this.isInitFromConfigDB = false;
        QUERY_CREATE = "insert into " + tableName + " (SYSTEM_CODE, TITLE, COUNT, WIN_LEFT, WIN_TOP, WIN_WIDTH, WIN_HEIGHT, MODULE_CODE, MODULE_ITEM, ID, ZINDEX, POSITION, TITLE_LEN, PROPERTIES) values (?,?,?,?,?,?,?,?,?,?,?,?,?,?)";
        QUERY_SAVE = "update " + tableName + " set TITLE=?, COUNT=?, WIN_LEFT=?, WIN_TOP=?, WIN_WIDTH=?, WIN_HEIGHT=?, MODULE_CODE=?, MODULE_ITEM=?, ZINDEX=?, WIN_MIN=?, POSITION=?, TITLE_LEN=?, PROPERTIES=? where ID=?";
        QUERY_LOAD =
                "select SYSTEM_CODE, TITLE, COUNT, WIN_LEFT, WIN_TOP, WIN_WIDTH, WIN_HEIGHT, MODULE_CODE, MODULE_ITEM, ZINDEX, WIN_MIN, POSITION, TITLE_LEN, PROPERTIES from " + tableName + " where ID=?";
        QUERY_DEL = "delete from " + tableName + " where ID=?";
        QUERY_LIST = "select ID from " + tableName;
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new DesktopItemDb(pk.getIntValue());
    }

    public DesktopItemDb getDesktopItemDb(int id) {
        return (DesktopItemDb) getObjectDb(new Integer(id));
    }

    public DesktopItemDb getDesktopItemDb(String systemCode, String position) {
        DesktopItemCache dic = (DesktopItemCache)objectCache;
        return dic.getDesktopItemDb(systemCode, position);
    }

    public DesktopItemDb getDesktopItemDbByPositionRaw(String systemCode, String position) {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            String sql = "select id from " + tableName + " where system_code=? and position=?";
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, systemCode);
            pstmt.setString(2, position);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    id = rs.getInt(1);
                    return getDesktopItemDb(id);
                }
            }
        } catch (SQLException e) {
            logger.error("getDesktopItemDbByPositionRaw:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return null;
    }

    public void delDesktopOfSystem(String sysCode) {
        JdbcTemplate jt = new JdbcTemplate(new com.cloudwebsoft.framework.db.Connection(cn.js.fan.web.Global.getDefaultDB()));
        String sql = "delete from user_desktop_setup where SYSTEM_CODE=?";
        try {
            jt.executeUpdate(sql, new Object[] {sysCode});
        }
        catch (Exception e) {
            logger.error("deleteDesktopOfUser:" + e.getMessage());
        }
    }

    public void setSystemCode(String systemCode) {
        this.systemCode = systemCode;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public void setLeft(int left) {
        this.left = left;
    }

    public void setTop(int top) {
        this.top = top;
    }

    public void setWidth(int width) {
        this.width = width;
    }

    public void setHeight(int height) {
        this.height = height;
    }

    public void setModuleCode(String moduleCode) {
        this.moduleCode = moduleCode;
    }

    public void setModuleItem(String moduleItem) {
        this.moduleItem = moduleItem;
    }

    public void setId(int id) {
        this.id = id;
    }

    public void setZIndex(int zIndex) {
        this.zIndex = zIndex;
    }

    public void setWinMin(boolean winMin) {
        this.winMin = winMin;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public void setTitleLen(int titleLen) {
        this.titleLen = titleLen;
    }

    public void setProperties(String properties) {
        this.properties = properties;
    }

    public String getSystemCode() {
        return systemCode;
    }

    public String getTitle() {
        return title;
    }

    public int getCount() {
        return count;
    }

    public int getLeft() {
        return left;
    }

    public int getTop() {
        return top;
    }

    public int getWidth() {
        return width;
    }

    public int getHeight() {
        return height;
    }

    public String getModuleCode() {
        return moduleCode;
    }

    public String getModuleItem() {
        return moduleItem;
    }

    public int getId() {
        return id;
    }

    public int getZIndex() {
        return zIndex;
    }

    public boolean isWinMin() {
        return winMin;
    }

    public String getPosition() {
        return position;
    }

    public int getTitleLen() {
        return titleLen;
    }

    public String getProperties() {
        return properties;
    }

    public int getMaxZIndexOfUser(String userName) {
        ResultSet rs = null;
        String sql = "select max(zIndex) from " + tableName + " where SYSTEM_CODE=?";
        Conn conn = new Conn(connname);
        try {
            PreparedStatement pstmt = conn.prepareStatement(sql);
            pstmt.setString(1, userName);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    int m = rs.getInt(1);
                    return m;
                }
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return 1;
    }

    public boolean create() {
        zIndex = getMaxZIndexOfUser(systemCode) + 1;
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            id = (int)SequenceMgr.nextID(SequenceMgr.DESKTOP_ITEM);
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, systemCode);
            pstmt.setString(2, title);
            DesktopMgr dm = new DesktopMgr();
            DesktopUnit du = dm.getDesktopUnit(moduleCode);
            if (du.getType().equals(du.TYPE_DOCUMENT))
                count = 500;
            pstmt.setInt(3, count);
            pstmt.setInt(4, left);
            pstmt.setInt(5, top);
            pstmt.setInt(6, width);
            pstmt.setInt(7, height);
            pstmt.setString(8, moduleCode);
            pstmt.setString(9, moduleItem);
            pstmt.setInt(10, id);
            pstmt.setInt(11, zIndex);
            pstmt.setString(12, position);
            pstmt.setInt(13, titleLen);
            pstmt.setString(14, properties);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                DesktopItemCache rc = new DesktopItemCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error("create:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public boolean save() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, title);
            pstmt.setInt(2, count);
            pstmt.setInt(3, left);
            pstmt.setInt(4, top);
            pstmt.setInt(5, width);
            pstmt.setInt(6, height);
            pstmt.setString(7, moduleCode);
            pstmt.setString(8, moduleItem);
            pstmt.setInt(9, zIndex);
            pstmt.setInt(10, winMin?1:0);
            pstmt.setString(11, position);
            pstmt.setInt(12, titleLen);
            pstmt.setString(13, properties);
            pstmt.setInt(14, id);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                DesktopItemCache rc = new DesktopItemCache(this);
                primaryKey.setValue(new Integer(id));
                rc.refreshSave(primaryKey);
                rc.refreshPosition(systemCode, position);
            }
        } catch (SQLException e) {
            logger.error("save:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public void load() {
        Conn conn = new Conn(connname);
        ResultSet rs = null;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setInt(1, id);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    systemCode = StrUtil.getNullStr(rs.getString(1));
                    title = StrUtil.getNullStr(rs.getString(2));
                    count = rs.getInt(3);
                    left = rs.getInt(4);
                    top = rs.getInt(5);
                    width = rs.getInt(6);
                    height = rs.getInt(7);
                    moduleCode = rs.getString(8);
                    moduleItem = StrUtil.getNullString(rs.getString(9));
                    zIndex = rs.getInt(10);
                    winMin = rs.getInt(11)==1;
                    position = StrUtil.getNullString(rs.getString(12));
                    titleLen = rs.getInt(13);
                    properties = StrUtil.getNullString(rs.getString(14));
                    props = parseProps(properties);
                    loaded = true;
                    primaryKey.setValue(new Integer(id));
                }
            }
        } catch (SQLException e) {
            logger.error("load:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
    }

    public boolean del() {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            PreparedStatement pstmt = conn.prepareStatement(QUERY_DEL);
            pstmt.setInt(1, id);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                re = conn.executePreUpdate() >= 0 ? true : false;
                DesktopItemCache rc = new DesktopItemCache(this);
                rc.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            logger.error("del:" + e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    public static HashMap parseProps(String propsStr) {
        HashMap props = new HashMap();
        String[] propPairs = StrUtil.split(propsStr, ",");
        if (propPairs==null)
            return props;
        int len = propPairs.length;
        for (int i=0; i<len; i++) {
            String str = propPairs[i];
            String[] pair = StrUtil.split(str, "=");
            if (pair!=null) {
                if (pair.length<2) {
                    LogUtil.getLog(DesktopItemDb.class).error("Properties " + propsStr + " format is invalid!");
                    break;
                }
                props.put(pair[0].trim(), pair[1].trim());
            }
        }
        return props;
    }

    public HashMap getProps() {
        return props;
    }

    private String systemCode;
    private String title;
    private int count = 10;
    private int left = 0;
    private int top = 0;
    private int width = 200;
    private int height = 100;
    private String moduleCode;
    private String moduleItem;
    private int id;
    private int zIndex = 100;
    private boolean winMin = false;
    private String position;
    private int titleLen;
    private String properties;
    private HashMap props;

}
