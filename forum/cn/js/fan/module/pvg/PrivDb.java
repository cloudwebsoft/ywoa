package cn.js.fan.module.pvg;

import java.sql.*;

import cn.js.fan.base.ObjectDb;
import cn.js.fan.db.*;
import cn.js.fan.web.*;
import org.apache.log4j.*;

public class PrivDb extends ObjectDb {
    String priv;
    String desc;

    static String[] privs = {"admin", "user", "backup", "email", "forum", "forum.message", "chat"};

    public static final String PRIV_ADMIN = privs[0];
    public static final String PRIV_USER = privs[1];
    public static final String PRIV_BACKUP = privs[2];
    public static final String PRIV_EMAIL = privs[3];
    public static final String PRIV_FORUM = privs[4];
    public static final String PRIV_FORUM_MESSAGE = privs[5];
    public static final String PRIV_CHAT = privs[6];

    public PrivDb() {
        init();
    }

    public PrivDb(String priv) {
        init();
        this.priv = priv;
        load();
    }

    public void initDB() {
        tableName = "privilege";
        primaryKey = new PrimaryKey("priv", PrimaryKey.TYPE_STRING);
        objectCache = new PrivCache(this);
        this.isInitFromConfigDB = false;

        QUERY_LIST = "select priv from " + tableName +
                     " order by isSystem desc, priv asc";

        QUERY_CREATE = "insert into privilege (priv, description) values (?,?)";
        QUERY_SAVE = "update privilege set description=? where priv=?";
        QUERY_LOAD = "select priv,description,isSystem from privilege where priv=?";
        QUERY_DEL = "delete from privilege where priv=?";
    }

    public ObjectDb getObjectRaw(PrimaryKey pk) {
        return new PrivDb(pk.getStrValue());
    }

    public PrivDb getPrivDb(String priv) {
        return (PrivDb) getObjectDb(priv);
    }

    public String getPriv() {
        return priv;
    }

    public void setPriv(String priv) {
        this.priv = priv;
    }

    public void setDesc(String d) {
        this.desc = d;
    }

    public void setIsSystem(int isSystem) {
        this.isSystem = isSystem;
    }

    public String getDesc() {
        return this.desc;
    }

    public int getIsSystem() {
        return isSystem;
    }

    public boolean create(String priv, String desc) {
        Conn conn = new Conn(connname);
        boolean re = false;
        try {
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_CREATE);
            pstmt.setString(1, priv);
            pstmt.setString(2, desc);
            re = conn.executePreUpdate()==1?true:false;

            if (re) {
                PrivCache rc = new PrivCache(this);
                rc.refreshCreate();
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_SAVE);
            pstmt.setString(1, desc);
            pstmt.setString(2, priv);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                PrivCache rc = new PrivCache(this);
                primaryKey.setValue(priv);
                rc.refreshSave(primaryKey);
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
            //更新文件内容
            PreparedStatement pstmt = conn.prepareStatement(QUERY_LOAD);
            pstmt.setString(1, priv);
            rs = conn.executePreQuery();
            if (rs != null) {
                if (rs.next()) {
                    priv = rs.getString(1);
                    desc = rs.getString(2);
                    isSystem = rs.getInt(3);
                }
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
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
            pstmt.setString(1, priv);
            re = conn.executePreUpdate()==1?true:false;
            if (re) {
                PrivCache rc = new PrivCache(this);
                rc.refreshDel(primaryKey);
                return true;
            }
        } catch (SQLException e) {
            logger.error(e.getMessage());
        } finally {
            if (conn != null) {
                conn.close();
                conn = null;
            }
        }
        return re;
    }

    private int isSystem = 0;
}
